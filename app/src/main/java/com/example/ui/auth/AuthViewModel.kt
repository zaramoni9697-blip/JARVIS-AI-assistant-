package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser, val profile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
    data class AccountSuspended(val email: String) : AuthUiState
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val auth = Firebase.auth
    private val TAG = "AuthViewModel"

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        authListener = FirebaseAuth.AuthStateListener { fbAuth ->
            val user = fbAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                loadProfile(user)
            } else {
                _userProfile.value = null
                _uiState.value = AuthUiState.Idle
            }
        }
        auth.addAuthStateListener(authListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        authListener?.let { auth.removeAuthStateListener(it) }
    }

    private fun loadProfile(user: FirebaseUser) {
        viewModelScope.launch {
            try {
                val profile = userRepository.syncUserProfile(user)
                _userProfile.value = profile
                if (profile.status == "suspended") {
                    _uiState.value = AuthUiState.AccountSuspended(user.email ?: "Your account")
                } else {
                    _uiState.value = AuthUiState.Authenticated(user, profile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load/sync user profile", e)
                // Still permit authenticated entry with fallback profile
                val fallback = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Agent"
                )
                _userProfile.value = fallback
                _uiState.value = AuthUiState.Authenticated(user, fallback)
            }
        }
    }

    fun attemptAutoSignIn(context: Context) {
        if (auth.currentUser != null) {
            loadProfile(auth.currentUser!!)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "default_web_client_id not found")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user?.let { loadProfile(it) }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Silent auto-sign-in skipped: ${e.message}")
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error("Google Sign-In configuration missing (default_web_client_id)")
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        loadProfile(user)
                    } else {
                        _uiState.value = AuthUiState.Error("Authentication failed: user is null")
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Unexpected credential type received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        auth.signOut()
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                _currentUser.value = null
                _userProfile.value = null
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    fun updateLanguagePreference(preference: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                userRepository.updateLanguagePreference(uid, preference)
                _userProfile.value = _userProfile.value?.copy(languagePreference = preference)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update language preference", e)
            }
        }
    }
}
