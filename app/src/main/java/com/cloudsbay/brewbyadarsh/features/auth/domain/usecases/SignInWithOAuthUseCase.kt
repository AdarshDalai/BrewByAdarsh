package com.cloudsbay.brewbyadarsh.features.auth.domain.usecases

import com.cloudsbay.brewbyadarsh.features.auth.domain.AuthRepository
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthSession
import javax.inject.Inject

class SignInWithOAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(provider: String): AuthResult<AuthSession> {
        val validProviders = listOf("google", "github", "apple")

        if (provider.isBlank()) {
            return AuthResult.Error(
                Exception("Invalid provider"),
                "Provider cannot be empty"
            )
        }

        if (!validProviders.contains(provider.lowercase())) {
            return AuthResult.Error(
                Exception("Invalid provider"),
                "Provider must be one of: ${validProviders.joinToString(", ")}"
            )
        }

        return authRepository.signInWithOAuth(provider)
    }
}

