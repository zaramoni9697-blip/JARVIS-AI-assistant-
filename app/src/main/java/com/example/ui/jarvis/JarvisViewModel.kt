package com.example.ui.jarvis

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.JarvisBrain
import com.example.data.model.Conversation
import com.example.data.model.DeviceStatus
import com.example.data.model.Memory
import com.example.data.repository.ConversationRepository
import com.example.data.repository.MemoryRepository
import com.example.util.DeviceController
import com.example.util.JarvisVoiceEngine
import com.example.util.LanguageDetectionResult
import com.example.util.LanguageDetector
import com.example.util.VoiceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConfirmationAction(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit
)

class JarvisViewModel(
    application: Application,
    private val userId: String,
    private val memoryRepository: MemoryRepository,
    private val conversationRepository: ConversationRepository
) : AndroidViewModel(application) {

    private val TAG = "JarvisViewModel"

    private val brain = JarvisBrain()
    private val deviceController = DeviceController(application)

    private val _voiceEngine: JarvisVoiceEngine = JarvisVoiceEngine(application) { transcript, isWakeWord ->
        handleTranscript(transcript, isWakeWord)
    }

    val voiceState: StateFlow<VoiceState> = _voiceEngine.voiceState
    val rmsAmplitude: StateFlow<Float> = _voiceEngine.rmsAmplitude

    val chatMessages: StateFlow<List<Conversation>> = conversationRepository.observeConversations(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val memories: StateFlow<List<Memory>> = memoryRepository.observeMemories(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _deviceStatus = MutableStateFlow(deviceController.getDeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    private val _currentLanguageResult = MutableStateFlow(
        LanguageDetectionResult("auto", "Auto Detect", 1.0f, "Auto-detecting language")
    )
    val currentLanguageResult: StateFlow<LanguageDetectionResult> = _currentLanguageResult.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingAssistantText = MutableStateFlow("")
    val streamingAssistantText: StateFlow<String> = _streamingAssistantText.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<ConfirmationAction?>(null)
    val pendingConfirmation: StateFlow<ConfirmationAction?> = _pendingConfirmation.asStateFlow()

    private var currentGenJob: Job? = null
    var userLanguagePreference: String = "auto"
        set(value) {
            field = value
            _voiceEngine.setLanguageMode(value)
        }

    init {
        refreshDeviceStatus()
    }

    fun refreshDeviceStatus() {
        _deviceStatus.value = deviceController.getDeviceStatus()
    }

    fun toggleMicrophone() {
        when (_voiceEngine.voiceState.value) {
            is VoiceState.Listening -> _voiceEngine.stopListening()
            is VoiceState.Speaking -> _voiceEngine.stopSpeaking()
            else -> _voiceEngine.startListening()
        }
    }

    fun stopSpeaking() {
        _voiceEngine.stopSpeaking()
    }

    private fun handleTranscript(transcript: String, isWakeWord: Boolean) {
        // Strip wake phrase if present to get clean user intent
        val cleanPrompt = transcript
            .replace("hey jarvis", "", ignoreCase = true)
            .replace("jarvis", "", ignoreCase = true)
            .replace("জারভিস", "")
            .replace("এই জারভিস", "")
            .replace("হে জারভিস", "")
            .trim()

        val promptToProcess = if (cleanPrompt.isEmpty()) "Hello JARVIS" else cleanPrompt
        sendUserMessage(promptToProcess, mode = "voice")
    }

    fun sendUserMessage(text: String, mode: String = "chat") {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        // 1. Detect language
        val detected = LanguageDetector.detect(trimmed, userLanguagePreference)
        _currentLanguageResult.value = detected

        viewModelScope.launch {
            // Check for immediate local device actions (e.g. Flashlight, Battery, Volume)
            if (checkAndHandleDeviceIntent(trimmed, detected.code)) {
                return@launch
            }

            // Record user message in Firestore
            conversationRepository.addMessage(
                userId = userId,
                role = "user",
                content = trimmed,
                detectedLang = detected.code,
                mode = mode
            )

            // Start AI generation
            _isGenerating.value = true
            _streamingAssistantText.value = ""
            _voiceEngine.setThinking()

            val memoryList = memories.value
            val deviceContextStr = buildDeviceContext()

            currentGenJob?.cancel()
            currentGenJob = viewModelScope.launch {
                val fullResponse = StringBuilder()
                try {
                    brain.generateResponseStream(
                        userPrompt = trimmed,
                        userMemories = memoryList,
                        languagePreference = userLanguagePreference,
                        deviceContext = deviceContextStr
                    ).collect { chunk ->
                        fullResponse.append(chunk)
                        _streamingAssistantText.value = fullResponse.toString()
                    }

                    val finalResponseText = fullResponse.toString().trim()
                    if (finalResponseText.isNotEmpty()) {
                        // Persist assistant message in Firestore
                        conversationRepository.addMessage(
                            userId = userId,
                            role = "assistant",
                            content = finalResponseText,
                            detectedLang = detected.code,
                            mode = mode
                        )

                        // If in voice mode, speak response
                        if (mode == "voice") {
                            _voiceEngine.speak(finalResponseText, detected.code)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating AI response", e)
                } finally {
                    _isGenerating.value = false
                    _streamingAssistantText.value = ""
                }
            }
        }
    }

    private fun checkAndHandleDeviceIntent(input: String, lang: String): Boolean {
        val lower = input.lowercase()

        // Flashlight intents
        if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("ফ্ল্যাশলাইট") || lower.contains("লাইট")) {
            val enable = !lower.contains("off") && !lower.contains("বন্ধ")
            _pendingConfirmation.value = ConfirmationAction(
                title = if (lang == "bn") "ফ্ল্যাশলাইট নিয়ন্ত্রণ" else "Flashlight Control",
                description = if (enable) {
                    if (lang == "bn") "JARVIS ফ্ল্যাশলাইট চালু করতে চায়। অনুমোদন করবেন?" else "JARVIS requests to turn ON the flashlight. Confirm?"
                } else {
                    if (lang == "bn") "JARVIS ফ্ল্যাশলাইট বন্ধ করতে চায়। অনুমোদন করবেন?" else "JARVIS requests to turn OFF the flashlight. Confirm?"
                },
                onConfirm = {
                    deviceController.toggleFlashlight(enable)
                    refreshDeviceStatus()
                    val ack = if (enable) {
                        if (lang == "bn") "ফ্ল্যাশলাইট চালু করা হয়েছে, স্যার।" else "Flashlight activated, Sir."
                    } else {
                        if (lang == "bn") "ফ্ল্যাশলাইট বন্ধ করা হয়েছে।" else "Flashlight turned off, Sir."
                    }
                    respondSystemAck(ack, lang)
                }
            )
            return true
        }

        // Battery check intents
        if (lower.contains("battery") || lower.contains("চার্জ") || lower.contains("ব্যাটারি")) {
            val status = deviceController.getDeviceStatus()
            val ack = if (lang == "bn") {
                "ব্যাটারি চার্জ ${status.batteryLevel}%${if (status.isCharging) " (চার্জ হচ্ছে)" else ""}। সকল সিস্টেম স্বাভাবিক।"
            } else {
                "Power reserves are at ${status.batteryLevel}%${if (status.isCharging) " and charging" else ""}. Power distribution is optimal, Sir."
            }
            respondSystemAck(ack, lang)
            return true
        }

        // Volume control intents
        if (lower.contains("volume") || lower.contains("সাউন্ড") || lower.contains("ভলিউম")) {
            val status = deviceController.getDeviceStatus()
            val ack = if (lang == "bn") {
                "বর্তমান ভলিউম ${status.volumePercent}%।"
            } else {
                "Current media volume is calibrated at ${status.volumePercent}%."
            }
            respondSystemAck(ack, lang)
            return true
        }

        return false
    }

    private fun respondSystemAck(text: String, lang: String) {
        viewModelScope.launch {
            conversationRepository.addMessage(
                userId = userId,
                role = "assistant",
                content = text,
                detectedLang = lang,
                mode = "chat"
            )
            _voiceEngine.speak(text, lang)
        }
    }

    fun dismissConfirmation() {
        _pendingConfirmation.value = null
    }

    fun confirmPendingAction() {
        val action = _pendingConfirmation.value
        _pendingConfirmation.value = null
        action?.onConfirm?.invoke()
    }

    fun analyzeImage(bitmap: Bitmap, prompt: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            val langResult = LanguageDetector.detect(prompt, userLanguagePreference)
            conversationRepository.addMessage(
                userId = userId,
                role = "user",
                content = "[Image Analysis Request] $prompt",
                detectedLang = langResult.code,
                mode = "chat"
            )

            val result = brain.analyzeImageWithPrompt(
                bitmap = bitmap,
                userPrompt = prompt,
                userMemories = memories.value,
                languagePreference = userLanguagePreference
            )

            val analysisText = result.getOrElse { "Visual scanning error: ${it.localizedMessage}" }
            conversationRepository.addMessage(
                userId = userId,
                role = "assistant",
                content = analysisText,
                detectedLang = langResult.code,
                mode = "chat"
            )
            _isGenerating.value = false
        }
    }

    fun saveMemory(title: String, content: String, category: String) {
        viewModelScope.launch {
            memoryRepository.addMemory(userId, title, content, category)
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(userId, memoryId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            conversationRepository.clearHistory(userId)
        }
    }

    fun executeFlashlightToggle() {
        val next = !_deviceStatus.value.flashlightOn
        deviceController.toggleFlashlight(next)
        refreshDeviceStatus()
    }

    fun executeVolumeChange(percent: Int) {
        deviceController.setVolumePercent(percent)
        refreshDeviceStatus()
    }

    private fun buildDeviceContext(): String {
        val status = _deviceStatus.value
        return "Battery: ${status.batteryLevel}% (Charging: ${status.isCharging}), RAM: ${status.freeRamMb}MB free of ${status.totalRamMb}MB, Model: ${status.deviceModel}, OS: ${status.androidVersion}"
    }

    override fun onCleared() {
        super.onCleared()
        _voiceEngine.destroy()
    }
}
