package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data object Thinking : VoiceState
    data class Speaking(val text: String) : VoiceState
    data class Error(val message: String) : VoiceState
}

class JarvisVoiceEngine(
    private val context: Context,
    private val onTranscriptReady: (transcript: String, isWakeWord: Boolean) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val TAG = "JarvisVoiceEngine"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    // Real-time audio amplitude for futuristic live voice waveform (values normalized 0f..1f)
    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private var currentLanguageMode: String = "auto" // "auto", "bn", "en"

    init {
        initSpeechRecognizer()
        initTextToSpeech()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@JarvisVoiceEngine)
            }
        } else {
            Log.w(TAG, "Speech recognition is not available on this device")
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context, this)
    }

    fun setLanguageMode(mode: String) {
        currentLanguageMode = mode
    }

    fun startListening() {
        stopSpeaking()
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            // Dynamic language hints based on user selection or auto dual-language recognition
            when (currentLanguageMode) {
                "bn" -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                }
                "en" -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                }
                else -> {
                    // Auto-detection: Support both English and Bengali
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "en-US"))
                }
            }
        }

        try {
            _voiceState.value = VoiceState.Listening
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognizer", e)
            _voiceState.value = VoiceState.Error(e.localizedMessage ?: "Could not start microphone")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _rmsAmplitude.value = 0f
            if (_voiceState.value is VoiceState.Listening) {
                _voiceState.value = VoiceState.Idle
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop listening", e)
        }
    }

    fun setThinking() {
        _voiceState.value = VoiceState.Thinking
    }

    fun speak(text: String, detectedLang: String = "auto") {
        if (!isTtsReady || textToSpeech == null) {
            Log.w(TAG, "TTS not ready yet")
            return
        }

        stopListening()
        _voiceState.value = VoiceState.Speaking(text)

        // Select suitable TTS locale based on detected language
        val targetLocale = when {
            detectedLang == "bn" || (detectedLang == "mixed" && LanguageDetector.detect(text).code == "bn") -> {
                Locale("bn", "BD")
            }
            else -> Locale.US
        }

        val langResult = textToSpeech?.setLanguage(targetLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to default
            textToSpeech?.setLanguage(Locale.US)
        }

        textToSpeech?.setPitch(1.0f)
        textToSpeech?.setSpeechRate(1.05f)

        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        _rmsAmplitude.value = 0f
        if (_voiceState.value is VoiceState.Speaking) {
            _voiceState.value = VoiceState.Idle
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up voice engine", e)
        }
    }

    // --- RecognitionListener Callbacks ---

    override fun onReadyForSpeech(params: Bundle?) {
        _voiceState.value = VoiceState.Listening
    }

    override fun onBeginningOfSpeech() {
        _voiceState.value = VoiceState.Listening
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Normalize rmsdB (typically -2dB to 10dB) into 0f..1f for UI waveform animation
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
        _rmsAmplitude.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _rmsAmplitude.value = 0f
        _voiceState.value = VoiceState.Thinking
    }

    override fun onError(error: Int) {
        _rmsAmplitude.value = 0f
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection required"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
            else -> "Speech recognition paused"
        }
        Log.w(TAG, "SpeechRecognizer error: $error ($errorMsg)")
        // Non-fatal error returns to Idle gracefully
        _voiceState.value = VoiceState.Idle
    }

    override fun onResults(results: Bundle?) {
        _rmsAmplitude.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()

        if (!text.isNullOrBlank()) {
            val isWake = checkForWakeWord(text)
            onTranscriptReady(text, isWake)
        } else {
            _voiceState.value = VoiceState.Idle
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            // Check for immediate wake-word trigger in continuous stream
            if (checkForWakeWord(text)) {
                onTranscriptReady(text, true)
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun checkForWakeWord(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("hey jarvis") ||
               lower.contains("jarvis") ||
               lower.contains("জারভিস") ||
               lower.contains("এই জারভিস") ||
               lower.contains("হে জারভিস")
    }

    // --- TextToSpeech.OnInitListener Callback ---

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _rmsAmplitude.value = 0.6f
                }

                override fun onDone(utteranceId: String?) {
                    _rmsAmplitude.value = 0f
                    if (_voiceState.value is VoiceState.Speaking) {
                        _voiceState.value = VoiceState.Idle
                    }
                }

                override fun onError(utteranceId: String?) {
                    _rmsAmplitude.value = 0f
                    if (_voiceState.value is VoiceState.Speaking) {
                        _voiceState.value = VoiceState.Idle
                    }
                }
            })
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status: $status")
        }
    }
}
