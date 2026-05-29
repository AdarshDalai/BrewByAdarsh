package com.cloudsbay.brewbyadarsh.core.errors

data class AppError(
    val title: String,
    val message: String,
    val isRetryable: Boolean = true
)

