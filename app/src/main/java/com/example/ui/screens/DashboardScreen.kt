package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.JarvisArcReactorCore
import com.example.ui.components.VoiceWaveformBar
import com.example.ui.jarvis.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningRed
import com.example.util.VoiceState

@Composable
fun DashboardScreen(
    jarvisViewModel: JarvisViewModel,
    userProfile: UserProfile?,
    isAdmin: Boolean,
    onNavigateToVoice: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToCoding: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToDevice: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val voiceState by jarvisViewModel.voiceState.collectAsState()
    val rmsAmplitude by jarvisViewModel.rmsAmplitude.collectAsState()
    val deviceStatus by jarvisViewModel.deviceStatus.collectAsState()
    val isGenerating by jarvisViewModel.isGenerating.collectAsState()
    val streamingText by jarvisViewModel.streamingAssistantText.collectAsState()

    var quickInputText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        JarvisBackground,
                        Color(0xFF071428),
                        JarvisBackground
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Top Status & User Profile Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(JarvisNeonMint)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CORE SYSTEM: ONLINE",
                                color = JarvisNeonMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "J.A.R.V.I.S.",
                            color = JarvisCyan,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(JarvisSurface)
                                .testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = JarvisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(JarvisSurface)
                                .testTag("profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = JarvisNeonMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Animated Arc Reactor Core Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisArcReactorCore(
                        size = 170.dp,
                        isActive = voiceState !is VoiceState.Error,
                        amplitude = rmsAmplitude,
                        onClick = {
                            jarvisViewModel.toggleMicrophone()
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val greeting = remember(userProfile) {
                        val name = userProfile?.displayName?.split(" ")?.firstOrNull() ?: "Sir"
                        "Hello, $name. I am JARVIS."
                    }
                    val subGreeting = "আপনার সেবায় সর্বদা প্রস্তুত • At your command"

                    Text(
                        text = greeting,
                        color = JarvisTextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = subGreeting,
                        color = JarvisTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice State HUD Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (voiceState) {
                            is VoiceState.Listening -> JarvisCyan.copy(alpha = 0.2f)
                            is VoiceState.Thinking -> JarvisGold.copy(alpha = 0.2f)
                            is VoiceState.Speaking -> JarvisNeonMint.copy(alpha = 0.2f)
                            else -> JarvisSurface
                        },
                        border = BorderStroke(
                            1.dp,
                            when (voiceState) {
                                is VoiceState.Listening -> JarvisCyan
                                is VoiceState.Thinking -> JarvisGold
                                is VoiceState.Speaking -> JarvisNeonMint
                                else -> Color(0xFF1B365D)
                            }
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            val statusText = when (voiceState) {
                                is VoiceState.Listening -> "● LISTENING FOR SPEECH..."
                                is VoiceState.Thinking -> "✦ NEURAL COGNITION..."
                                is VoiceState.Speaking -> "▶ VOCAL TRANSMISSION..."
                                else -> "WAKE WORD: \"HEY JARVIS\" / \"জারভিস\""
                            }
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = when (voiceState) {
                                    is VoiceState.Listening -> JarvisCyan
                                    is VoiceState.Thinking -> JarvisGold
                                    is VoiceState.Speaking -> JarvisNeonMint
                                    else -> JarvisTextSecondary
                                }
                            )

                            if (voiceState is VoiceState.Speaking) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = JarvisWarningRed,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { jarvisViewModel.stopSpeaking() }
                                )
                            }
                        }
                    }

                    // Live Waveform
                    Spacer(modifier = Modifier.height(8.dp))
                    VoiceWaveformBar(
                        amplitude = rmsAmplitude,
                        isActive = voiceState is VoiceState.Listening || voiceState is VoiceState.Speaking,
                        height = 36.dp
                    )
                }
            }

            // Real-time Streaming Response Card (if active)
            item {
                AnimatedVisibility(
                    visible = isGenerating || streamingText.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                        border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "AI NEURAL LINK",
                                    color = JarvisCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isGenerating) {
                                    Text(
                                        text = "STREAMING...",
                                        color = JarvisNeonMint,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            if (isGenerating) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    color = JarvisCyan,
                                    trackColor = Color(0xFF0B1E38)
                                )
                            }
                            Text(
                                text = streamingText.ifEmpty { "Analyzing input..." },
                                color = JarvisTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Quick Telemetry Row (Battery, RAM, Language Mode)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryChip(
                        title = "BATTERY",
                        value = "${deviceStatus.batteryLevel}%",
                        sub = if (deviceStatus.isCharging) "Charging" else "Optimal",
                        color = JarvisNeonMint,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryChip(
                        title = "MEMORY",
                        value = "${deviceStatus.freeRamMb} MB",
                        sub = "Free RAM",
                        color = JarvisCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryChip(
                        title = "LANGUAGE",
                        value = "Dual Auto",
                        sub = "বাং + EN",
                        color = JarvisGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Navigation Shortcuts Grid
            item {
                Text(
                    text = "COMMAND MODULES",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CommandModuleCard(
                            title = "Voice Assistant",
                            subtitle = "Interactive Speech & Waveform",
                            icon = Icons.Default.GraphicEq,
                            accentColor = JarvisCyan,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToVoice
                        )
                        CommandModuleCard(
                            title = "AI Neural Chat",
                            subtitle = "Dual Bangla & English AI",
                            icon = Icons.Default.Chat,
                            accentColor = JarvisNeonMint,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToChat
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CommandModuleCard(
                            title = "AI Coding Brain",
                            subtitle = "Code Gen, Fixes & Diff",
                            icon = Icons.Default.Code,
                            accentColor = Color(0xFF90CAF9),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCoding
                        )
                        CommandModuleCard(
                            title = "Personal Memory",
                            subtitle = "User Knowledge Vault",
                            icon = Icons.Default.Psychology,
                            accentColor = JarvisGold,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMemory
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CommandModuleCard(
                            title = "Phone Controls",
                            subtitle = "Flashlight, Volume & Alarms",
                            icon = Icons.Default.PhoneAndroid,
                            accentColor = Color(0xFF64B5F6),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToDevice
                        )
                        if (isAdmin) {
                            CommandModuleCard(
                                title = "Admin Panel",
                                subtitle = "User Accounts & Telemetry",
                                icon = Icons.Default.AdminPanelSettings,
                                accentColor = JarvisWarningRed,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToAdmin
                            )
                        } else {
                            CommandModuleCard(
                                title = "System Settings",
                                subtitle = "Language & Voice Config",
                                icon = Icons.Default.Settings,
                                accentColor = JarvisTextSecondary,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToSettings
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sticky Quick-Input Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = JarvisSurface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, Color(0xFF1B365D))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { jarvisViewModel.toggleMicrophone() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceState is VoiceState.Listening) JarvisCyan else Color(0xFF10223D)
                        )
                        .testTag("dashboard_mic_button")
                ) {
                    Icon(
                        imageVector = if (voiceState is VoiceState.Listening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (voiceState is VoiceState.Listening) Color(0xFF001B24) else JarvisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = quickInputText,
                    onValueChange = { quickInputText = it },
                    placeholder = {
                        Text(
                            text = "Ask JARVIS or give a command...",
                            color = JarvisTextSecondary.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_text_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        if (quickInputText.isNotBlank()) {
                            jarvisViewModel.sendUserMessage(quickInputText, mode = "chat")
                            quickInputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan)
                        .testTag("dashboard_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF001B24),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryChip(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = JarvisCardBg,
        border = BorderStroke(1.dp, Color(0xFF1B365D)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = sub,
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CommandModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
        border = BorderStroke(1.dp, Color(0xFF1B365D)),
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            Column {
                Text(
                    text = title,
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
