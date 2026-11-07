package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val languagePreference: String = "auto", // "auto", "bn", "en"
    val role: String = "user", // "user", "admin"
    val status: String = "active", // "active", "suspended"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Memory(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "general", // "preference", "fact", "task", "coding", "general"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "user", // "user", "assistant"
    val content: String = "",
    val detectedLanguage: String = "auto", // "bn", "en", "mixed"
    val mode: String = "chat", // "voice", "chat", "coding"
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppStats(
    val totalUsers: Long = 0L,
    val totalConversations: Long = 0L,
    val systemHealth: String = "All Systems Nominal",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class LanguageMode(val key: String, val label: String, val flag: String) {
    AUTO("auto", "Auto Detect", "🌐"),
    BANGLA("bn", "বাংলা", "🇧🇩"),
    ENGLISH("en", "English", "🇬🇧")
}

data class DeviceStatus(
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val flashlightOn: Boolean = false,
    val volumePercent: Int = 50,
    val freeRamMb: Long = 0L,
    val totalRamMb: Long = 0L,
    val deviceModel: String = "",
    val androidVersion: String = ""
)
