package com.cloudsbay.brewbyadarsh.features.auth.domain.usecases

import com.cloudsbay.brewbyadarsh.features.auth.domain.AuthRepository
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import javax.inject.Inject

class ResendVerificationEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): AuthResult<Unit> {
        if (email.isBlank()) {
            return AuthResult.Error(
                Exception("Invalid email"),
                "Email address is required"
            )
        }
        return authRepository.resendVerificationEmail(email)
    }
}
