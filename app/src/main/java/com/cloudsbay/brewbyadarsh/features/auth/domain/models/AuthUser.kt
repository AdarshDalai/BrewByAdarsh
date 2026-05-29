package com.cloudsbay.brewbyadarsh.features.auth.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(
    val id: String,
    val email: String?,
    val phone: String?,
    val emailConfirmedAt: String?,
    val phoneConfirmedAt: String?,
    val lastSignInAt: String?,
    val userMetadata: Map<String, String>? = null,
    val appMetadata: Map<String, String>? = null
)

