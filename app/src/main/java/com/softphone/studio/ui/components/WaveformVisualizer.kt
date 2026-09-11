package com.softphone.studio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.softphone.studio.theme.*
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    activeColor: Color = TextPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidAudioTelemetry")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AudioPhaseAnim"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(OledSurfaceElevated, RoundedCornerShape(12.dp))
            .border(1.dp, OledBorderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val baseHeights = listOf(14f, 22f, 32f, 20f, 28f, 16f, 36f, 24f, 18f, 30f, 26f, 14f, 32f, 20f, 26f, 16f)

        for (i in 0 until barCount) {
            val baseH = baseHeights[i % baseHeights.size]
            val animatedHeight = if (isPlaying) {
                // Reactive undulating sine wave with harmonic displacement
                val harmonic = (sin((i * 0.45f) + phase) * 0.45f) + (sin((i * 0.9f) - phase * 1.2f) * 0.25f)
                (baseH * (0.55f + abs(harmonic))).coerceIn(6f, 36f)
            } else {
                (baseH * 0.4f).coerceAtLeast(6f)
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.dp)
                    .background(
                        if (isPlaying) activeColor else OledBorderMedium,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
