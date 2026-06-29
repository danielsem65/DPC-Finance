package com.semdev.dpc.admin.firestore

import android.util.Log
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class Device(
    val deviceId: String = "",
    val fcmToken: String = "",
    val locked: Boolean = false,
    val lastSeen: Long = 0L,
    val model: String = ""
)

object AdminRepository {
    const val TAG = "LockDPC-AdminRepo"
    private const val COLLECTION_DEVICES = "devices"
    private const val COLLECTION_COMMANDS = "commands"

    private val db = com.google.firebase.Firebase.firestore

    suspend fun getDevices(): List<Device> {
        return try {
            val snapshot = db.collection(COLLECTION_DEVICES)
                .get(Source.SERVER)
                .await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Device(
                    deviceId = data["deviceId"] as? String ?: doc.id,
                    fcmToken = data["fcmToken"] as? String ?: "",
                    locked = data["locked"] as? Boolean ?: false,
                    lastSeen = data["lastSeen"] as? Long ?: 0L,
                    model = data["model"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get devices", e)
            emptyList()
        }
    }

    suspend fun getDevice(deviceId: String): Device? {
        return try {
            val doc = db.collection(COLLECTION_DEVICES).document(deviceId)
                .get(Source.SERVER)
                .await()
            val data = doc.data ?: return null
            Device(
                deviceId = data["deviceId"] as? String ?: doc.id,
                fcmToken = data["fcmToken"] as? String ?: "",
                locked = data["locked"] as? Boolean ?: false,
                lastSeen = data["lastSeen"] as? Long ?: 0L,
                model = data["model"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get device $deviceId", e)
            null
        }
    }

    suspend fun sendCommand(deviceId: String, commandType: String): Boolean {
        return try {
            val command = hashMapOf(
                "deviceId" to deviceId,
                "type" to commandType,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_COMMANDS)
                .document(UUID.randomUUID().toString())
                .set(command)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send command $commandType to $deviceId", e)
            false
        }
    }
}
