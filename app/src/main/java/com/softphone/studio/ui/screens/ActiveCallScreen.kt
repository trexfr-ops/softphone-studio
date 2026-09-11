package com.softphone.studio.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.theme.*
import com.softphone.studio.ui.components.WaveformVisualizer
import com.softphone.studio.viewmodel.SoftphoneViewModel

@Composable
fun ActiveCallScreen(viewModel: SoftphoneViewModel) {
    val targetNumber by viewModel.activeCallTarget.collectAsState()
    val elapsed by viewModel.activeCallElapsedSeconds.collectAsState()
    val isMuted by viewModel.isCallMuted.collectAsState()
    val isSpeaker by viewModel.isSpeakerOn.collectAsState()
    val isHold by viewModel.isCallOnHold.collectAsState()
    val haptic = LocalHapticFeedback.current

    val mins = String.format("%02d", elapsed / 60)
    val secs = String.format("%02d", elapsed % 60)

    // Orbital Radar Waves
    val transition = rememberInfiniteTransition(label = "ActiveCallRadar")
    val waveScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CallRadarWave"
    )
    val waveAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CallRadarAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))

            // Orbital Pulse Avatar with Radiating Radar Wave
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outermost radar wave
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            scaleX = waveScale
                            scaleY = waveScale
                            alpha = waveAlpha
                        }
                        .border(1.5.dp, EmeraldSuccess, CircleShape)
                )

                // Secondary tighter wave
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            val tightScale = 1.0f + (waveScale - 1f) * 0.45f
                            scaleX = tightScale
                            scaleY = tightScale
                            alpha = (waveAlpha * 1.3f).coerceAtMost(0.4f)
                        }
                        .background(EmeraldSuccessBg, CircleShape)
                )

                // Central Avatar Circle
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(OledSurfaceElevated, CircleShape)
                        .border(2.dp, EmeraldSuccess, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = targetNumber.ifEmpty { "+48 732 458 912" },
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isHold) "CALL ON HOLD" else "SIP TLS Call Active • Warsaw Line",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isHold) TextSecondary else EmeraldSuccess
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$mins:$secs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            WaveformVisualizer(
                isPlaying = !isHold,
                barCount = 10,
                activeColor = EmeraldSuccess,
                modifier = Modifier.width(180.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallCircleControl(
                    icon = Icons.Default.Mic,
                    label = "Mute",
                    isActive = isMuted,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.isCallMuted.value = !isMuted
                    }
                )
                CallCircleControl(
                    icon = Icons.Default.VolumeUp,
                    label = "Speaker",
                    isActive = isSpeaker,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.isSpeakerOn.value = !isSpeaker
                    }
                )
                CallCircleControl(
                    icon = Icons.Default.Pause,
                    label = "Hold",
                    isActive = isHold,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.isCallOnHold.value = !isHold
                    }
                )
            }

            // End Call Button with Spring Touch
            val endInteraction = remember { MutableInteractionSource() }
            val isEndPressed by endInteraction.collectIsPressedAsState()
            val endScale by animateFloatAsState(
                targetValue = if (isEndPressed) 0.88f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = 600f
                ),
                label = "EndCallButtonScale"
            )

            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.endCall()
                },
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = endScale
                        scaleY = endScale
                    },
                shape = CircleShape,
                color = DangerRed,
                interactionSource = endInteraction
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CallCircleControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 600f
        ),
        label = "CallControlScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = CircleShape,
            color = if (isActive) TextPrimary else OledSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, OledBorderSubtle)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) OledBlack else TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
    }
}
