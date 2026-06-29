package com.semdev.dpc.user.firestore

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object DeviceRepository {
    private const val COLLECTION = "devices"
    private const val FIELD_TOKEN = "fcmToken"
    private const val FIELD_LOCKED = "locked"
    private const val FIELD_LAST_SEEN = "lastSeen"
    private const val FIELD_MODEL = "model"
    private const val FIELD_DEVICE_ID = "deviceId"
    private const val EXTRA_PROVISIONING = "provisioning"
    private const val EXTRA_DEALER_ID = "dealerId"
    private const val EXTRA_ACCOUNT_ID = "accountId"
    private const val EXTRA_ACTIVATION_CODE = "activationCode"
    private const val PREFS_NAME = "touchbase_provisioning"
    private const val PREF_REGISTERED = "registered"

    private val db = FirebaseFirestore.getInstance()

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun hasRegistered(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_REGISTERED, false)
    }

    suspend fun register(context: Context, adminExtras: Bundle? = null): String {
        val deviceId = getDeviceId(context)
        val token = FirebaseMessaging.getInstance().token.await()
        val data = hashMapOf(
            FIELD_DEVICE_ID to deviceId,
            FIELD_TOKEN to token,
            FIELD_LOCKED to false,
            FIELD_LAST_SEEN to System.currentTimeMillis(),
            FIELD_MODEL to android.os.Build.MODEL
        )

        adminExtras?.let { extras ->
            val provisioning = hashMapOf<String, Any>()
            extras.getString(EXTRA_DEALER_ID)?.let { if (it.isNotEmpty()) provisioning[EXTRA_DEALER_ID] = it }
            extras.getString(EXTRA_ACCOUNT_ID)?.let { if (it.isNotEmpty()) provisioning[EXTRA_ACCOUNT_ID] = it }
            extras.getString(EXTRA_ACTIVATION_CODE)?.let { if (it.isNotEmpty()) provisioning[EXTRA_ACTIVATION_CODE] = it }
            if (provisioning.isNotEmpty()) data[EXTRA_PROVISIONING] = provisioning
        }

        db.collection(COLLECTION).document(deviceId).set(data, SetOptions.merge()).await()

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_REGISTERED, true).apply()

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
