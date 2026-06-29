package com.semdev.dpc.admin.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semdev.dpc.admin.ui.QrEncoder

@Composable
fun QrProvisionScreen(modifier: Modifier = Modifier) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var apkUrl by remember {
        mutableStateOf("https://github.com/danielsem65/DPC-Finance/releases/latest/download/user-app.apk")
    }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Provision New Device", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "On a factory-reset device, tap the welcome screen 6 times\nto open the QR scanner, then scan the code below.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = apkUrl,
            onValueChange = { apkUrl = it },
            label = { Text("APK Download URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                error = null
                if (apkUrl.isBlank()) {
                    error = "Enter a valid APK URL"
                    return@Button
                }
                try {
                    val componentName = "com.semdev.dpc.user/com.semdev.dpc.user.DeviceAdminReceiver"
                    val json = """{"android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME":"$componentName","android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION":"$apkUrl","android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED":true,"android.app.extra.PROVISIONING_SKIP_EDUCATION_SCREENS":true}"""
                    qrBitmap = QrEncoder.encode(json)
                } catch (e: Exception) {
                    error = "Failed to generate QR: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Generate QR Code", fontWeight = FontWeight.Bold)
        }

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        if (qrBitmap != null) {
            Spacer(Modifier.height(24.dp))
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "Provisioning QR Code",
                        modifier = Modifier.size(280.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Scan with device camera", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp)) {
                Text("Instructions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Text("1. Factory reset the target device", fontSize = 13.sp)
                Text("2. Go through initial setup until the Welcome screen", fontSize = 13.sp)
                Text("3. Tap the welcome screen 6 times quickly", fontSize = 13.sp)
                Text("4. A QR scanner will appear", fontSize = 13.sp)
                Text("5. Scan the QR code above", fontSize = 13.sp)
                Text("6. Device will download and install the DPC app", fontSize = 13.sp)
                Text("7. Device is now enrolled under your control", fontSize = 13.sp)
            }
        }
    }
}
