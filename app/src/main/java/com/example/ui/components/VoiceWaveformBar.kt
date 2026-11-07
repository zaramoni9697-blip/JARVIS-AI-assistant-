package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonMint

@Composable
fun VoiceWaveformBar(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    barCount: Int = 24,
    height: Dp = 50.dp,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformIdlePulse")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val totalWidth = size.width
            val totalHeight = size.height
            val spacing = totalWidth / barCount
            val barWidth = (spacing * 0.55f).coerceAtLeast(3f)

            for (i in 0 until barCount) {
                // Wave function shape centered around the middle bars
                val normalizedIndex = (i - barCount / 2f) / (barCount / 2f)
                val bellCurve = (1f - (normalizedIndex * normalizedIndex)).coerceIn(0.15f, 1f)

                val idleFactor = if (i % 2 == 0) pulse1 else pulse2
                val currentFactor = if (amplitude > 0.08f) {
                    (amplitude * 1.5f * bellCurve).coerceIn(0.12f, 1f)
                } else if (isActive) {
                    (idleFactor * 0.45f * bellCurve).coerceIn(0.1f, 0.6f)
                } else {
                    0.08f
                }

                val barH = (totalHeight * currentFactor).coerceIn(4.dp.toPx(), totalHeight)
                val x = i * spacing + (spacing - barWidth) / 2f
                val y = (totalHeight - barH) / 2f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            JarvisNeonMint,
                            JarvisCyan,
                            Color(0xFF2979FF)
                        ),
                        startY = y,
                        endY = y + barH
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
