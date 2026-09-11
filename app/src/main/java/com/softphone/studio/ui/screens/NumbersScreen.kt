package com.softphone.studio.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onNavigateToChat: (String) -> Unit
) {
    val numbers by viewModel.numbers.collectAsState()
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
                Text(
                    text = "My Numbers",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = { showAddModal = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(OledSurfaceElevated, CircleShape)
                        .border(1.dp, OledBorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Number",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(numbers, key = { it.id }) { item ->
                NumberCard(
                    item = item,
                    onCopy = { /* Copy to clipboard */ },
                    onSms = { onNavigateToChat(item.number) },
                    onCall = { viewModel.startCall(item.number) },
                    onRenew = { viewModel.renewNumber(item.id) }
                )
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
                        color = OledBlack,
                        modifier = Modifier
                            .background(TextPrimary, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.carrierName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(EmeraldSuccessBg, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(EmeraldSuccess, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Active",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Big Phone Number
            Text(
                text = item.number,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Validity Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Expires in ${item.daysRemaining} days",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${(item.progressPercentage * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { item.progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = TextPrimary,
                trackColor = OledSurfaceElevated
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TactileButton(
                    text = "Copy",
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null, Modifier.size(13.dp)) }
                )
                TactileButton(
                    text = if (item.daysRemaining <= 15) "Renew" else "SMS",
                    onClick = if (item.daysRemaining <= 15) onRenew else onSms,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            if (item.daysRemaining <= 15) Icons.Default.Refresh else Icons.Default.Email,
                            null,
                            Modifier.size(13.dp)
                        )
                    }
                )
                TactileButton(
                    text = "Call",
                    onClick = onCall,
                    isPrimary = true,
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Call, null, Modifier.size(13.dp)) }
                )
            }
        }
    }
}

@Composable
fun AddNumberSheetContent(
    onConfirm: (String, String, String) -> Unit
) {
    var selectedTag by remember { mutableStateOf("PL") }
    var previewNumber by remember { mutableStateOf("+48 732 998 104") }
    var carrierName by remember { mutableStateOf("Play Poland") }

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
            text = "Select region to provision an encrypted virtual SIP line:",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val regions = listOf(
                Triple("PL", "+48", "Play Poland"),
                Triple("UK", "+44", "Vodafone UK"),
                Triple("US", "+1", "T-Mobile US")
            )
            regions.forEach { (tag, code, carrier) ->
                FilterChip(
                    selected = selectedTag == tag,
                    onClick = {
                        selectedTag = tag
                        carrierName = carrier
                        previewNumber = "$code 7${(100..999).random()} ${(100..999).random()}"
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
                Text(
                    text = previewNumber,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Direct SIP Provisioned • Immediate Activation",
                    fontSize = 11.sp,
                    color = EmeraldSuccess,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        TactileButton(
            text = "Reserve & Activate Line",
            onClick = { onConfirm(previewNumber, selectedTag, carrierName) },
            isPrimary = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
