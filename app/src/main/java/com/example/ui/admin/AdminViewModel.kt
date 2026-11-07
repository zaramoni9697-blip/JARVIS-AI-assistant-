package com.example.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppStats
import com.example.data.model.UserProfile
import com.example.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminUiState {
    data object Loading : AdminUiState
    data class Success(val stats: AppStats, val users: List<UserProfile>) : AdminUiState
    data class Error(val message: String) : AdminUiState
}

class AdminViewModel(private val adminRepository: AdminRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            val statsResult = adminRepository.getSystemStats()
            val usersResult = adminRepository.getAllUsers()

            if (statsResult.isSuccess && usersResult.isSuccess) {
                _uiState.value = AdminUiState.Success(
                    stats = statsResult.getOrThrow(),
                    users = usersResult.getOrThrow()
                )
            } else {
                val errorMsg = statsResult.exceptionOrNull()?.localizedMessage
                    ?: usersResult.exceptionOrNull()?.localizedMessage
                    ?: "Failed to load admin analytics"
                _uiState.value = AdminUiState.Error(errorMsg)
            }
        }
    }

    fun toggleUserStatus(userId: String, currentStatus: String) {
        viewModelScope.launch {
            val result = adminRepository.toggleUserStatus(userId, currentStatus)
            result.fold(
                onSuccess = { newStatus ->
                    _actionFeedback.value = "User account status changed to $newStatus"
                    loadDashboardData()
                },
                onFailure = {
                    _actionFeedback.value = "Action failed: ${it.localizedMessage}"
                }
            )
        }
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }
}
