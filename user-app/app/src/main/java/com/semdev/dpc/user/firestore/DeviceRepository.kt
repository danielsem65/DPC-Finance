package com.semdev.dpc.user.firestore

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object DeviceRepository {
    private const val COLLECTION_DEVICES = "devices"
    private const val FIELD_DEVICE_ID = "deviceId"
    private const val FIELD_FCM_TOKEN = "fcmToken"
    private const val FIELD_LOCKED = "locked"
    private const val FIELD_LAST_SEEN = "lastSeen"
    private const val FIELD_MODEL = "model"

    private val db = com.google.firebase.Firebase.firestore

    suspend fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    suspend fun registerDevice(context: Context): String {
        val deviceId = getDeviceId(context)
        val token = FirebaseMessaging.getInstance().token.await()
        val deviceData = hashMapOf(
            FIELD_DEVICE_ID to deviceId,
            FIELD_FCM_TOKEN to token,
            FIELD_LOCKED to false,
            FIELD_LAST_SEEN to System.currentTimeMillis(),
            FIELD_MODEL to android.os.Build.MODEL
        )
        db.collection(COLLECTION_DEVICES).document(deviceId)
            .set(deviceData, SetOptions.merge())
            .await()
        return deviceId
    }

    fun updateFcmToken(context: Context, token: String) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        db.collection(COLLECTION_DEVICES).document(deviceId)
            .update(FIELD_FCM_TOKEN, token, FIELD_LAST_SEEN, System.currentTimeMillis())
    }

    fun updateLockStatus(context: Context, locked: Boolean) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        db.collection(COLLECTION_DEVICES).document(deviceId)
            .update(FIELD_LOCKED, locked, FIELD_LAST_SEEN, System.currentTimeMillis())
    }
}
