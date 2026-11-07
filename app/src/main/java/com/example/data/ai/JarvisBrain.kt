package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.Memory
import com.example.util.LanguageDetectionResult
import com.example.util.LanguageDetector
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.GenerateContentResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

class JarvisBrain {

    private val TAG = "JarvisBrain"

    private val defaultModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private fun buildSystemContext(
        userMemories: List<Memory>,
        languageResult: LanguageDetectionResult,
        deviceContext: String? = null
    ): String {
        val memoryBlock = if (userMemories.isNotEmpty()) {
            "USER PERSONAL MEMORIES (Recall and reference when relevant):\n" +
                    userMemories.joinToString("\n") { "- [${it.category.uppercase()}] ${it.title}: ${it.content}" }
        } else {
            "No prior personal memories saved yet."
        }

        val deviceBlock = if (!deviceContext.isNullOrBlank()) {
            "DEVICE TELEMETRY & CONTEXT:\n$deviceContext"
        } else {
            ""
        }

        return """
            You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the iconic, highly capable, futuristic AI Assistant inspired by Tony Stark's personal AI.
            
            CORE INSTRUCTIONS:
            1. LANGUAGE & COMMUNICATION (CRITICAL):
               ${languageResult.promptInstruction}
               - You have seamless dual-language fluency in Bangla (বাংলা) and English.
               - If the user talks in Bangla, reply in eloquent, respectful, natural Bangla (সম্মানজনক, প্রমিত ও সাবলীল বাংলা).
               - If the user talks in English, reply in classic JARVIS persona: sophisticated, articulate, helpful, loyal, with subtle dry wit when appropriate.
               - If the user uses code-mixed speech (বাংলিশ / mixed Bangla & English), understand the complete thought and respond naturally in the dominant language.
            
            2. PERSONAL MEMORY CONTEXT:
               $memoryBlock
            
            3. HARDWARE & DEVICE CONTEXT:
               $deviceBlock
            
            4. TONE & BEHAVIOR:
               - Concise, sharp, direct, and exceptionally smart. Avoid unnecessary filler or AI preambles.
               - If the user asks you to perform an Android action (e.g. flashlight, volume, battery check, alarm, open app), acknowledge clearly and provide helpful instructions or confirmation.
               - In coding questions, output clean, production-grade code with markdown blocks.
        """.trimIndent()
    }

    suspend fun generateResponseStream(
        userPrompt: String,
        userMemories: List<Memory>,
        languagePreference: String = "auto",
        deviceContext: String? = null
    ): Flow<String> = flow {
        val languageResult = LanguageDetector.detect(userPrompt, languagePreference)
        val systemContext = buildSystemContext(userMemories, languageResult, deviceContext)
        val fullPrompt = "$systemContext\n\nUser: $userPrompt\nJARVIS:"

        try {
            withTimeout(60_000L) {
                defaultModel.generateContentStream(fullPrompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        this@flow.emit(chunkText)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Streaming timed out", e)
            val timeoutMsg = if (languageResult.code == "bn") {
                "দুঃখিত, সংযোগে বিলম্ব হওয়ায় প্রক্রিয়াটি সম্পন্ন হতে বেশি সময় লেগেছে। দয়া করে পুনরায় বলুন।"
            } else {
                "I apologize, Sir. The neural link timed out after 60 seconds. Please try again."
            }
            emit(timeoutMsg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Streaming generation failed", e)
            emit("Error: ${e.localizedMessage ?: "Neural processing failed."}")
        }
    }

    suspend fun generateResponse(
        userPrompt: String,
        userMemories: List<Memory>,
        languagePreference: String = "auto",
        deviceContext: String? = null
    ): Result<String> {
        return try {
            val languageResult = LanguageDetector.detect(userPrompt, languagePreference)
            val systemContext = buildSystemContext(userMemories, languageResult, deviceContext)
            val fullPrompt = "$systemContext\n\nUser: $userPrompt\nJARVIS:"

            val response = withTimeout(60_000L) {
                defaultModel.generateContent(fullPrompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(IllegalStateException("Empty response from AI core"))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Generation timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed", e)
            Result.failure(e)
        }
    }

    suspend fun analyzeImageWithPrompt(
        bitmap: Bitmap,
        userPrompt: String,
        userMemories: List<Memory>,
        languagePreference: String = "auto"
    ): Result<String> {
        return try {
            val languageResult = LanguageDetector.detect(userPrompt, languagePreference)
            val systemContext = buildSystemContext(userMemories, languageResult)
            val promptText = "$systemContext\n\n[Visual Image Provided]\nUser prompt: $userPrompt\nJARVIS Analysis:"

            val response = withTimeout(60_000L) {
                defaultModel.generateContent(
                    content {
                        image(bitmap)
                        text(promptText)
                    }
                )
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(IllegalStateException("No analysis returned for the image."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Vision analysis timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Vision analysis failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateCodeAssistance(
        actionType: String, // "generate", "explain", "debug", "optimize"
        userQuery: String,
        codeSnippet: String?,
        languagePreference: String = "auto"
    ): Result<String> {
        val langResult = LanguageDetector.detect(userQuery, languagePreference)
        val instructions = when (actionType) {
            "explain" -> "Explain the following code step-by-step with architecture insights, complexity analysis, and best practices."
            "debug" -> "Identify all bugs, performance issues, logic flaws, or security vulnerabilities in this code. Provide the exact fixes and corrected code."
            "optimize" -> "Refactor and optimize this code for memory efficiency, execution speed, and modern Android/Kotlin/Clean Architecture standards."
            else -> "Generate production-grade, modular, well-commented code meeting the user's requirements."
        }

        val prompt = """
            You are JARVIS AI Senior Software Architect.
            Mode: AI Coding Assistant.
            Task: $instructions
            ${langResult.promptInstruction}
            
            User Query: $userQuery
            ${if (!codeSnippet.isNullOrBlank()) "Code Snippet:\n```\n$codeSnippet\n```" else ""}
            
            Provide a clear, expert breakdown followed by the full, formatted code block.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                defaultModel.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(IllegalStateException("No code output generated."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
