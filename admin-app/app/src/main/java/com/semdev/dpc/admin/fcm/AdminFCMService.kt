package com.semdev.dpc.admin.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AdminFCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(TAG, "Admin FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Admin message: ${message.data}")
    }

    companion object {
        private const val TAG = "AdminFCMService"
    }
}
