package com.softphone.studio.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.model.MessageThread
import com.softphone.studio.theme.*

@Composable
fun MessagesScreen(
    onOpenChat: (String) -> Unit
) {
    val sampleThreads = listOf(
        MessageThread("1", "SIP Verification Gateway", "+48 22 990 011", "Your softphone one-time token is: 849-210.", "10:14", true),
        MessageThread("2", "Play Mobile Network", "+48 732 458 912", "Virtual SIP trunk routing established via Warsaw datacenter.", "09:02", false),
        MessageThread("3", "Alexander Wright", "+44 770 982 110", "Audio quality on Opus codec is crystal clear!", "Yesterday", false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Messages",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sampleThreads, key = { it.id }) { thread ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChat(thread.phoneNumber) },
                    shape = RoundedCornerShape(14.dp),
                    color = OledSurface,
                    border = BorderStroke(1.dp, OledBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(OledSurfaceElevated, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(thread.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(thread.timestamp, fontSize = 11.sp, color = TextTertiary)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = thread.lastMessage,
                                fontSize = 12.sp,
                                color = if (thread.isUnread) TextPrimary else TextSecondary,
                                fontWeight = if (thread.isUnread) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
