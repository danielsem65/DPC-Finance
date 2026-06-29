package com.semdev.dpc.user.firestore

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object DeviceRepository {
    private const val COLLECTION = "devices"
    private const val FIELD_TOKEN = "fcmToken"
    private const val FIELD_LOCKED = "locked"
    private const val FIELD_LAST_SEEN = "lastSeen"
    private const val FIELD_MODEL = "model"
    private const val FIELD_DEVICE_ID = "deviceId"

    private val db = FirebaseFirestore.getInstance()

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    suspend fun register(context: Context): String {
        val deviceId = getDeviceId(context)
        val token = FirebaseMessaging.getInstance().token.await()
        val data = hashMapOf(
            FIELD_DEVICE_ID to deviceId,
            FIELD_TOKEN to token,
            FIELD_LOCKED to false,
            FIELD_LAST_SEEN to System.currentTimeMillis(),
            FIELD_MODEL to android.os.Build.MODEL
        )
        db.collection(COLLECTION).document(deviceId)
            .set(data)
            .await()
        return deviceId
    }

    suspend fun updateToken(context: Context, token: String) {
        val deviceId = getDeviceId(context)
        db.collection(COLLECTION).document(deviceId)
            .update(FIELD_TOKEN, token, FIELD_LAST_SEEN, System.currentTimeMillis())
            .await()
    }

    suspend fun updateLockStatus(context: Context, locked: Boolean, adminName: String? = null) {
        val deviceId = getDeviceId(context)
        val updates = mutableMapOf<String, Any>(FIELD_LOCKED to locked, FIELD_LAST_SEEN to System.currentTimeMillis())
        if (locked && !adminName.isNullOrEmpty()) {
            updates["lockedBy"] = adminName
        }
        db.collection(COLLECTION).document(deviceId)
            .update(updates)
            .await()
    }
}
