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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.model.PhoneNumberItem
import com.softphone.studio.theme.*
import com.softphone.studio.ui.components.TactileButton
import com.softphone.studio.viewmodel.SoftphoneViewModel

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
    val clipboardManager = LocalClipboardManager.current
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
                        text = if (authToken != null) "PhantomLine Cloud Connected" else "PhantomLine Virtual Lines",
                        fontSize = 11.sp,
                        color = if (authToken != null) ActiveGreen else TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (authToken != null) {
                        IconButton(
                            onClick = { viewModel.fetchUserNumbers() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(OledSurface, CircleShape)
                                .border(1.dp, OledBorderSubtle, CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Numbers", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    IconButton(
                        onClick = { showAddModal = true },
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
            // 2NR Cloud Status / Login Banner
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
                            TextButton(onClick = { viewModel.logout2nr() }) {
                                Text("Log Out", fontSize = 11.sp, color = DangerRed)
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAuth() },
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
                                Text("Tap to sign in or register for live Polish numbers & SMS // Dev: trexhausted", fontSize = 11.sp, color = TextSecondary)
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

            if (numbers.isEmpty()) {
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
                        onCopy = { clipboardManager.setText(AnnotatedString(item.number)) },
                        onSms = { onNavigateToChat(item.number) },
                        onCall = { viewModel.startCall(item.number) },
                        onRenew = { viewModel.renewNumber(item.id) }
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { showAddModal = true },
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = OledSurface,
        border = BorderStroke(1.dp, OledBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Tag & Active badge
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(ActiveGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.daysRemaining} days left",
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

            // Quick Actions: Copy, SMS, Call, Renew
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
    onConfirm: (number: String, tag: String, carrier: String) -> Unit
) {
    var selectedTag by remember { mutableStateOf("PL") }
    var carrierName by remember { mutableStateOf("PhantomLine Cloud") }
    var previewNumber by remember { mutableStateOf("+48 732 891 042") }
    val authToken by viewModel.authToken.collectAsState()
    val pendingNumber by viewModel.pendingRandomNumber.collectAsState()
    val isLoading by viewModel.isNumberLoading.collectAsState()

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
                Spacer(modifier = Modifier.height(6.dp))
                if (isLoading) {
                    CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = previewNumber,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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
