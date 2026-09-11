package com.softphone.studio.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                onClick = { filterUnreadOnly = false },
                modifier = Modifier.weight(1f)
            )
            FilterPill(
                title = "Unread ($unreadCount)",
                selected = filterUnreadOnly,
                onClick = { filterUnreadOnly = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val displayedList = if (filterUnreadOnly) voicemails.filter { it.isUnread } else voicemails

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedList, key = { it.id }) { item ->
                val isCurrentlyPlaying = playingId == item.id

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

                        // Animated Waveform Visualizer
                        WaveformVisualizer(isPlaying = isCurrentlyPlaying)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Scrubber Slider
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

                            Box(
                                modifier = Modifier
                                    .background(OledSurfaceElevated, RoundedCornerShape(6.dp))
                                    .border(1.dp, OledBorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val nextSpeed = when (speed) {
                                            1.0f -> 1.25f
                                            1.25f -> 1.5f
                                            1.5f -> 2.0f
                                            else -> 1.0f
                                        }
                                        viewModel.voicemailPlaybackSpeed.value = nextSpeed
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${speed}x",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
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
                                onClick = { viewModel.toggleVoicemailPlayback(item) },
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
                                onClick = { viewModel.startCall(item.callerNumber) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.Call, null, Modifier.size(13.dp)) }
                            )
                            TactileButton(
                                text = "Delete",
                                onClick = { viewModel.deleteVoicemail(item.id) },
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
