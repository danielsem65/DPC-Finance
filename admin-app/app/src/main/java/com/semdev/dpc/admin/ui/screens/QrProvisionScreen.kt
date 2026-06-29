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
import java.util.UUID

@Composable
fun QrProvisionScreen(modifier: Modifier = Modifier) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var apkUrl by remember {
        mutableStateOf("https://github.com/danielsem65/DPC-Finance/releases/latest/download/user-app.apk")
    }
    var apkChecksum by remember { mutableStateOf("") }
    var dealerId by remember { mutableStateOf("dealer-demo-001") }
    var accountId by remember { mutableStateOf("") }
    var activationCode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var rawJson by remember { mutableStateOf<String?>(null) }

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
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = apkChecksum,
            onValueChange = { apkChecksum = it },
            label = { Text("APK SHA-256 Checksum (optional, base64)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = dealerId,
            onValueChange = { dealerId = it },
            label = { Text("Dealer ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = accountId,
            onValueChange = { accountId = it },
            label = { Text("Account ID (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = activationCode,
            onValueChange = { activationCode = it },
            label = { Text("Activation Code (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                error = null
                rawJson = null
                if (apkUrl.isBlank()) {
                    error = "Enter a valid APK URL"
                    return@Button
                }
                try {
                    val componentName = "com.semdev.dpc.user/com.semdev.dpc.user.DeviceAdminReceiver"
                    val provisioningToken = UUID.randomUUID().toString().replace("-", "")
                    val deviceId = UUID.randomUUID().toString()

                            val extrasJson = buildString {
                        append("""{"schemaVersion":"1","provisioningToken":""")
                        append("\"$provisioningToken\"")
                        append(""","deviceId":""")
                        append("\"$deviceId\"")
                        append(""","dealerId":""")
                        append("\"$dealerId\"")
                        if (accountId.isNotBlank()) {
                            append(""","accountId":""")
                            append("\"$accountId\"")
                        }
                        if (activationCode.isNotBlank()) {
                            append(""","activationCode":""")
                            append("\"$activationCode\"")
                        }
                        append("}")
                    }

                    val json = buildString {
                        append("""{"android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME":""")
                        append("\"$componentName\"")
                        append(""","android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION":""")
                        append("\"$apkUrl\"")
                        if (apkChecksum.isNotBlank()) {
                            append(""","android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM":""")
                            append("\"$apkChecksum\"")
                        }
                        append(""","android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED":true""")
                        append(""","android.app.extra.PROVISIONING_SKIP_EDUCATION_SCREENS":true""")
                        append(""","android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE":""")
                        append(extrasJson)
                        append("}")
                    }
                    rawJson = json
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
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Provisioning Token", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(rawJson?.let { extractProvisioningToken(it) } ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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

private fun extractProvisioningToken(json: String): String {
    val key = "\"provisioningToken\":\""
    val start = json.indexOf(key)
    if (start == -1) return ""
    val end = json.indexOf("\"", start + key.length)
    return if (end == -1) "" else json.substring(start + key.length, end)
}
