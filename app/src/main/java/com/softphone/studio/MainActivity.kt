package com.softphone.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
                MainRoot(viewModel)
            }
        }
    }
}

@Composable
fun MainRoot(viewModel: SoftphoneViewModel) {
    var currentTab by remember { mutableStateOf(NavigationItem.NUMBERS) }
    var isAuthScreenOpen by remember { mutableStateOf(false) }
    val isCallActive by viewModel.isCallActive.collectAsState()

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
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (isAuthScreenOpen) {
                    AuthScreen(viewModel = viewModel, onLoginSuccess = { isAuthScreenOpen = false }, onClose = { isAuthScreenOpen = false })
                } else {
                    when (currentTab) {
                        NavigationItem.NUMBERS -> NumbersScreen(viewModel, onNavigateToChat = { currentTab = NavigationItem.MESSAGES }, onOpenAuth = { isAuthScreenOpen = true })
                        NavigationItem.KEYPAD -> DialerScreen(viewModel)
                        NavigationItem.MESSAGES -> MessagesScreen(viewModel = viewModel, onOpenChat = { /* Detail */ })
                        NavigationItem.VOICEMAIL -> VoicemailScreen(viewModel)
                        NavigationItem.SETTINGS -> SettingsScreen(viewModel, onNavigateToAuth = { isAuthScreenOpen = true })
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
