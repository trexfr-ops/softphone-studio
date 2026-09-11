package com.softphone.studio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.model.VoicemailItem
import com.softphone.studio.theme.*
import com.softphone.studio.ui.components.TactileButton
import com.softphone.studio.ui.components.WaveformVisualizer
import com.softphone.studio.viewmodel.SoftphoneViewModel

@Composable
fun VoicemailScreen(viewModel: SoftphoneViewModel) {
    val voicemails by viewModel.voicemails.collectAsState()
    val playingId by viewModel.playingVoicemailId.collectAsState()
    val currentSec by viewModel.voicemailCurrentSeconds.collectAsState()
    val speed by viewModel.voicemailPlaybackSpeed.collectAsState()
    val haptic = LocalHapticFeedback.current

    var filterUnreadOnly by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Voicemail",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OledSurface, RoundedCornerShape(12.dp))
                .border(1.dp, OledBorderSubtle, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val unreadCount = voicemails.count { it.isUnread }
            FilterPill(
                title = "All Voicemails (${voicemails.size})",
                selected = !filterUnreadOnly,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    filterUnreadOnly = false
                },
                modifier = Modifier.weight(1f)
            )
            FilterPill(
                title = "Unread ($unreadCount)",
                selected = filterUnreadOnly,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    filterUnreadOnly = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val displayedList = if (filterUnreadOnly) voicemails.filter { it.isUnread } else voicemails

        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Voicemail,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No voicemails", color = TextSecondary, fontSize = 14.sp)
                    Text("Recorded audio messages will appear here.", color = TextTertiary, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(displayedList, key = { _, it -> it.id }) { index, item ->
                    val isCurrentlyPlaying = playingId == item.id
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(item.id) {
                        kotlinx.coroutines.delay((index * 35L).coerceAtMost(250L))
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = OledSurface,
                            border = BorderStroke(1.dp, if (item.isUnread) OledBorderHighlight else OledBorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.callerTag,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = OledBlack,
                                                modifier = Modifier
                                                    .background(TextPrimary, RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.callerNumber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }

                                    if (item.isUnread) {
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldSuccessBg, CircleShape)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("New", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Reactive Undulating Waveform Visualizer
                                WaveformVisualizer(isPlaying = isCurrentlyPlaying)

                                Spacer(modifier = Modifier.height(8.dp))

                                // Scrubber Slider & Animated Speed Chip
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val curSecInt = if (isCurrentlyPlaying) currentSec.toInt() else 0
                                    val curM = String.format("%02d", curSecInt / 60)
                                    val curS = String.format("%02d", curSecInt % 60)
                                    val maxM = String.format("%02d", item.durationSeconds / 60)
                                    val maxS = String.format("%02d", item.durationSeconds % 60)

                                    Text(
                                        text = "$curM:$curS / $maxM:$maxS",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                val nextSpeed = when (speed) {
                                                    1.0f -> 1.25f
                                                    1.25f -> 1.5f
                                                    1.5f -> 2.0f
                                                    else -> 1.0f
                                                }
                                                viewModel.voicemailPlaybackSpeed.value = nextSpeed
                                            },
                                        shape = RoundedCornerShape(6.dp),
                                        color = OledSurfaceElevated,
                                        border = BorderStroke(1.dp, OledBorderSubtle)
                                    ) {
                                        AnimatedContent(
                                            targetState = speed,
                                            transitionSpec = {
                                                (slideInVertically { -it / 2 } + fadeIn(tween(140)))
                                                    .togetherWith(slideOutVertically { it / 2 } + fadeOut(tween(90)))
                                            },
                                            label = "SpeedPillTransition"
                                        ) { spd ->
                                            Text(
                                                text = "${spd}x",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Slider(
                                    value = if (isCurrentlyPlaying) currentSec else 0f,
                                    onValueChange = { viewModel.voicemailCurrentSeconds.value = it },
                                    valueRange = 0f..item.durationSeconds.toFloat(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = TextPrimary,
                                        activeTrackColor = TextPrimary,
                                        inactiveTrackColor = OledSurfaceHover
                                    )
                                )

                                // Transcript
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = OledSurfaceElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, OledBorderSubtle)
                                ) {
                                    Text(
                                        text = "Transcript: \"${item.transcript}\"",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(10.dp),
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Card Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TactileButton(
                                        text = if (isCurrentlyPlaying) "Pause" else "Play Audio",
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.toggleVoicemailPlayback(item)
                                        },
                                        isPrimary = true,
                                        modifier = Modifier.weight(1.2f),
                                        leadingIcon = {
                                            Icon(
                                                if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                null,
                                                Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                    TactileButton(
                                        text = "Call Back",
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.startCall(item.callerNumber)
                                        },
                                        modifier = Modifier.weight(1f),
                                        leadingIcon = { Icon(Icons.Default.Call, null, Modifier.size(13.dp)) }
                                    )
                                    TactileButton(
                                        text = "Delete",
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.deleteVoicemail(item.id)
                                        },
                                        isDanger = true,
                                        modifier = Modifier.weight(1f),
                                        leadingIcon = { Icon(Icons.Default.Delete, null, Modifier.size(13.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(9.dp),
        color = if (selected) OledSurfaceElevated else OledSurface,
        border = if (selected) BorderStroke(1.dp, OledBorderSubtle) else null
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) TextPrimary else TextSecondary
            )
        }
    }
}
