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

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    activeColor: Color = TextPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(OledSurfaceElevated, RoundedCornerShape(12.dp))
            .border(1.dp, OledBorderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = listOf(14, 24, 34, 18, 28, 12, 38, 22, 16, 30, 26, 10, 32, 18, 22, 14)

        for (i in 0 until barCount) {
            val baseH = heights[i % heights.size]
            val animatedFactor by if (isPlaying) {
                infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 350 + (i * 40), easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "barScale$i"
                )
            } else {
                rememberUpdatedState(newValue = 1f)
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((baseH * animatedFactor).dp)
                    .background(
                        if (isPlaying) activeColor else OledBorderMedium,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
