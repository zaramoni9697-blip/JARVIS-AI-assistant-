package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VoiceWaveformBar
import com.example.ui.jarvis.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningRed
import com.example.util.VoiceState

@Composable
fun VoiceAssistantScreen(
    jarvisViewModel: JarvisViewModel,
    onBack: () -> Unit
) {
    val voiceState by jarvisViewModel.voiceState.collectAsState()
    val rmsAmplitude by jarvisViewModel.rmsAmplitude.collectAsState()
    val streamingText by jarvisViewModel.streamingAssistantText.collectAsState()
    val detectedLang by jarvisViewModel.currentLanguageResult.collectAsState()
    val chatMessages by jarvisViewModel.chatMessages.collectAsState()

    val lastInteraction = chatMessages.takeLast(2)

    val infiniteTransition = rememberInfiniteTransition(label = "VoiceOrbPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val dynamicScale = if (rmsAmplitude > 0.1f) 1f + (rmsAmplitude * 0.35f) else pulseScale

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        JarvisBackground,
                        Color(0xFF071426),
                        JarvisBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(JarvisSurface)
                        .testTag("voice_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisCyan
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = JarvisSurface,
                    border = BorderStroke(1.dp, Color(0xFF1B365D))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "MODE: DUAL AUTO",
                            color = JarvisNeonMint,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "বাংলা + EN",
                            color = JarvisTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Main Voice Orb Centerpiece
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(dynamicScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                when (voiceState) {
                                    is VoiceState.Listening -> JarvisCyan.copy(alpha = 0.5f)
                                    is VoiceState.Thinking -> JarvisGold.copy(alpha = 0.5f)
                                    is VoiceState.Speaking -> JarvisNeonMint.copy(alpha = 0.5f)
                                    else -> Color(0xFF1B365D).copy(alpha = 0.3f)
                                },
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                JarvisCyan,
                                JarvisNeonMint,
                                Color(0xFF2979FF),
                                JarvisCyan
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable { jarvisViewModel.toggleMicrophone() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF0F264A),
                                    Color(0xFF061020)
                                )
                            )
                        )
                        .border(1.dp, JarvisCyan.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (voiceState) {
                                is VoiceState.Listening -> Icons.Default.Mic
                                is VoiceState.Speaking -> Icons.Default.VolumeUp
                                is VoiceState.Thinking -> Icons.Default.GraphicEq
                                else -> Icons.Default.MicOff
                            },
                            contentDescription = "Voice State",
                            tint = when (voiceState) {
                                is VoiceState.Listening -> JarvisCyan
                                is VoiceState.Thinking -> JarvisGold
                                is VoiceState.Speaking -> JarvisNeonMint
                                else -> JarvisTextSecondary
                            },
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (voiceState) {
                                is VoiceState.Listening -> "LISTENING"
                                is VoiceState.Thinking -> "THINKING"
                                is VoiceState.Speaking -> "SPEAKING"
                                else -> "TAP TO TALK"
                            },
                            color = JarvisTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Waveform
            VoiceWaveformBar(
                amplitude = rmsAmplitude,
                barCount = 32,
                height = 54.dp,
                isActive = voiceState is VoiceState.Listening || voiceState is VoiceState.Speaking
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Transcript / Live Output Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, Color(0xFF1B365D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEURAL SPEECH TRANSCRIPT",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = JarvisCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Detected: ${detectedLang.displayName}",
                                color = JarvisCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val displayText = when {
                        streamingText.isNotEmpty() -> streamingText
                        voiceState is VoiceState.Speaking -> (voiceState as VoiceState.Speaking).text
                        lastInteraction.isNotEmpty() -> lastInteraction.last().content
                        else -> "Say \"Hey JARVIS\" or tap the orb to speak. You can speak in natural Bengali or English without changing settings."
                    }

                    Text(
                        text = displayText,
                        color = JarvisTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (voiceState is VoiceState.Speaking) {
                    Button(
                        onClick = { jarvisViewModel.stopSpeaking() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisWarningRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Speaking",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Interrupt Voice", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { jarvisViewModel.toggleMicrophone() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("voice_assistant_toggle_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (voiceState is VoiceState.Listening) Color(0xFF1B365D) else JarvisCyan,
                        contentColor = if (voiceState is VoiceState.Listening) JarvisCyan else Color(0xFF001B24)
                    )
                ) {
                    Icon(
                        imageVector = if (voiceState is VoiceState.Listening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic Toggle",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (voiceState is VoiceState.Listening) "Stop Listening" else "Activate Voice",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Wake phrase: \"Hey JARVIS\" • \"জারভিস\"",
                color = JarvisTextSecondary.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
