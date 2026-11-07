package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonMint
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactorCore(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    isActive: Boolean = true,
    amplitude: Float = 0f,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRing"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRing"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val dynamicPulse = if (amplitude > 0.1f) 1f + (amplitude * 0.25f) else pulseScale

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.9f

            // 1. Soft glowing outer aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisCyan.copy(alpha = 0.35f * dynamicPulse),
                        JarvisNeonMint.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.15f
                ),
                center = center,
                radius = baseRadius * 1.15f
            )

            // 2. Outer segmented HUD ring
            rotate(outerRotation, pivot = center) {
                val segments = 12
                val sweep = 18f
                for (i in 0 until segments) {
                    val startAngle = (360f / segments) * i
                    drawArc(
                        color = JarvisCyan.copy(alpha = 0.7f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 2f, baseRadius * 2f),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 3. Middle rotating ring with tick marks
            val midRadius = baseRadius * 0.78f
            drawCircle(
                color = Color(0xFF1B365D),
                radius = midRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            rotate(innerRotation, pivot = center) {
                val ticks = 8
                for (i in 0 until ticks) {
                    val angle = Math.toRadians((360.0 / ticks) * i)
                    val r1 = midRadius - 8.dp.toPx()
                    val r2 = midRadius + 8.dp.toPx()
                    val p1 = Offset((center.x + r1 * cos(angle)).toFloat(), (center.y + r1 * sin(angle)).toFloat())
                    val p2 = Offset((center.x + r2 * cos(angle)).toFloat(), (center.y + r2 * sin(angle)).toFloat())
                    drawLine(
                        color = JarvisNeonMint,
                        start = p1,
                        end = p2,
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }

            // 4. Inner glowing energy core
            val coreRadius = baseRadius * 0.52f * dynamicPulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        JarvisCyan,
                        Color(0xFF003845),
                        Color(0xFF0A1628)
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            drawCircle(
                color = JarvisCyan,
                radius = coreRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        // Center typography HUD indicator
        Text(
            text = if (isActive) "JARVIS" else "STANDBY",
            color = Color.White,
            fontSize = (size.value * 0.085f).sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
    }
}
