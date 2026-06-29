package com.semdev.dpc.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.semdev.dpc.admin.ui.screens.DashboardScreen
import com.semdev.dpc.admin.ui.screens.DeviceDetailScreen
import com.semdev.dpc.admin.ui.screens.LoginScreen
import com.semdev.dpc.admin.ui.theme.AdminTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminTheme {
                val navController = rememberNavController()
                val auth = remember { FirebaseAuth.getInstance() }
                val isLoggedIn = remember { mutableStateOf(auth.currentUser != null) }

                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn.value) Screen.Dashboard.route else Screen.Login.route
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn.value = true
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            onDeviceClick = { deviceId ->
                                navController.navigate(Screen.DeviceDetail.createRoute(deviceId))
                            },
                            onLogout = {
                                auth.signOut()
                                isLoggedIn.value = false
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.DeviceDetail.route) { backStackEntry ->
                        val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
                        DeviceDetailScreen(
                            deviceId = deviceId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
