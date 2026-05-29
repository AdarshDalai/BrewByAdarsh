package com.cloudsbay.brewbyadarsh.features.auth.domain.usecases

import com.cloudsbay.brewbyadarsh.features.auth.domain.AuthRepository
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthSession
import javax.inject.Inject

class GetCurrentSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult<AuthSession?> {
        return authRepository.getSession()
    }
}

