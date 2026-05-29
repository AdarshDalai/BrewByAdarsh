package com.cloudsbay.brewbyadarsh.core.errors

enum class AuthAction {
    SIGN_IN,
    SIGN_UP,
    SIGN_OUT,
    RESET_PASSWORD,
    UPDATE_PASSWORD,
    UPDATE_PROFILE,
    GET_SESSION,
    REFRESH_SESSION,
    VERIFY_OTP,
    RESEND_VERIFICATION
}

object AuthErrorMapper {
    fun map(throwable: Throwable, action: AuthAction): AppError {
        val message = (throwable.message ?: "").lowercase()

        return when {
            message.contains("unable to resolve host") || message.contains("unknownhost") -> {
                AppError(
                    title = titleFor(action),
                    message = "We can't reach the server right now. Check your internet and try again.",
                    isRetryable = true
                )
            }
            message.contains("timeout") || message.contains("timed out") -> {
                AppError(
                    title = titleFor(action),
                    message = "The connection is taking too long. Please try again in a moment.",
                    isRetryable = true
                )
            }
            message.contains("network") || message.contains("connection") -> {
                AppError(
                    title = titleFor(action),
                    message = "Network error. Please check your connection and try again.",
                    isRetryable = true
                )
            }
            message.contains("invalid login credentials") || message.contains("invalid_grant") -> {
                AppError(
                    title = titleFor(action),
                    message = "Email or password is incorrect. Please try again.",
                    isRetryable = true
                )
            }
            message.contains("email") && message.contains("already") -> {
                AppError(
                    title = titleFor(action),
                    message = "This email is already in use. Try signing in instead.",
                    isRetryable = false
                )
            }
            message.contains("user not found") || message.contains("not found") -> {
                AppError(
                    title = titleFor(action),
                    message = "We couldn't find an account with this email.",
                    isRetryable = false
                )
            }
            message.contains("session is null") -> {
                AppError(
                    title = titleFor(action),
                    message = "We couldn't finish signing in. Please try again.",
                    isRetryable = true
                )
            }
            else -> {
                AppError(
                    title = titleFor(action),
                    message = genericMessageFor(action),
                    isRetryable = true
                )
            }
        }
    }

    private fun titleFor(action: AuthAction): String = when (action) {
        AuthAction.SIGN_IN -> "Sign In Failed"
        AuthAction.SIGN_UP -> "Sign Up Failed"
        AuthAction.SIGN_OUT -> "Sign Out Failed"
        AuthAction.RESET_PASSWORD -> "Password Reset Failed"
        AuthAction.UPDATE_PASSWORD -> "Password Update Failed"
        AuthAction.UPDATE_PROFILE -> "Update Failed"
        AuthAction.GET_SESSION -> "Session Error"
        AuthAction.REFRESH_SESSION -> "Session Error"
        AuthAction.VERIFY_OTP -> "Verification Failed"
        AuthAction.RESEND_VERIFICATION -> "Verification Failed"
    }

    private fun genericMessageFor(action: AuthAction): String = when (action) {
        AuthAction.SIGN_IN -> "We couldn't sign you in right now. Please try again."
        AuthAction.SIGN_UP -> "We couldn't create your account right now. Please try again."
        AuthAction.SIGN_OUT -> "We couldn't sign you out right now. Please try again."
        AuthAction.RESET_PASSWORD -> "We couldn't send the reset email. Please try again."
        AuthAction.UPDATE_PASSWORD -> "We couldn't update your password. Please try again."
        AuthAction.UPDATE_PROFILE -> "We couldn't update your profile. Please try again."
        AuthAction.GET_SESSION -> "We couldn't access your session. Please try again."
        AuthAction.REFRESH_SESSION -> "We couldn't refresh your session. Please try again."
        AuthAction.VERIFY_OTP -> "We couldn't verify the code. Please try again."
        AuthAction.RESEND_VERIFICATION -> "We couldn't resend the verification email. Please try again."
    }
}

