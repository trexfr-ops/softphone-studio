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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.softphone.studio.model.MessageThread
import com.softphone.studio.theme.*
import com.softphone.studio.ui.components.TactileButton
import com.softphone.studio.viewmodel.SoftphoneViewModel

@Composable
fun UnreadRadarBeacon() {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarBeacon")
    val beaconScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BeaconScale"
    )
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BeaconAlpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(16.dp)) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .graphicsLayer {
                    scaleX = beaconScale
                    scaleY = beaconScale
                    alpha = beaconAlpha
                }
                .background(ActiveGreen, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(ActiveGreen, CircleShape)
        )
    }
}

@Composable
fun MessagesScreen(
    viewModel: SoftphoneViewModel,
    onOpenChat: (String) -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val authToken by viewModel.authToken.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    var selectedThreadForDetail by remember { mutableStateOf<MessageThread?>(null) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }
    var recentlyCopiedOtp by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(copiedNotice) {
        if (copiedNotice != null) {
            kotlinx.coroutines.delay(2200)
            copiedNotice = null
        }
    }

    LaunchedEffect(recentlyCopiedOtp) {
        if (recentlyCopiedOtp != null) {
            kotlinx.coroutines.delay(2200)
            recentlyCopiedOtp = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Messages",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = if (authToken != null) "PhantomLine SMS Gateway • Encrypted" else "PhantomLine Inbox",
                    fontSize = 11.sp,
                    color = if (authToken != null) ActiveGreen else TextSecondary
                )
            }

            if (authToken != null) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.fetchSms()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(OledSurface, CircleShape)
                        .border(1.dp, OledBorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh SMS",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Copied Banner Floating Pill
        AnimatedVisibility(
            visible = copiedNotice != null,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 2 }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(10.dp),
                color = OledSurfaceElevated,
                border = BorderStroke(1.dp, ActiveGreen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ActiveGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = copiedNotice ?: "",
                        color = ActiveGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // PhantomLine Cloud status card if not logged in
        if (authToken == null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = OledSurface,
                border = BorderStroke(1.dp, OledBorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(OledSurfaceElevated, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PhantomLine Cloud Standby",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sign in to receive encrypted Polish SMS in this inbox.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No SMS messages yet", color = TextSecondary, fontSize = 14.sp)
                    Text("Incoming texts will appear here automatically.", color = TextTertiary, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(messages, key = { _, thread -> thread.id }) { index, thread ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(thread.id) {
                        kotlinx.coroutines.delay((index * 35L).coerceAtMost(250L))
                        isVisible = true
                    }

                    val otpMatch = remember(thread.lastMessage) {
                        Regex("""\b\d{4,8}\b""").find(thread.lastMessage)?.value
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedThreadForDetail = thread
                                    onOpenChat(thread.phoneNumber)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = OledSurface,
                            border = BorderStroke(1.dp, if (thread.isUnread) OledBorderHighlight else OledBorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(OledSurfaceElevated, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = if (thread.isUnread) ActiveGreen else TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = thread.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (thread.isUnread) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                UnreadRadarBeacon()
                                            }
                                        }
                                        Text(
                                            text = thread.timestamp,
                                            fontSize = 11.sp,
                                            color = TextTertiary
                                        )
                                    }
                                    if (thread.recipientLine.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = thread.recipientLine,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ActiveGreen.copy(alpha = 0.85f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = thread.lastMessage,
                                        fontSize = 12.sp,
                                        color = if (thread.isUnread) TextPrimary else TextSecondary,
                                        fontWeight = if (thread.isUnread) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 2
                                    )

                                    // One-Tap OTP Quick Action Chip
                                    if (otpMatch != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val isThisCopied = recentlyCopiedOtp == otpMatch
                                        Surface(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                clipboardManager.setText(AnnotatedString(otpMatch))
                                                recentlyCopiedOtp = otpMatch
                                                copiedNotice = "OTP Code $otpMatch copied to clipboard"
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isThisCopied) ActiveGreen.copy(alpha = 0.22f) else OledSurfaceElevated,
                                            border = BorderStroke(1.dp, if (isThisCopied) ActiveGreen else ActiveGreen.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isThisCopied) Icons.Default.Check else Icons.Default.VpnKey,
                                                    contentDescription = null,
                                                    tint = ActiveGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isThisCopied) "COPIED: $otpMatch" else "OTP: $otpMatch  •  TAP TO COPY",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (isThisCopied) ActiveGreen else TextPrimary
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
        }
    }

    // SMS Detail Dialog with OTP Extraction & Copying
    selectedThreadForDetail?.let { thread ->
        val otpMatch = remember(thread.lastMessage) {
            val digitsRegex = Regex("""\b\d{4,8}\b""")
            digitsRegex.find(thread.lastMessage)?.value
        }

        Dialog(onDismissRequest = { selectedThreadForDetail = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(18.dp),
                color = OledSurface,
                border = BorderStroke(1.dp, OledBorderMedium)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = thread.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (thread.recipientLine.isNotBlank())
                                    "${thread.phoneNumber} • ${thread.recipientLine} • ${thread.timestamp}"
                                else
                                    "${thread.phoneNumber} • ${thread.timestamp}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedThreadForDetail = null
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Message Content Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = OledBlack,
                        border = BorderStroke(1.dp, OledBorderSubtle)
                    ) {
                        Text(
                            text = thread.lastMessage,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    // Extracted OTP Code Section if present
                    if (otpMatch != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = OledSurfaceElevated,
                            border = BorderStroke(1.dp, ActiveGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = null,
                                        tint = ActiveGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Detected Code", fontSize = 10.sp, color = TextSecondary)
                                        Text(
                                            text = otpMatch,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        clipboardManager.setText(AnnotatedString(otpMatch))
                                        copiedNotice = "Code copied: $otpMatch"
                                        selectedThreadForDetail = null
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, ActiveGreen),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ActiveGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Copy Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                clipboardManager.setText(AnnotatedString(thread.lastMessage))
                                copiedNotice = "Message copied to clipboard"
                                selectedThreadForDetail = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, OledBorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Full Text", fontSize = 11.sp)
                        }

                        TactileButton(
                            text = "Done",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedThreadForDetail = null
                            },
                            isPrimary = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
