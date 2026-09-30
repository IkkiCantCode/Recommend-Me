package com.ikki.recommendme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikki.recommendme.data.repository.AuthRepository
import com.ikki.recommendme.domain.model.AuthResult
import com.ikki.recommendme.domain.model.UserProfile
import com.ikki.recommendme.ui.appViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One-shot navigation event after a successful sign in / sign up. */
sealed interface AuthEvent {
    data class SignedIn(val profile: UserProfile) : AuthEvent
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    data class AuthUiState(
        val loading: Boolean = false,
        val error: String? = null,
        val event: AuthEvent? = null
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) = authenticate {
        authRepository.signIn(email, password)
    }

    fun signUp(name: String, email: String, password: String) = authenticate {
        authRepository.signUp(name, email, password)
    }

    fun signInWithGoogle() = authenticate {
        authRepository.signInWithGoogle()
    }

    private fun authenticate(block: suspend () -> AuthResult) {
        if (_uiState.value.loading) return
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = block()) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(loading = false, error = null, event = AuthEvent.SignedIn(result.profile))
                }
                is AuthResult.Error -> _uiState.update {
                    it.copy(loading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumeEvent() {
        _uiState.update { it.copy(event = null) }
    }

    companion object {
        val Factory = appViewModelFactory { AuthViewModel(it.authRepository) }
    }
}
