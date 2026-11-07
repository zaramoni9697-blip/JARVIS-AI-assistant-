package com.example.util

data class LanguageDetectionResult(
    val code: String,              // "bn", "en", or "mixed"
    val displayName: String,       // "বাংলা", "English", or "Bangla-English Mixed"
    val confidence: Float,
    val promptInstruction: String
)

object LanguageDetector {

    // Unicode block for Bengali: 0980–09FF
    private val BANGLA_CHAR_REGEX = Regex("[\\u0980-\\u09FF]")
    private val ENGLISH_CHAR_REGEX = Regex("[a-zA-Z]")

    fun detect(text: String, userPreference: String = "auto"): LanguageDetectionResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return LanguageDetectionResult(
                code = "en",
                displayName = "English",
                confidence = 1.0f,
                promptInstruction = "Respond in natural English."
            )
        }

        // If user manually overrode language preference in settings (not "auto"), respect it:
        if (userPreference == "bn") {
            return LanguageDetectionResult(
                code = "bn",
                displayName = "বাংলা",
                confidence = 1.0f,
                promptInstruction = "The user preferred Bangla. Respond in natural, polite Bangla (বাংলায় উত্তর দিন)."
            )
        } else if (userPreference == "en") {
            return LanguageDetectionResult(
                code = "en",
                displayName = "English",
                confidence = 1.0f,
                promptInstruction = "The user preferred English. Respond in crisp, articulate English as JARVIS."
            )
        }

        // Automatic Detection (Default):
        var banglaCount = 0
        var englishCount = 0

        for (ch in trimmed) {
            when {
                ch in '\u0980'..'\u09FF' -> banglaCount++
                (ch in 'a'..'z') || (ch in 'A'..'Z') -> englishCount++
            }
        }

        val totalLetters = banglaCount + englishCount
        if (totalLetters == 0) {
            return LanguageDetectionResult(
                code = "en",
                displayName = "English",
                confidence = 0.5f,
                promptInstruction = "Respond naturally as JARVIS."
            )
        }

        val banglaRatio = banglaCount.toFloat() / totalLetters
        val englishRatio = englishCount.toFloat() / totalLetters

        return when {
            // Mixed / Code-switching (both languages present with significant representation)
            banglaCount > 0 && englishCount > 0 && banglaRatio >= 0.15f && englishRatio >= 0.15f -> {
                val dominant = if (banglaRatio >= englishRatio) "bn" else "en"
                LanguageDetectionResult(
                    code = "mixed",
                    displayName = "Bangla-English (Mixed)",
                    confidence = 0.9f,
                    promptInstruction = if (dominant == "bn") {
                        "The user spoke/typed in code-mixed Bangla-English (বাংলিশ/মিশ্র ভাষা). Understand the entire context deeply and respond naturally in polite conversational Bangla, keeping common technical terms in English if appropriate."
                    } else {
                        "The user spoke/typed in code-mixed English-Bangla. Understand the mixed context and respond articulately in English with warmth and precision as JARVIS."
                    }
                )
            }

            banglaRatio > 0.5f -> {
                LanguageDetectionResult(
                    code = "bn",
                    displayName = "বাংলা",
                    confidence = banglaRatio,
                    promptInstruction = "The user addressed you in Bangla. Respond purely in natural, polite, intelligent Bengali (সুন্দর, সাবলীল ও স্পষ্ট বাংলায় উত্তর দিন)."
                )
            }

            else -> {
                LanguageDetectionResult(
                    code = "en",
                    displayName = "English",
                    confidence = englishRatio,
                    promptInstruction = "The user addressed you in English. Respond in natural, articulate, witty, and loyal JARVIS persona."
                )
            }
        }
    }
}
