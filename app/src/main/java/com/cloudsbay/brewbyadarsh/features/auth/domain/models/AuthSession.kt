package com.cloudsbay.brewbyadarsh.features.auth.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String?,
    val user: AuthUser,
    val expiresAt: Long?
)

