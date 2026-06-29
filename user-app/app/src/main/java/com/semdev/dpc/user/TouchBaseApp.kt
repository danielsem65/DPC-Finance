package com.semdev.dpc.user

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import com.semdev.dpc.user.firestore.DeviceRepository

@Composable
fun TouchBaseApp() {
    val context = LocalContext.current
    val dpm = remember {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }
    val adminName = remember { ComponentName(context, DeviceAdminReceiver::class.java) }
    val isDeviceOwner = remember { dpm.isDeviceOwnerApp(context.packageName) }
    var deviceId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            deviceId = DeviceRepository.registerDevice(context)
            Log.d("TouchBase", "Device registered: $deviceId")
        } catch (e: Exception) {
            Log.e("TouchBase", "Failed to register device", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isDeviceOwner) {
            Text(
                text = "NOT DEVICE OWNER",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This app must be set as Device Owner to function.\n\n" +
                        "Run: adb shell dpm set-device-owner com.semdev.dpc.user/.DeviceAdminReceiver",
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Device Protected",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This device is managed by TouchBase.\nAdministrator controls apply.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Device ID: $deviceId",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
