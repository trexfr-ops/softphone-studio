package com.softphone.studio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.theme.*
import com.softphone.studio.viewmodel.SoftphoneViewModel

@Composable
fun DialerScreen(viewModel: SoftphoneViewModel) {
    val digits by viewModel.dialerInput.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Ambient Breathing Radar Pulse for VoIP Call Ready State
    val infiniteTransition = rememberInfiniteTransition(label = "DialerRadarPulse")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CallRadarScale"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CallRadarAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Display with Fluid Animated Character Stream
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = digits,
                transitionSpec = {
                    if (targetState.length >= initialState.length) {
                        (slideInVertically { it / 2 } + fadeIn(tween(140)))
                            .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(90)))
                    } else {
                        (slideInVertically { -it / 2 } + fadeIn(tween(140)))
                            .togetherWith(slideOutVertically { it / 2 } + fadeOut(tween(90)))
                    }
                },
                label = "DialerDigitStream"
            ) { currentDigits ->
                Text(
                    text = if (currentDigits.isEmpty()) "Enter Number" else currentDigits,
                    fontSize = when {
                        currentDigits.isEmpty() -> 22.sp
                        currentDigits.length > 13 -> 24.sp
                        currentDigits.length > 9 -> 28.sp
                        else -> 32.sp
                    },
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (currentDigits.isEmpty()) TextTertiary else TextPrimary,
                    maxLines = 1
                )
            }

            if (digits.isNotEmpty()) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.backspaceDialer()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Keypad Grid with Tactile Compression
        val keys = listOf(
            listOf(Pair("1", ""), Pair("2", "ABC"), Pair("3", "DEF")),
            listOf(Pair("4", "GHI"), Pair("5", "JKL"), Pair("6", "MNO")),
            listOf(Pair("7", "PQRS"), Pair("8", "TUV"), Pair("9", "WXYZ")),
            listOf(Pair("*", ""), Pair("0", "+"), Pair("#", ""))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for ((num, letters) in row) {
                        DialerKey(
                            number = num,
                            letters = letters,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.pressDialerKey(num)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Action Row with Radar Aura VoIP Call Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clear All Button
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.clearDialer()
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(OledSurfaceElevated, CircleShape)
                    .border(1.dp, OledBorderSubtle, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Green Call Button with Ambient Radar Aura Rings
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulse Ring
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            scaleX = radarScale
                            scaleY = radarScale
                            alpha = radarAlpha
                        }
                        .border(1.5.dp, EmeraldSuccess, CircleShape)
                )

                // Inner Glow Ring
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            val innerScale = 1.0f + (radarScale - 1.0f) * 0.45f
                            scaleX = innerScale
                            scaleY = innerScale
                            alpha = (radarAlpha * 1.4f).coerceAtMost(0.4f)
                        }
                        .background(EmeraldSuccessBg, CircleShape)
                )

                val callInteraction = remember { MutableInteractionSource() }
                val isCallPressed by callInteraction.collectIsPressedAsState()
                val callScale by animateFloatAsState(
                    targetValue = if (isCallPressed) 0.88f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = 600f
                    ),
                    label = "CallButtonSpring"
                )

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.startCall()
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            scaleX = callScale
                            scaleY = callScale
                        },
                    shape = CircleShape,
                    color = EmeraldSuccess,
                    interactionSource = callInteraction
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Start VoIP Call",
                            tint = OledBlack,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(46.dp))
        }
    }
}

@Composable
fun DialerKey(
    number: String,
    letters: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val keyScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 600f
        ),
        label = "DialerKeyScale"
    )

    val keyBorderColor by animateColorAsState(
        targetValue = if (isPressed) ActiveGreen.copy(alpha = 0.6f) else OledBorderSubtle,
        animationSpec = tween(120),
        label = "DialerKeyBorder"
    )

    Surface(
        modifier = modifier
            .height(68.dp)
            .graphicsLayer {
                scaleX = keyScale
                scaleY = keyScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (isPressed) OledSurfaceElevated else OledSurface,
        border = BorderStroke(1.dp, keyBorderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 24.sp
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
