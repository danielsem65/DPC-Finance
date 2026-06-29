package com.semdev.dpc.user.fcm

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.semdev.dpc.user.DeviceAdminReceiver
import com.semdev.dpc.user.LockScreenActivity
import com.semdev.dpc.user.firestore.DeviceRepository

class UserFCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        DeviceRepository.updateFcmToken(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val command = message.data[CMD_KEY] ?: return
        Log.d(TAG, "Received command: $command")
        val adminName = ComponentName(this, DeviceAdminReceiver::class.java)
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        when (command.uppercase()) {
            CMD_LOCK -> {
                dpm.lockNow()
                val intent = Intent(this, LockScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                DeviceRepository.updateLockStatus(this, true)
            }
            CMD_UNLOCK -> {
                dpm.setKeyguardDisabled(adminName, false)
                DeviceRepository.updateLockStatus(this, false)
            }
            CMD_REBOOT -> {
                dpm.reboot(adminName)
            }
            CMD_WIPE -> {
                dpm.wipeData(0)
            }
            CMD_LOCK_SCREEN -> {
                dpm.setDeviceOwnerLockScreenInfo(
                    adminName,
                    message.data[CMD_MESSAGE] ?: "Locked by administrator"
                )
            }
        }
    }

    companion object {
        const val TAG = "TouchBase-FCM"
        const val CMD_KEY = "command"
        const val CMD_LOCK = "LOCK"
        const val CMD_UNLOCK = "UNLOCK"
        const val CMD_REBOOT = "REBOOT"
        const val CMD_WIPE = "WIPE"
        const val CMD_LOCK_SCREEN = "LOCK_SCREEN"
        const val CMD_MESSAGE = "message"
    }
}
