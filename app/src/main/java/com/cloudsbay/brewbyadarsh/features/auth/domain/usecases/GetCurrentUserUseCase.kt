package com.cloudsbay.brewbyadarsh.features.auth.domain.usecases

import com.cloudsbay.brewbyadarsh.features.auth.domain.AuthRepository
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthUser
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult<AuthUser?> {
        return authRepository.getCurrentUser()
    }
}

