package com.semdev.dpc.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semdev.dpc.admin.firestore.AdminRepository
import kotlinx.coroutines.launch

data class CommandAction(val label: String, val type: String, val color: Color)

private val commands = listOf(
    CommandAction("LOCK", "LOCK", Color(0xFFF44336)),
    CommandAction("UNLOCK", "UNLOCK", Color(0xFF4CAF50)),
    CommandAction("REBOOT", "REBOOT", Color(0xFFFF9800)),
    CommandAction("WIPE", "WIPE", Color(0xFF000000)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceId: String,
    onBack: () -> Unit
) {
    var device by remember { mutableStateOf<Map<String, Any?>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(deviceId) {
        val result = AdminRepository.getDevice(deviceId)
        result.fold(
            onSuccess = { device = it; isLoading = false },
            onFailure = { isLoading = false; message = "Failed to load device" }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Details") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (device == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Device not found", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(device!!["model"] as? String ?: "Unknown", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Device ID", device!!["id"] as? String ?: "\u2014")
                        DetailRow("Locked", if ((device!!["locked"] as? Boolean) == true) "Yes" else "No")
                        DetailRow("Last Seen", formatTimestamp(device!!["lastSeen"] as? Long))
                        DetailRow("Locked By", device!!["lockedBy"] as? String ?: "\u2014")
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("Commands", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                commands.forEach { cmd ->
                    Button(
                        onClick = {
                            sending = cmd.type
                            scope.launch {
                                val email = AdminRepository.getCurrentUser()?.email ?: "admin"
                                val result = AdminRepository.sendCommand(deviceId, cmd.type, email)
                                sending = null
                                message = if (result.isSuccess) "${cmd.type} command sent" else "Failed: ${result.exceptionOrNull()?.message}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cmd.color),
                        enabled = sending != cmd.type
                    ) {
                        if (sending == cmd.type) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text(cmd.label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (message != null) {
                    Spacer(Modifier.height(16.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(message!!, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$label: ", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, fontSize = 14.sp)
    }
}

private fun formatTimestamp(millis: Long?): String {
    if (millis == null) return "\u2014"
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}
