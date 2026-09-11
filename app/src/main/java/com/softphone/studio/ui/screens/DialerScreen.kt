package com.softphone.studio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.theme.*
import com.softphone.studio.viewmodel.SoftphoneViewModel

@Composable
fun DialerScreen(viewModel: SoftphoneViewModel) {
    val digits by viewModel.dialerInput.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = digits,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                maxLines = 1
            )

            if (digits.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.backspaceDialer() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Keypad Grid
        val keys = listOf(
            listOf(Pair("1", ""), Pair("2", "ABC"), Pair("3", "DEF")),
            listOf(Pair("4", "GHI"), Pair("5", "JKL"), Pair("6", "MNO")),
            listOf(Pair("7", "PQRS"), Pair("8", "TUV"), Pair("9", "WXYZ")),
            listOf(Pair("*", ""), Pair("0", "+"), Pair("#", ""))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for ((num, letters) in row) {
                        DialerKey(
                            number = num,
                            letters = letters,
                            onClick = { viewModel.pressDialerKey(num) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.clearDialer() },
                modifier = Modifier
                    .size(44.dp)
                    .background(OledSurfaceElevated, CircleShape)
                    .border(1.dp, OledBorderSubtle, CircleShape)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextSecondary)
            }

            // Green Call Button
            Button(
                onClick = { viewModel.startCall() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldSuccess,
                    contentColor = OledBlack
                ),
                modifier = Modifier.size(68.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(30.dp))
            }

            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
fun DialerKey(
    number: String,
    letters: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        modifier = modifier
            .height(68.dp)
            .graphicsLayer {
                scaleX = if (isPressed) 0.92f else 1f
                scaleY = if (isPressed) 0.92f else 1f
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (isPressed) OledSurfaceElevated else OledSurface,
        border = BorderStroke(1.dp, if (isPressed) OledBorderHighlight else OledBorderSubtle)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 24.sp
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
