package com.softphone.studio.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.softphone.studio.viewmodel.SoftphoneViewModel

@Composable
fun AuthScreen(
    viewModel: SoftphoneViewModel,
    onLoginSuccess: () -> Unit,
    onClose: () -> Unit = onLoginSuccess
) {
    var authMode by remember { mutableStateOf("signin") }
    var email by remember { mutableStateOf("rolledrick581@gmail.com") }
    var password by remember { mutableStateOf("scorp1on1sop@A") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()
    val currentToken by viewModel.authToken.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar with Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lock Icon Header
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(OledSurface, RoundedCornerShape(20.dp))
                .border(1.dp, OledBorderSubtle, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "PhantomLine Account",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Polish Virtual Telecom Gateway // Dev: trexhausted",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mode Selector: Sign In / Register / Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OledSurface, RoundedCornerShape(12.dp))
                .border(1.dp, OledBorderSubtle, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterPill(title = "Sign In", selected = authMode == "signin", onClick = { authMode = "signin" }, modifier = Modifier.weight(1f))
            FilterPill(title = "Register", selected = authMode == "register", onClick = { authMode = "register" }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error message banner
        androidx.compose.animation.AnimatedVisibility(
            visible = authError != null,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = OledSurface,
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
            ) {
                Text(
                    text = authError ?: "",
                    color = DangerRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Success message banner
        androidx.compose.animation.AnimatedVisibility(
            visible = authSuccess != null,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = OledSurface,
                border = BorderStroke(1.dp, ActiveGreen.copy(alpha = 0.5f))
            ) {
                Text(
                    text = authSuccess ?: "",
                    color = ActiveGreen,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            placeholder = { Text("e.g. user@example.com") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OledBorderHighlight,
                unfocusedBorderColor = OledBorderSubtle,
                focusedLabelColor = TextPrimary,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = OledSurface,
                unfocusedContainerColor = OledSurface
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Min. 6 chars (Upper, Lower, Number)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = TextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OledBorderHighlight,
                unfocusedBorderColor = OledBorderSubtle,
                focusedLabelColor = TextPrimary,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = OledSurface,
                unfocusedContainerColor = OledSurface
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button with Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                color = TextPrimary,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
        } else {
            TactileButton(
                text = if (authMode == "signin") "Sign In to PhantomLine" else "Create PhantomLine Account",
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        if (authMode == "signin") {
                            viewModel.login2nr(email, password, onSuccess = onLoginSuccess)
                        } else {
                            viewModel.register2nr(email, password, onSuccess = {
                                authMode = "signin"
                            })
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skip / Continue Offline
        TextButton(onClick = onClose) {
            Text(
                text = "Continue Without Logging In",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Notice
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = OledSurfaceElevated,
            border = BorderStroke(1.dp, OledBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("PhantomLine Telecom Gateway", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Encrypted cloud transport directly linked to Polish virtual mobile pools. Developed by trexhausted (Discord).",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

