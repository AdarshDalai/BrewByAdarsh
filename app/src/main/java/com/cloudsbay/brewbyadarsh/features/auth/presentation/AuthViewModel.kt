package com.cloudsbay.brewbyadarsh.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthSession
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthUser
import com.cloudsbay.brewbyadarsh.features.auth.domain.usecases.GetCurrentSessionUseCase
import com.cloudsbay.brewbyadarsh.features.auth.domain.usecases.ObserveAuthStateUseCase
import com.cloudsbay.brewbyadarsh.features.auth.domain.usecases.ResetPasswordUseCase
import com.cloudsbay.brewbyadarsh.features.auth.domain.usecases.SignInUseCase
import com.cloudsbay.brewbyadarsh.features.auth.domain.usecases.SignInWithOAuthUseCase
import com.cloudsbay.brewbyadarsh.features.auth.domain.usecases.SignOutUseCase
import com.cloudsbay.brewbyadarsh.features.auth.domain.usecases.SignUpUseCase
import com.cloudsbay.brewbyadarsh.features.auth.domain.usecases.RefreshSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val getCurrentSessionUseCase: GetCurrentSessionUseCase,
    private val signInWithOAuthUseCase: SignInWithOAuthUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val refreshSessionUseCase: RefreshSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: Flow<Boolean> = observeAuthStateUseCase().map { it != null }

    init {
        observeAuthState()
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = signInUseCase(email, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentSession = result.data,
                            authUser = result.data.user,
                            isAuthenticated = true
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is AuthResult.UserVerificationRequired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.message,
                            hasUnverifiedEmail = true,
                            unverifiedUserEmail = email
                        )
                    }
                }
            }
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            when (val result = signUpUseCase(email, password, confirmPassword)) {
                is AuthResult.Success -> {
                    val user = result.data
                    // Check if email is not verified
                    if (user.emailConfirmedAt == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                authUser = user,
                                hasUnverifiedEmail = true,
                                unverifiedUserEmail = user.email
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                authUser = user,
                                hasUnverifiedEmail = false
                            )
                        }
                    }
                }
                is AuthResult.UserVerificationRequired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.message,
                            hasUnverifiedEmail = true
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = signOutUseCase()) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentSession = null,
                            authUser = null,
                            isAuthenticated = false
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Sign out failed"
                        )
                    }
                }
                is AuthResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is AuthResult.UserVerificationRequired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = resetPasswordUseCase(email)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Password reset email sent"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to send reset email"
                        )
                    }
                }
                is AuthResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is AuthResult.UserVerificationRequired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    fun signInWithOAuth(provider: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = signInWithOAuthUseCase(provider)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentSession = result.data,
                            authUser = result.data.user,
                            isAuthenticated = true
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is AuthResult.UserVerificationRequired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    fun getCurrentSession() {
        viewModelScope.launch {
            when (val result = getCurrentSessionUseCase()) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentSession = result.data,
                            authUser = result.data?.user,
                            isAuthenticated = result.data != null
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                is AuthResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is AuthResult.UserVerificationRequired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            when (val result = refreshSessionUseCase()) {
                is AuthResult.Success -> {
                    val session = result.data
                    val user = session.user
                    if (user.emailConfirmedAt != null) {
                        _uiState.update {
                            it.copy(
                                currentSession = session,
                                authUser = user,
                                isAuthenticated = true,
                                hasUnverifiedEmail = false,
                                unverifiedUserEmail = null,
                                error = null,
                                message = null
                            )
                        }
                    }
                }
                is AuthResult.Error -> {
                    // Silent on periodic checks to avoid noisy UI while waiting for verification.
                }
                is AuthResult.Loading -> Unit
                is AuthResult.UserVerificationRequired -> Unit
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { user ->
                _uiState.update {
                    it.copy(
                        authUser = user,
                        isAuthenticated = user != null
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearUnverifiedEmailState() {
        _uiState.update { it.copy(hasUnverifiedEmail = false) }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val authUser: AuthUser? = null,
    val currentSession: AuthSession? = null,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val hasUnverifiedEmail: Boolean = false,
    val unverifiedUserEmail: String? = null
)
