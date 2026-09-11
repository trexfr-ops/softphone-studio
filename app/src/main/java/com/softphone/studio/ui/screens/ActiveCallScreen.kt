package com.softphone.studio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

    val mins = String.format("%02d", elapsed / 60)
    val secs = String.format("%02d", elapsed % 60)

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

            // Pulse Avatar
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
                text = "SIP TLS Call Active",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldSuccess
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$mins:$secs",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            WaveformVisualizer(
                isPlaying = true,
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
                    onClick = { viewModel.isCallMuted.value = !isMuted }
                )
                CallCircleControl(
                    icon = Icons.Default.VolumeUp,
                    label = "Speaker",
                    isActive = isSpeaker,
                    onClick = { viewModel.isSpeakerOn.value = !isSpeaker }
                )
                CallCircleControl(
                    icon = Icons.Default.Pause,
                    label = "Hold",
                    isActive = isHold,
                    onClick = { viewModel.isCallOnHold.value = !isHold }
                )
            }

            // End Call Button
            Button(
                onClick = { viewModel.endCall() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed,
                    contentColor = TextPrimary
                ),
                modifier = Modifier.size(72.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    modifier = Modifier.size(32.dp)
                )
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(if (isActive) TextPrimary else OledSurfaceElevated, CircleShape)
                .border(1.dp, OledBorderSubtle, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) OledBlack else TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
    }
}
