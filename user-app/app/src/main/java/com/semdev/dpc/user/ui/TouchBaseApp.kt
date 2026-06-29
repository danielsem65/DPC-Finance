package com.semdev.dpc.user.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semdev.dpc.user.DeviceAdminReceiver
import com.semdev.dpc.user.firestore.DeviceRepository
import kotlinx.coroutines.launch

@Composable
fun TouchBaseApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var deviceId by remember { mutableStateOf("") }
    var isRegistered by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val dm = context.getSystemService(DevicePolicyManager::class.java)
    val cn = DeviceAdminReceiver.getComponentName(context)
    val isDeviceOwner = dm.isDeviceOwnerApp(context.packageName)

    LaunchedEffect(Unit) {
        try {
            val id = DeviceRepository.getDeviceId(context)
            deviceId = id
            DeviceRepository.register(context)
            isRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TouchBase DPC") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Registering device...")
            } else {
                Icon(
                    imageVector = if (isDeviceOwner) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = if (isDeviceOwner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (isDeviceOwner) "Device Protected" else "Not Device Owner",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDeviceOwner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "ID: $deviceId",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                if (!isDeviceOwner) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = "Run: adb shell dpm set-device-owner com.semdev.dpc.user/.DeviceAdminReceiver",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                if (isRegistered) {
                    Spacer(Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Firebase: Connected", fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                            Text("FCM: Active", fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
}
