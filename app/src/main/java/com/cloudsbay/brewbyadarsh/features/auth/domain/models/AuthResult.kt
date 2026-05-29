package com.cloudsbay.brewbyadarsh.features.auth.domain.models

sealed class AuthResult<T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val exception: Exception, val message: String) : AuthResult<T>()
    class Loading<T> : AuthResult<T>()
    data class UserVerificationRequired<T>(val message: String) : AuthResult<T>()
}

