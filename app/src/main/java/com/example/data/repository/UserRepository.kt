package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val TAG = "UserRepository"

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
            } else {
                null
            }
        }.catch { e ->
            handleFirestoreError(e as Exception, OperationType.GET, docRef.path)
            emit(null)
        }
    }

    suspend fun syncUserProfile(user: FirebaseUser): UserProfile {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java) ?: UserProfile(
                    userId = uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Agent"
                )
            } else {
                val isAdminEmail = user.email?.equals("mfoysalalom7@gmail.com", ignoreCase = true) == true
                val newProfile = UserProfile(
                    userId = uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Agent",
                    photoUrl = user.photoUrl?.toString(),
                    languagePreference = "auto",
                    role = if (isAdminEmail) "admin" else "user",
                    status = "active"
                )
                val payload = hashMapOf<String, Any>(
                    "userId" to uid,
                    "email" to newProfile.email,
                    "displayName" to newProfile.displayName,
                    "languagePreference" to newProfile.languagePreference,
                    "role" to newProfile.role,
                    "status" to newProfile.status,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                if (newProfile.photoUrl != null) {
                    payload["photoUrl"] = newProfile.photoUrl
                }
                docRef.set(payload).await()
                newProfile
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateLanguagePreference(userId: String, preference: String) {
        val docRef = db.collection("users").document(userId)
        try {
            docRef.update(
                mapOf(
                    "languagePreference" to preference,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateDisplayName(userId: String, newName: String) {
        val docRef = db.collection("users").document(userId)
        try {
            docRef.update(
                mapOf(
                    "displayName" to newName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun isUserAdmin(profile: UserProfile?): Boolean {
        if (profile == null) return false
        val currentEmail = auth.currentUser?.email
        return profile.role == "admin" || currentEmail?.equals("mfoysalalom7@gmail.com", ignoreCase = true) == true
    }
}
