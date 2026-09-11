package com.softphone.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softphone.studio.theme.*
import com.softphone.studio.ui.screens.*
import com.softphone.studio.viewmodel.SoftphoneViewModel

enum class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    NUMBERS("numbers", "Numbers", Icons.Default.ViewAgenda),
    KEYPAD("keypad", "Keypad", Icons.Default.Dialpad),
    MESSAGES("messages", "Messages", Icons.Default.Chat),
    VOICEMAIL("voicemail", "Voicemail", Icons.Default.Voicemail),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val viewModel: SoftphoneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SoftphoneStudioTheme {
                AppRoot(viewModel)
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: SoftphoneViewModel) {
    var isSplashVisible by remember { mutableStateOf(true) }
    var hasSkippedInitialAuth by remember { mutableStateOf(false) }
    val authToken by viewModel.authToken.collectAsState()

    Crossfade(
        targetState = isSplashVisible,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "MonoLuxurySplashTransition"
    ) { showSplash ->
        if (showSplash) {
            MonoLuxurySplashScreen(onFinish = { isSplashVisible = false })
        } else {
            val needsInitialAuth = (authToken == null && !hasSkippedInitialAuth)
            AnimatedContent(
                targetState = needsInitialAuth,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.98f, animationSpec = tween(300, easing = FastOutSlowInEasing)))
                        .togetherWith(fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing)))
                },
                label = "InitialAuthTransition"
            ) { showAuth ->
                if (showAuth) {
                    AuthScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { hasSkippedInitialAuth = true },
                        onClose = { hasSkippedInitialAuth = true }
                    )
                } else {
                    MainRoot(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainRoot(viewModel: SoftphoneViewModel) {
    var currentTab by remember { mutableStateOf(NavigationItem.NUMBERS) }
    var isAuthScreenOpen by remember { mutableStateOf(false) }
    val isCallActive by viewModel.isCallActive.collectAsState()
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = OledBlack,
            bottomBar = {
                NavigationBar(
                    containerColor = OledBlack,
                    modifier = Modifier.border(1.dp, OledBorderSubtle)
                ) {
                    NavigationItem.values().forEach { item ->
                        val selected = currentTab == item && !isAuthScreenOpen
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isAuthScreenOpen = false
                                currentTab = item
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextPrimary,
                                selectedTextColor = TextPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = OledSurfaceElevated
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = isAuthScreenOpen,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.98f, animationSpec = tween(250, easing = FastOutSlowInEasing)))
                            .togetherWith(fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing)))
                    },
                    label = "AuthModalTransition"
                ) { inAuth ->
                    if (inAuth) {
                        AuthScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { isAuthScreenOpen = false },
                            onClose = { isAuthScreenOpen = false }
                        )
                    } else {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                                (slideInHorizontally(animationSpec = tween(260, easing = FastOutSlowInEasing)) { (it / 3) * direction } +
                                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                    scaleIn(initialScale = 0.98f, animationSpec = tween(260, easing = FastOutSlowInEasing)))
                                    .togetherWith(
                                        slideOutHorizontally(animationSpec = tween(180, easing = FastOutLinearInEasing)) { (-it / 3) * direction } +
                                        fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing))
                                    )
                            },
                            label = "MainTabTransition"
                        ) { tab ->
                            when (tab) {
                                NavigationItem.NUMBERS -> NumbersScreen(
                                    viewModel = viewModel,
                                    onNavigateToChat = { currentTab = NavigationItem.MESSAGES },
                                    onOpenAuth = { isAuthScreenOpen = true }
                                )
                                NavigationItem.KEYPAD -> DialerScreen(viewModel = viewModel)
                                NavigationItem.MESSAGES -> MessagesScreen(
                                    viewModel = viewModel,
                                    onOpenChat = { /* Detail */ }
                                )
                                NavigationItem.VOICEMAIL -> VoicemailScreen(viewModel = viewModel)
                                NavigationItem.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateToAuth = { isAuthScreenOpen = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Call Screen Overlay
        if (isCallActive) {
            ActiveCallScreen(viewModel)
        }
    }
}
