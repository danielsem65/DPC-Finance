package com.semdev.dpc.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.semdev.dpc.admin.firestore.AdminRepository
import com.semdev.dpc.admin.ui.screens.DashboardScreen
import com.semdev.dpc.admin.ui.screens.DeviceDetailScreen
import com.semdev.dpc.admin.ui.screens.LoginScreen
import com.semdev.dpc.admin.ui.screens.QrProvisionScreen

sealed class Screen { object Login : Screen(); object Dashboard : Screen(); class DeviceDetail(val deviceId: String) : Screen(); object QrProvision : Screen() }

data class AppState(
    val currentScreen: Screen = Screen.Login,
    val isLoggedIn: Boolean = false,
    val userEmail: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    var state by remember { mutableStateOf(AppState()) }
    val scope = rememberCoroutineScope()

    when (val screen = state.currentScreen) {
        is Screen.Login -> LoginScreen(
            onLoginSuccess = { email ->
                state = state.copy(isLoggedIn = true, userEmail = email, currentScreen = Screen.Dashboard)
            }
        )
        is Screen.Dashboard -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("TouchBase Admin", fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = true,
                            onClick = { state = state.copy(currentScreen = Screen.Dashboard) },
                            icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                            label = { Text("Devices") }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { state = state.copy(currentScreen = Screen.QrProvision) },
                            icon = { Icon(Icons.Filled.QrCode, contentDescription = null) },
                            label = { Text("Provision") }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = {
                                AdminRepository.logout()
                                state = AppState()
                            },
                            icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                            label = { Text("Logout") }
                        )
                    }
                }
            ) { padding ->
                DashboardScreen(
                    modifier = Modifier.padding(padding),
                    onDeviceClick = { deviceId ->
                        state = state.copy(currentScreen = Screen.DeviceDetail(deviceId))
                    }
                )
            }
        }
        is Screen.DeviceDetail -> DeviceDetailScreen(
            deviceId = screen.deviceId,
            onBack = { state = state.copy(currentScreen = Screen.Dashboard) }
        )
        is Screen.QrProvision -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Provision Device") },
                        navigationIcon = {
                            TextButton(onClick = { state = state.copy(currentScreen = Screen.Dashboard) }) {
                                Text("Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            ) { padding ->
                QrProvisionScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}
