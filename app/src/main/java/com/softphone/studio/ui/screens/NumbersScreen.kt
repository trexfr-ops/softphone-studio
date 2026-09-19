package com.softphone.studio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.model.PhoneNumberItem
import com.softphone.studio.theme.*
import com.softphone.studio.ui.components.TactileButton
import com.softphone.studio.viewmodel.SoftphoneViewModel

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF0C0C0C),
            Color(0xFF222222),
            Color(0xFF0C0C0C)
        ),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 320f, translateAnim + 320f)
    )
}

@Composable
fun ShimmerNumberSkeletonCard(brush: Brush) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = OledSurface,
        border = BorderStroke(1.dp, OledBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 86.dp, height = 22.dp)
                        .background(brush, RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .size(width = 70.dp, height = 20.dp)
                        .background(brush, RoundedCornerShape(6.dp))
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(28.dp)
                    .background(brush, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(brush, RoundedCornerShape(10.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(brush, RoundedCornerShape(10.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(brush, RoundedCornerShape(10.dp))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumbersScreen(
    viewModel: SoftphoneViewModel,
    onNavigateToChat: (String) -> Unit,
    onOpenAuth: () -> Unit = {}
) {
    val numbers by viewModel.numbers.collectAsState()
    val authToken by viewModel.authToken.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val isNumberLoading by viewModel.isNumberLoading.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val shimmerBrush = rememberShimmerBrush()
    var showAddModal by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Numbers",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (authToken != null) "PhantomLine Warsaw Pool • Active" else "Authentication Required",
                        fontSize = 11.sp,
                        color = if (authToken != null) ActiveGreen else TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (authToken != null) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.fetchUserNumbers()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(OledSurface, CircleShape)
                                .border(1.dp, OledBorderSubtle, CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Numbers", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showAddModal = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(OledSurfaceElevated, CircleShape)
                            .border(1.dp, OledBorderSubtle, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Number", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // PhantomLine Cloud Status / Login Banner
            item {
                if (authToken != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = OledSurface,
                        border = BorderStroke(1.dp, ActiveGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(ActiveGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("PhantomLine Warsaw Cloud Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(userEmail ?: "Authenticated", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            TextButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.logout2nr()
                            }) {
                                Text("Log Out", fontSize = 11.sp, color = DangerRed)
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenAuth()
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = OledSurface,
                        border = BorderStroke(1.dp, OledBorderMedium)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(OledSurfaceElevated, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("PhantomLine Account Required", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Sign in to access and manage live Polish virtual numbers & SMS", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TactileButton(
                                text = "Sign In",
                                onClick = onOpenAuth,
                                isPrimary = true
                            )
                        }
                    }
                }
            }

            // Snackbar feedback banner if renewal was triggered
            snackbarMessage?.let { msg ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = OledSurfaceElevated,
                        border = BorderStroke(1.dp, OledBorderMedium)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(msg, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            TextButton(onClick = { snackbarMessage = null }) {
                                Text("Dismiss", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Shimmer Loading Skeleton when querying fleet
            if (isNumberLoading) {
                item {
                    ShimmerNumberSkeletonCard(brush = shimmerBrush)
                }
            }

            if (numbers.isEmpty() && !isNumberLoading) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = OledSurface,
                        border = BorderStroke(1.dp, OledBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(OledSurfaceElevated, CircleShape)
                                    .border(1.dp, OledBorderSubtle, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Virtual Numbers Allocated",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (authToken != null)
                                    "Your PhantomLine Warsaw pool has no active lines yet. Tap below to reserve a Polish line."
                                else
                                    "Sign into your PhantomLine account to load your active lines.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(numbers, key = { it.id }) { item ->
                    NumberCard(
                        item = item,
                        onCopy = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            clipboardManager.setText(AnnotatedString(item.number))
                            snackbarMessage = "Copied ${item.number} to clipboard"
                        },
                        onSms = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToChat(item.number)
                        },
                        onCall = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.startCall(item.number)
                        },
                        onRenew = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.renewNumber(item.id) { success, msg ->
                                snackbarMessage = msg
                            }
                        }
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showAddModal = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, OledBorderMedium),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = OledSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Provision New Virtual Number", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (showAddModal) {
            ModalBottomSheet(
                onDismissRequest = { showAddModal = false },
                containerColor = OledSurface,
                scrimColor = OledBlack.copy(alpha = 0.7f)
            ) {
                AddNumberSheetContent(
                    viewModel = viewModel,
                    shimmerBrush = shimmerBrush,
                    onOpenAuth = {
                        showAddModal = false
                        onOpenAuth()
                    },
                    onDismiss = { showAddModal = false }
                )
            }
        }
    }
}

@Composable
fun NumberCard(
    item: PhoneNumberItem,
    onCopy: () -> Unit,
    onSms: () -> Unit,
    onCall: () -> Unit,
    onRenew: () -> Unit
) {
    val leaseProgress = item.progressPercentage
    val animatedProgress by animateFloatAsState(
        targetValue = leaseProgress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "LeaseGaugeProgress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = OledSurface,
        border = BorderStroke(1.dp, OledBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.number,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (item.expirationDateStr.isNotBlank())
                            "${item.carrierName} • Expires: ${item.expirationDateStr}"
                        else
                            "${item.carrierName} • ${item.daysRemaining}d validity",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Precision Circular Lease Countdown Gauge with Renew trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onRenew)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val strokeWidth = 2.5.dp.toPx()
                            // Track
                            drawArc(
                                color = OledBorderSubtle,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            // Indicator
                            drawArc(
                                color = if (item.daysRemaining <= 1) DangerRed else ActiveGreen,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.daysRemaining}d",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.daysRemaining <= 1) DangerRed else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action row: Copy, SMS, Call, Extend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, OledBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = OledBlack, contentColor = TextSecondary),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onSms,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, OledBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = OledBlack, contentColor = TextSecondary),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = "SMS", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMS", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, OledBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = OledBlack, contentColor = TextSecondary),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp)
                }

                Button(
                    onClick = onRenew,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OledSurfaceElevated, contentColor = TextPrimary),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Extend", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Extend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddNumberSheetContent(
    viewModel: SoftphoneViewModel,
    shimmerBrush: Brush,
    onOpenAuth: () -> Unit,
    onDismiss: () -> Unit
) {
    val authToken by viewModel.authToken.collectAsState()
    val pendingNumber by viewModel.pendingRandomNumber.collectAsState()
    val isLoading by viewModel.isNumberLoading.collectAsState()
    val haptic = LocalHapticFeedback.current
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(authToken) {
        if (authToken != null && pendingNumber == null) {
            viewModel.fetchRandomNumber()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Provision Virtual Polish Line",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (authToken == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = OledBlack,
                border = BorderStroke(1.dp, OledBorderMedium)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                    Text(
                        text = "Authentication Required",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "To reserve and activate genuine Polish (+48) carrier mobile lines with SMS reception, please sign in or register your PhantomLine account.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TactileButton(
                        text = "Sign In to PhantomLine",
                        onClick = onOpenAuth,
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Text(
                text = "Live Polish (+48) Warsaw mobile gateway pool:",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = OledBlack,
                border = BorderStroke(1.dp, OledBorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("WARSAW POOL ALLOCATION PREVIEW", fontSize = 11.sp, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isLoading || pendingNumber == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(32.dp)
                                .background(shimmerBrush, RoundedCornerShape(6.dp))
                        )
                    } else {
                        Text(
                            text = pendingNumber?.first ?: "",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Physical SIM / Carrier Switch Bounded",
                        fontSize = 11.sp,
                        color = ActiveGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            statusMessage?.let { msg ->
                Text(
                    text = msg,
                    fontSize = 12.sp,
                    color = if (isSuccess) ActiveGreen else DangerRed,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.fetchRandomNumber()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OledBorderMedium),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = OledSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Reroll Line", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                TactileButton(
                    text = if (isLoading) "Provisioning..." else "Reserve Line",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.reservePendingNumber("PhantomLine Line") { success, msg ->
                            isSuccess = success
                            statusMessage = msg
                            if (success) {
                                onDismiss()
                            }
                        }
                    },
                    isPrimary = true,
                    enabled = !isLoading && pendingNumber != null,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}
