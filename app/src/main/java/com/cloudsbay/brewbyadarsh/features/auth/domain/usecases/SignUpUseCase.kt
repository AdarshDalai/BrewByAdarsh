package com.cloudsbay.brewbyadarsh.features.auth.domain.usecases

import com.cloudsbay.brewbyadarsh.features.auth.domain.AuthRepository
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthUser
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, confirmPassword: String): AuthResult<AuthUser> {
        // Validation
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            return AuthResult.Error(
                Exception("Invalid input"),
                "All fields are required"
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

        if (password != confirmPassword) {
            return AuthResult.Error(
                Exception("Password mismatch"),
                "Passwords do not match"
            )
        }

        return authRepository.signUp(email, password)
    }
}

