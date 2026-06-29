package com.semdev.dpc.user

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class LockScreenActivity : ComponentActivity() {
    private var shouldFinish by mutableStateOf(false)

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            shouldFinish = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())

        registerReceiver(unlockReceiver, IntentFilter("com.semdev.dpc.user.ACTION_UNLOCK"), RECEIVER_NOT_EXPORTED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            val km = getSystemService(KeyguardManager::class.java)
            km.requestDismissKeyguard(this, null)
        }

        setContent {
            LockScreenContent()
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldFinish) {
            finishAndRemoveTask()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(unlockReceiver) } catch (_: Exception) {}
    }

    @Composable
    private fun LockScreenContent() {
        LaunchedEffect(shouldFinish) {
            if (shouldFinish) {
                finishAndRemoveTask()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A237E)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔒", fontSize = 72.sp)
                Spacer(Modifier.height(24.dp))
                Text(
                    "Device Locked",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "This device has been remotely locked.\nContact your administrator.",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
