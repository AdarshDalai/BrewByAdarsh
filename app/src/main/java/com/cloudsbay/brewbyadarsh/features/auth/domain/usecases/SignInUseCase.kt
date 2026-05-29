package com.cloudsbay.brewbyadarsh.features.auth.domain.usecases

import com.cloudsbay.brewbyadarsh.features.auth.domain.AuthRepository
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthSession
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult<AuthSession> {
        // Validation
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error(
                Exception("Invalid input"),
                "Email and password cannot be empty"
            )
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthResult.Error(
                Exception("Invalid email"),
                "Please enter a valid email address"
            )
        }

        if (password.length < 6) {
            return AuthResult.Error(
                Exception("Invalid password"),
                "Password must be at least 6 characters"
            )
        }

        return authRepository.signIn(email, password)
    }
}

