package com.example.ui.coding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.JarvisBrain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CodingUiState {
    data object Idle : CodingUiState
    data object Loading : CodingUiState
    data class Success(val response: String) : CodingUiState
    data class Error(val message: String) : CodingUiState
}

class CodingViewModel : ViewModel() {

    private val brain = JarvisBrain()

    private val _uiState = MutableStateFlow<CodingUiState>(CodingUiState.Idle)
    val uiState: StateFlow<CodingUiState> = _uiState.asStateFlow()

    private val _selectedAction = MutableStateFlow("generate") // "generate", "explain", "debug", "optimize"
    val selectedAction: StateFlow<String> = _selectedAction.asStateFlow()

    private val _queryInput = MutableStateFlow("")
    val queryInput: StateFlow<String> = _queryInput.asStateFlow()

    private val _codeSnippetInput = MutableStateFlow("")
    val codeSnippetInput: StateFlow<String> = _codeSnippetInput.asStateFlow()

    fun setAction(action: String) {
        _selectedAction.value = action
    }

    fun setQuery(query: String) {
        _queryInput.value = query
    }

    fun setCodeSnippet(snippet: String) {
        _codeSnippetInput.value = snippet
    }

    fun executeCodingTask(langPref: String = "auto") {
        val query = _queryInput.value.trim()
        val snippet = _codeSnippetInput.value.trim()

        if (query.isEmpty() && snippet.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = CodingUiState.Loading
            val result = brain.generateCodeAssistance(
                actionType = _selectedAction.value,
                userQuery = query.ifEmpty { "Analyze and process the provided snippet" },
                codeSnippet = snippet.ifEmpty { null },
                languagePreference = langPref
            )
            result.fold(
                onSuccess = { _uiState.value = CodingUiState.Success(it) },
                onFailure = { _uiState.value = CodingUiState.Error(it.localizedMessage ?: "Coding analysis failed") }
            )
        }
    }

    fun clear() {
        _queryInput.value = ""
        _codeSnippetInput.value = ""
        _uiState.value = CodingUiState.Idle
    }
}
