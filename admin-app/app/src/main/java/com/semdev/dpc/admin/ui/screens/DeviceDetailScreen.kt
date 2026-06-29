package com.semdev.dpc.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semdev.dpc.admin.firestore.AdminRepository
import com.semdev.dpc.admin.firestore.Device
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(deviceId: String, onBack: () -> Unit) {
    var device by remember { mutableStateOf<Device?>(null) }
    var loading by remember { mutableStateOf(true) }
    var commandLoading by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(deviceId) {
        device = AdminRepository.getDevice(deviceId)
        loading = false
    }

    fun sendCommand(type: String) {
        commandLoading = true
        feedback = null
        scope.launch {
            val success = AdminRepository.sendCommand(deviceId, type)
            commandLoading = false
            feedback = if (success) "Command '$type' sent successfully"
            else "Failed to send command '$type'"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Control") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (device == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Device not found", fontSize = 18.sp)
                }
            } else {
                val d = device!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Device Info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow("Model", d.model.ifBlank { "Unknown" })
                            InfoRow("Device ID", d.deviceId)
                            InfoRow("Status", if (d.locked) "Locked" else "Unlocked")
                            InfoRow(
                                "Last Seen",
                                dateFormat.format(Date(d.lastSeen))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Commands", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    CommandButton(
                        text = "LOCK DEVICE",
                        icon = Icons.Default.Lock,
                        color = Color(0xFFD32F2F),
                        enabled = !commandLoading,
                        onClick = { sendCommand("LOCK") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CommandButton(
                        text = "UNLOCK DEVICE",
                        icon = Icons.Default.LockOpen,
                        color = Color(0xFF388E3C),
                        enabled = !commandLoading,
                        onClick = { sendCommand("UNLOCK") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CommandButton(
                        text = "REBOOT DEVICE",
                        icon = Icons.Default.RestartAlt,
                        color = Color(0xFFF57C00),
                        enabled = !commandLoading,
                        onClick = { sendCommand("REBOOT") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CommandButton(
                        text = "FACTORY RESET",
                        icon = Icons.Default.DeleteForever,
                        color = Color(0xFFB71C1C),
                        enabled = !commandLoading,
                        onClick = { sendCommand("WIPE") }
                    )

                    if (feedback != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = feedback!!,
                            color = if (feedback!!.startsWith("Failed"))
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFF388E3C),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun CommandButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
