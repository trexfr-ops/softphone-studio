package com.softphone.studio.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.theme.*
import com.softphone.studio.ui.components.TactileButton

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var authMode by remember { mutableStateOf("signin") }
    var emailOrId by remember { mutableStateOf("developer@sip.local") }
    var password by remember { mutableStateOf("SuperSecure123!") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberDevice by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Lock Icon Header
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(OledSurface, RoundedCornerShape(18.dp))
                .border(1.dp, OledBorderSubtle, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Softphone Account",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Direct authentication & SIP credentials",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mode Selector: Sign In / Register / SIP Direct
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OledSurface, RoundedCornerShape(12.dp))
                .border(1.dp, OledBorderSubtle, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterPill("Sign In", selected = authMode == "signin", onClick = { authMode = "signin" }, modifier = Modifier.weight(1f))
            FilterPill("Register", selected = authMode == "register", onClick = { authMode = "register" }, modifier = Modifier.weight(1f))
            FilterPill("SIP Direct", selected = authMode == "sip", onClick = { authMode = "sip" }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (authMode) {
            "signin" -> {
                OutlinedTextField(
                    value = emailOrId,
                    onValueChange = { emailOrId = it },
                    label = { Text("Email or Softphone ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OledSurfaceElevated,
                        unfocusedContainerColor = OledSurface,
                        focusedBorderColor = OledBorderHighlight,
                        unfocusedBorderColor = OledBorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OledSurfaceElevated,
                        unfocusedContainerColor = OledSurface,
                        focusedBorderColor = OledBorderHighlight,
                        unfocusedBorderColor = OledBorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remember this device", fontSize = 12.sp, color = TextSecondary)
                    Switch(
                        checked = rememberDevice,
                        onCheckedChange = { rememberDevice = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OledBlack,
                            checkedTrackColor = TextPrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = OledSurfaceElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                TactileButton(
                    text = "Sign In to Softphone",
                    onClick = onLoginSuccess,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                TactileButton(
                    text = "Sign in with One-Time Security Code",
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "register" -> {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OledSurfaceElevated,
                        unfocusedContainerColor = OledSurface,
                        focusedBorderColor = OledBorderHighlight,
                        unfocusedBorderColor = OledBorderSubtle
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Create Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OledSurfaceElevated,
                        unfocusedContainerColor = OledSurface,
                        focusedBorderColor = OledBorderHighlight,
                        unfocusedBorderColor = OledBorderSubtle
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                TactileButton(
                    text = "Create Softphone Account",
                    onClick = onLoginSuccess,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "sip" -> {
                OutlinedTextField(
                    value = "sip.softphone.network:5061",
                    onValueChange = {},
                    label = { Text("SIP Registrar Domain") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = "100249",
                    onValueChange = {},
                    label = { Text("SIP Extension / User ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = "c8f2a1048b6d",
                    onValueChange = {},
                    label = { Text("SIP Auth Secret") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                TactileButton(
                    text = "Save & Connect SIP Trunk",
                    onClick = onLoginSuccess,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Protected by End-to-End SRTP Voice Encryption.\nNo third-party trackers or external login services.",
            fontSize = 11.sp,
            color = TextTertiary,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
