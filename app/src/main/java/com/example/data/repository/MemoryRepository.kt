package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Memory
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MemoryRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(targetUserId: String): String {
        val currentUid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated")
        require(currentUid == targetUserId) { "Cross-user memory access is prohibited" }
        return currentUid
    }

    fun observeMemories(userId: String): Flow<List<Memory>> {
        val collectionRef = db.collection("users").document(userId).collection("memories")
        return collectionRef.snapshots().map { snapshot ->
            snapshot.toObjects(Memory::class.java)
        }.catch { e ->
            handleFirestoreError(e as Exception, OperationType.LIST, collectionRef.path)
            emit(emptyList())
        }
    }

    suspend fun addMemory(
        userId: String,
        title: String,
        content: String,
        category: String = "general"
    ): Result<String> {
        return try {
            requireUserId(userId)
            val memoryId = "mem_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            val docRef = db.collection("users").document(userId).collection("memories").document(memoryId)

            val payload = hashMapOf<String, Any>(
                "id" to memoryId,
                "userId" to userId,
                "title" to title.trim(),
                "content" to content.trim(),
                "category" to category.lowercase().trim(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload).await()
            Result.success(memoryId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$userId/memories")
            Result.failure(e)
        }
    }

    suspend fun updateMemory(
        userId: String,
        memoryId: String,
        title: String,
        content: String,
        category: String
    ): Result<Unit> {
        return try {
            requireUserId(userId)
            val docRef = db.collection("users").document(userId).collection("memories").document(memoryId)
            docRef.update(
                mapOf(
                    "title" to title.trim(),
                    "content" to content.trim(),
                    "category" to category.lowercase().trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$userId/memories/$memoryId")
            Result.failure(e)
        }
    }

    suspend fun deleteMemory(userId: String, memoryId: String): Result<Unit> {
        return try {
            requireUserId(userId)
            val docRef = db.collection("users").document(userId).collection("memories").document(memoryId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$userId/memories/$memoryId")
            Result.failure(e)
        }
    }
}
