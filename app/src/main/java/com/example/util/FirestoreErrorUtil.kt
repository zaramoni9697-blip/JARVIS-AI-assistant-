package com.example.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType {
    GET,
    LIST,
    CREATE,
    UPDATE,
    DELETE,
    TRANSACTION
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String
) {
    try {
        val rootCause = generateSequence(exception as Throwable) { it.cause }.last()
        val isFirestoreException = rootCause is FirebaseFirestoreException

        val diagnostic = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("operation", operationType.name)
            put("path", path)
            put("exceptionClass", rootCause.javaClass.simpleName)
            put("message", rootCause.message ?: "No message provided")
            if (isFirestoreException) {
                val ffe = rootCause as FirebaseFirestoreException
                put("firestoreErrorCode", ffe.code.name)
                put("firestoreErrorValue", ffe.code.value())
            }
        }
        Log.e("FirestoreError", diagnostic.toString())
    } catch (e: Exception) {
        Log.e("FirestoreError", "Failed to format Firestore error log", e)
    }
}
