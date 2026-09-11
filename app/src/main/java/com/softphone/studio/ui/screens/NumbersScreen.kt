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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
                        text = if (authToken != null) "PhantomLine Warsaw Pool • Active" else "PhantomLine Virtual Fleet",
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
                                    Text("PhantomLine Cloud Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(userEmail ?: "Logged in", fontSize = 11.sp, color = TextSecondary)
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
                                Text("PhantomLine Account", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Sign in or register for live Polish numbers & SMS // Dev: trexhausted", fontSize = 11.sp, color = TextSecondary)
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
                                    "Your PhantomLine pool has no active lines yet. Tap below to reserve a Polish line."
                                else
                                    "Sign in to your account or provision a Polish (+48) number.",
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
                            viewModel.renewNumber(item.id)
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
                    Text("Reserve New Virtual Number", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    onConfirm = { number, tag, carrier ->
                        viewModel.addVirtualNumber(number, tag, carrier)
                        showAddModal = false
                    }
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
    val leaseProgress = (item.daysRemaining.toFloat() / 30f).coerceIn(0f, 1f)
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
            // Header Tag & Active Lease Circular Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(OledSurfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, OledBorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.countryTag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.carrierName,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Precision Circular Lease Countdown Gauge
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
                                color = Color(0x2BFFFFFF),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            // Progress Arc
                            drawArc(
                                color = if (item.daysRemaining <= 3) DangerRed else ActiveGreen,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${item.daysRemaining}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "days left",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phone Number Text (Monospace & prominent)
            Text(
                text = item.number,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions: Copy, SMS, Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, OledBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onSms,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, OledBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = "SMS", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMS", fontSize = 11.sp)
                }

                TactileButton(
                    text = "Call",
                    onClick = onCall,
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AddNumberSheetContent(
    viewModel: SoftphoneViewModel,
    shimmerBrush: Brush,
    onConfirm: (number: String, tag: String, carrier: String) -> Unit
) {
    var selectedTag by remember { mutableStateOf("PL") }
    var carrierName by remember { mutableStateOf("PhantomLine Cloud") }
    var previewNumber by remember { mutableStateOf("+48 732 891 042") }
    val authToken by viewModel.authToken.collectAsState()
    val pendingNumber by viewModel.pendingRandomNumber.collectAsState()
    val isLoading by viewModel.isNumberLoading.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(pendingNumber) {
        pendingNumber?.let {
            previewNumber = it.first
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Get a New Virtual Number",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Select region to provision a PhantomLine Polish or global line:",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val regions = listOf(
                Triple("PL", "+48", "PhantomLine Poland"),
                Triple("UK", "+44", "Vodafone UK"),
                Triple("US", "+1", "T-Mobile US")
            )
            regions.forEach { (tag, code, carrier) ->
                FilterChip(
                    selected = selectedTag == tag,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTag = tag
                        carrierName = carrier
                        if (tag == "PL" && authToken != null) {
                            viewModel.fetchRandomNumber()
                        } else {
                            previewNumber = "$code 7${(100..999).random()} ${(100..999).random()}"
                        }
                    },
                    label = { Text("[$tag] $carrier") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = OledSurfaceElevated,
                        labelColor = TextSecondary,
                        selectedContainerColor = TextPrimary,
                        selectedLabelColor = OledBlack
                    )
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = OledBlack,
            border = BorderStroke(1.dp, OledBorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Allocated Line Preview", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(28.dp)
                            .background(shimmerBrush, RoundedCornerShape(6.dp))
                    )
                } else {
                    Text(
                        text = previewNumber,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Direct Provisioned • Immediate Activation",
                    fontSize = 11.sp,
                    color = ActiveGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        TactileButton(
            text = "Reserve & Activate Line",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (authToken != null && pendingNumber != null) {
                    viewModel.reservePendingNumber("PhantomLine Line") {
                        onConfirm(previewNumber, selectedTag, carrierName)
                    }
                } else {
                    onConfirm(previewNumber, selectedTag, carrierName)
                }
            },
            isPrimary = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
