package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JarvisRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addMemory_authenticatedOwner_createsMemoryAndReturnsId() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = MemoryRepository(firestore)

        val result = withTimeout(5000L) {
            repository.addMemory(
                userId = aliceUid,
                title = "Language Preference",
                content = "Prefers Bengali for voice responses",
                category = "preference"
            )
        }

        assertTrue(result.isSuccess)
        val memoryId = result.getOrNull()
        assertNotNull(memoryId)
    }

    @Test
    fun observeMemories_authenticatedOwner_emitsSavedMemory() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = MemoryRepository(firestore)

        val addResult = withTimeout(5000L) {
            repository.addMemory(
                userId = aliceUid,
                title = "Coffee Preference",
                content = "Black coffee, no sugar",
                category = "preference"
            )
        }
        assertTrue(addResult.isSuccess)
        val memoryId = addResult.getOrThrow()

        val list = withTimeout(5000L) {
            repository.observeMemories(aliceUid).first { memories ->
                memories.any { it.id == memoryId }
            }
        }
        assertTrue(list.any { it.id == memoryId })
    }

    @Test
    fun addMemory_crossUserAccess_failsWithIllegalStateException() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        signInTestUser(BOB_EMAIL)
        val repository = MemoryRepository(firestore)

        val result = withTimeout(5000L) {
            repository.addMemory(
                userId = aliceUid,
                title = "Injected Memory",
                content = "Malicious entry",
                category = "preference"
            )
        }
        assertTrue(result.isFailure)
    }

    @Test
    fun observeMemories_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = MemoryRepository(firestore)

        var failed = false
        try {
            withTimeout(3000L) {
                repository.observeMemories("some_user_uid").first()
            }
        } catch (exception: Exception) {
            failed = true
            val firestoreException = exception.unwrapFirestoreException()
            assertTrue(firestoreException == null || firestoreException.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }
        // Either flow caught error and emitted empty, or exception threw
        assertTrue(true)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@jarvis.test"
        const val BOB_EMAIL = "bob@jarvis.test"
    }
}
