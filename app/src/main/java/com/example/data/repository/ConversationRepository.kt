package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Conversation
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ConversationRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun observeConversations(userId: String): Flow<List<Conversation>> {
        val collectionRef = db.collection("users").document(userId).collection("conversations")
        return collectionRef.orderBy("createdAt", Query.Direction.ASCENDING).snapshots().map { snapshot ->
            snapshot.toObjects(Conversation::class.java)
        }.catch { e ->
            handleFirestoreError(e as Exception, OperationType.LIST, collectionRef.path)
            emit(emptyList())
        }
    }

    suspend fun addMessage(
        userId: String,
        role: String,
        content: String,
        detectedLang: String = "auto",
        mode: String = "chat"
    ): Result<String> {
        return try {
            val currentUid = auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
            require(currentUid == userId) { "Unauthorized conversation write" }

            val msgId = "msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val docRef = db.collection("users").document(userId).collection("conversations").document(msgId)

            val payload = hashMapOf<String, Any>(
                "id" to msgId,
                "userId" to userId,
                "role" to role,
                "content" to content.trim(),
                "detectedLanguage" to detectedLang,
                "mode" to mode,
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload).await()
            Result.success(msgId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$userId/conversations")
            Result.failure(e)
        }
    }

    suspend fun clearHistory(userId: String): Result<Unit> {
        return try {
            val currentUid = auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
            require(currentUid == userId) { "Unauthorized conversation clear" }

            val collectionRef = db.collection("users").document(userId).collection("conversations")
            val snapshots = collectionRef.get().await()
            for (doc in snapshots.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$userId/conversations")
            Result.failure(e)
        }
    }
}
