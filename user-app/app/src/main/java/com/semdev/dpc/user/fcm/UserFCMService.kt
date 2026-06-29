package com.semdev.dpc.user.fcm

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.semdev.dpc.user.DeviceAdminReceiver
import com.semdev.dpc.user.LockScreenActivity
import com.semdev.dpc.user.UserApp
import com.semdev.dpc.user.firestore.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserFCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DeviceRepository.updateToken(this@UserFCMService, token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val command = message.data["command"] ?: return
        Log.d(TAG, "Command received: $command")
        when (command) {
            "LOCK" -> handleLock()
            "UNLOCK" -> handleUnlock()
            "REBOOT" -> handleReboot()
            "WIPE" -> handleWipe()
            else -> Log.w(TAG, "Unknown command: $command")
        }
    }

    private fun handleLock() {
        showNotification("Device Locked", "Remote lock command received")
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DeviceRepository.updateLockStatus(this@UserFCMService, true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update lock status", e)
            }
        }
    }

    private fun handleUnlock() {
        val dm = android.app.admin.DevicePolicyManager::class.java
            .let { getSystemService(it.name) as android.app.admin.DevicePolicyManager }
        val cn = DeviceAdminReceiver.getComponentName(this)
        dm.lockNow() // just a toggle; activity will be dismissed
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DeviceRepository.updateLockStatus(this@UserFCMService, false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update lock status", e)
            }
        }
        // The LockScreenActivity listens for UNLOCK broadcast
        sendBroadcast(Intent("com.semdev.dpc.user.ACTION_UNLOCK"))
    }

    private fun handleReboot() {
        val pm = getSystemService(android.os.PowerManager::class.java)
        try {
            pm.reboot("TouchBase remote reboot")
        } catch (e: Exception) {
            Log.e(TAG, "Reboot failed", e)
        }
    }

    private fun handleWipe() {
        val dm = getSystemService(android.app.admin.DevicePolicyManager::class.java)
        val cn = DeviceAdminReceiver.getComponentName(this)
        if (dm.isAdminActive(cn)) {
            dm.wipeData(android.app.admin.DevicePolicyManager.WIPE_RESET_PROTECTION_DATA)
        }
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, UserApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(1001, notification)
    }

    companion object {
        private const val TAG = "UserFCMService"
    }
}
