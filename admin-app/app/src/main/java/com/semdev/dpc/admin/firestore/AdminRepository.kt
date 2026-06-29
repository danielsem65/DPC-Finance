package com.semdev.dpc.admin.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AdminRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private const val DEVICES_COL = "devices"
    private const val COMMANDS_COL = "commands"

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getDevices(): Result<List<Map<String, Any?>>> {
        return try {
            val snapshot = db.collection(DEVICES_COL).get().await()
            val devices = snapshot.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendCommand(deviceId: String, commandType: String, adminEmail: String): Result<String> {
        return try {
            val command = hashMapOf(
                "deviceId" to deviceId,
                "type" to commandType,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "createdBy" to adminEmail
            )
            val ref = db.collection(COMMANDS_COL).add(command).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDevice(deviceId: String): Result<Map<String, Any?>?> {
        return try {
            val doc = db.collection(DEVICES_COL).document(deviceId).get().await()
            val data = doc.data?.toMutableMap()
            data?.put("id", doc.id)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
