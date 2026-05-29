package com.cloudsbay.brewbyadarsh.features.auth.domain.usecases

import com.cloudsbay.brewbyadarsh.features.auth.domain.AuthRepository
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): AuthResult<Unit> {
        if (email.isBlank()) {
            return AuthResult.Error(
                Exception("Invalid input"),
                "Email cannot be empty"
            )
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthResult.Error(
                Exception("Invalid email"),
                "Please enter a valid email address"
            )
        }

        return authRepository.resetPassword(email)
    }
}

