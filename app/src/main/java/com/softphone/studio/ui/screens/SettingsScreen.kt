package com.softphone.studio.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.model.SipTransport
import com.softphone.studio.theme.*
import com.softphone.studio.ui.components.OledSlider
import com.softphone.studio.ui.components.TactileButton
import com.softphone.studio.viewmodel.SoftphoneViewModel

@Composable
fun SettingsScreen(
    viewModel: SoftphoneViewModel,
    onNavigateToAuth: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Engine & Audio Settings",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        // CARD 1: AUDIO DSP SLIDERS
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = OledSurface,
            border = BorderStroke(1.dp, OledBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Audio Levels & DSP Sliders",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                OledSlider(
                    label = "Speaker / Call Volume",
                    value = settings.speakerVolumePercent.toFloat(),
                    onValueChange = { viewModel.settingsState.value = settings.copy(speakerVolumePercent = it.toInt()) },
                    valueRange = 0f..100f,
                    badgeText = "${settings.speakerVolumePercent}%"
                )

                OledSlider(
                    label = "Microphone Gain",
                    value = settings.microphoneGainDb.toFloat(),
                    onValueChange = { viewModel.settingsState.value = settings.copy(microphoneGainDb = it.toInt()) },
                    valueRange = -12f..18f,
                    badgeText = "${if (settings.microphoneGainDb >= 0) "+" else ""}${settings.microphoneGainDb} dB"
                )

                OledSlider(
                    label = "SIP Jitter Buffer Latency",
                    value = settings.jitterBufferMs.toFloat(),
                    onValueChange = { viewModel.settingsState.value = settings.copy(jitterBufferMs = it.toInt()) },
                    valueRange = 20f..160f,
                    badgeText = "${settings.jitterBufferMs} ms"
                )

                OledSlider(
                    label = "Ringtone & Alerts",
                    value = settings.ringtoneVolumePercent.toFloat(),
                    onValueChange = { viewModel.settingsState.value = settings.copy(ringtoneVolumePercent = it.toInt()) },
                    valueRange = 0f..100f,
                    badgeText = "${settings.ringtoneVolumePercent}%"
                )
            }
        }

        // CARD 2: SIP PROTOCOL & CODECS
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = OledSurface,
            border = BorderStroke(1.dp, OledBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SIP Transport Protocol",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OledSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SipTransport.values().forEach { transport ->
                        val isSelected = settings.sipTransport == transport
                        FilterPill(
                            title = if (transport == SipTransport.TLS) "TLS (Secure)" else transport.name,
                            selected = isSelected,
                            onClick = { viewModel.settingsState.value = settings.copy(sipTransport = transport) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ToggleRow(
                    title = "Opus HD Codec Priority",
                    subtitle = "48kHz high-fidelity adaptive audio",
                    checked = settings.isOpusHdEnabled,
                    onCheckedChange = { viewModel.settingsState.value = settings.copy(isOpusHdEnabled = it) }
                )
                ToggleRow(
                    title = "Acoustic Echo Cancellation",
                    subtitle = "Zero feedback on speakerphone",
                    checked = settings.isEchoCancellationEnabled,
                    onCheckedChange = { viewModel.settingsState.value = settings.copy(isEchoCancellationEnabled = it) }
                )
                ToggleRow(
                    title = "AI Background Noise Filter",
                    subtitle = "Suppresses traffic and ambient noise",
                    checked = settings.isAiNoiseFilterEnabled,
                    onCheckedChange = { viewModel.settingsState.value = settings.copy(isAiNoiseFilterEnabled = it) }
                )
            }
        }

        // CARD 3: FEEDBACK & CREDENTIALS
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = OledSurface,
            border = BorderStroke(1.dp, OledBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Feedback & Interactions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                ToggleRow(
                    title = "DTMF Keypad Synthesizer",
                    subtitle = "Web Audio multi-frequency tones",
                    checked = settings.isDtmfAudioEnabled,
                    onCheckedChange = { viewModel.settingsState.value = settings.copy(isDtmfAudioEnabled = it) }
                )
                ToggleRow(
                    title = "Tactile Vibration Haptics",
                    subtitle = "Micro-vibrations on dialpad keys",
                    checked = settings.isVibrationHapticsEnabled,
                    onCheckedChange = { viewModel.settingsState.value = settings.copy(isVibrationHapticsEnabled = it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                TactileButton(
                    text = "Manage Account & Cloud Credentials",
                    onClick = onNavigateToAuth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // CARD 4: DEVELOPER & SERVICE INFO
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = OledSurface,
            border = BorderStroke(1.dp, OledBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "About PhantomLine",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Service", fontSize = 12.sp, color = TextSecondary)
                    Text("PhantomLine Virtual Telecom", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Developer", fontSize = 12.sp, color = TextSecondary)
                    Text("trexhausted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Discord", fontSize = 12.sp, color = TextSecondary)
                    Text("trexhausted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ActiveGreen)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edition", fontSize = 12.sp, color = TextSecondary)
                    Text("Mono Luxury OLED Black", fontSize = 12.sp, color = TextTertiary)
                }
            }
        }
    }
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OledBlack,
                checkedTrackColor = TextPrimary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = OledSurfaceElevated
            )
        )
    }
}
