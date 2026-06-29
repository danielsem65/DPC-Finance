package com.semdev.dpc.user

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semdev.dpc.user.firestore.DeviceRepository
import com.semdev.dpc.user.ui.theme.LockDPCTheme

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        setContent {
            LockDPCTheme(darkTheme = true) {
                LockScreenContent()
            }
        }
    }

    @Composable
    private fun LockScreenContent() {
        val context = LocalContext.current
        val dpm = remember {
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        }
        val adminName = remember { ComponentName(context, DeviceAdminReceiver::class.java) }
        var isUnlocking by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD32F2F)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "⚠",
                    fontSize = 72.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Device Locked",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This device has been locked by your administrator.\nContact support to restore access.",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        isUnlocking = true
                        dpm.setKeyguardDisabled(adminName, false)
                        DeviceRepository.updateLockStatus(context, false)
                        finish()
                    },
                    enabled = !isUnlocking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isUnlocking) "Unlocking..." else "Unlock Device",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* Emergency call intent */ },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Emergency Call",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
