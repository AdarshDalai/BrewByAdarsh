package com.cloudsbay.brewbyadarsh.features.auth.data

import android.util.Log
import com.cloudsbay.brewbyadarsh.core.SessionManager
import com.cloudsbay.brewbyadarsh.features.auth.domain.AuthRepository
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthResult
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthSession
import com.cloudsbay.brewbyadarsh.features.auth.domain.models.AuthUser
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.cloudsbay.brewbyadarsh.core.errors.AuthAction
import com.cloudsbay.brewbyadarsh.core.errors.AuthErrorMapper

class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val sessionManager: SessionManager
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }


    override suspend fun getSession(): AuthResult<AuthSession?> = withContext(Dispatchers.IO) {
        try {
            tryCompletePendingSignOut()
            val session = sessionManager.authSession.first()
            AuthResult.Success(session)
        } catch (e: Exception) {
            val appError = AuthErrorMapper.map(e, AuthAction.GET_SESSION)
            AuthResult.Error(e, appError.message)
        }
    }

    override suspend fun signUp(email: String, password: String): AuthResult<AuthUser> =
        withContext(Dispatchers.IO) {
            try {
                tryCompletePendingSignOut()
                Log.d(TAG, "signUp: starting for $email")
                val signUpResult = auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Log.d(TAG, "signUp: result=$signUpResult")

                val user = signUpResult ?: auth.currentUserOrNull()
                val session = auth.currentSessionOrNull()
                Log.d(TAG, "signUp: user=$user, session=$session")

                if (session == null && user != null) {
                    // Sign up successful but email verification required
                    AuthResult.UserVerificationRequired("Please check your email to verify your account.")
                } else if (user != null) {
                    val authUser = AuthUser(
                        id = user.id,
                        email = user.email,
                        phone = user.phone,
                        emailConfirmedAt = user.emailConfirmedAt?.toString(),
                        phoneConfirmedAt = user.phoneConfirmedAt?.toString(),
                        lastSignInAt = user.lastSignInAt?.toString(),
                        userMetadata = user.userMetadata?.mapValues { it.value.toString() },
                        appMetadata = user.appMetadata?.mapValues { it.value.toString() }
                    )
                    AuthResult.Success(authUser)
                } else {
                    val appError = AuthErrorMapper.map(Exception("User not created"), AuthAction.SIGN_UP)
                    AuthResult.Error(Exception("Sign up failed"), appError.message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "signUp: error", e)
                val appError = AuthErrorMapper.map(e, AuthAction.SIGN_UP)
                AuthResult.Error(e, appError.message)
            }
        }

    override suspend fun signIn(email: String, password: String): AuthResult<AuthSession> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "signIn: starting for $email")
                tryCompletePendingSignOut()
                auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                Log.d(TAG, "signIn: authentication successful for $email")
                val supabaseSession = auth.currentSessionOrNull() ?: throw Exception("Session is null after sign in")

                val user = AuthUser(
                    id = supabaseSession.user?.id ?: "",
                    email = supabaseSession.user?.email,
                    phone = supabaseSession.user?.phone,
                    emailConfirmedAt = supabaseSession.user?.emailConfirmedAt?.toString(),
                    phoneConfirmedAt = supabaseSession.user?.phoneConfirmedAt?.toString(),
                    lastSignInAt = supabaseSession.user?.lastSignInAt?.toString(),
                    userMetadata = supabaseSession.user?.userMetadata?.mapValues { it.value.toString() },
                    appMetadata = supabaseSession.user?.appMetadata?.mapValues { it.value.toString() }
                )

                if (user.emailConfirmedAt == null) {
                    Log.d(TAG, "signIn: email not confirmed for $email")
                    return@withContext AuthResult.UserVerificationRequired(
                        "Please verify your email to continue."
                    )
                }

                val authSession = AuthSession(
                    accessToken = supabaseSession.accessToken,
                    refreshToken = supabaseSession.refreshToken,
                    user = user,
                    expiresAt = supabaseSession.expiresAt.toEpochMilliseconds()
                )

                sessionManager.saveSession(authSession)
                Log.d(TAG, "signIn: session saved successfully for $email")
                AuthResult.Success(authSession)
            } catch (e: Exception) {
                Log.e(TAG, "signIn: error", e)
                val errorMessage = (e.message ?: "").lowercase()
                if (errorMessage.contains("email not confirmed") || errorMessage.contains("email not verified")) {
                    Log.w(TAG, "signIn: email not confirmed for $email")
                    return@withContext AuthResult.UserVerificationRequired(
                        "Please verify your email to continue."
                    )
                }
                val appError = AuthErrorMapper.map(e, AuthAction.SIGN_IN)
                AuthResult.Error(e, appError.message)
            }
        }

    override suspend fun signInWithOAuth(provider: String): AuthResult<AuthSession> =
        withContext(Dispatchers.IO) {
            try {
                AuthResult.Error(
                    Exception("OAuth not yet configured"),
                    "OAuth sign in is not available yet. Please use email/password."
                )
            } catch (e: Exception) {
                val appError = AuthErrorMapper.map(e, AuthAction.SIGN_IN)
                AuthResult.Error(e, appError.message)
            }
        }

    override suspend fun signOut(): AuthResult<Unit> = withContext(Dispatchers.IO) {
        try {
            var remoteSucceeded = false
            try {
                auth.signOut()
                remoteSucceeded = true
            } catch (_: Exception) {
                // Remote logout can fail offline; local sign-out should still proceed.
            }
            if (!remoteSucceeded) {
                sessionManager.markPendingSignOut()
            } else {
                sessionManager.clearPendingSignOut()
            }
            sessionManager.clearSession()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val appError = AuthErrorMapper.map(e, AuthAction.SIGN_OUT)
            AuthResult.Error(e, appError.message)
        }
    }

    override suspend fun resetPassword(email: String): AuthResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                AuthResult.Success(Unit)
            } catch (e: Exception) {
                val appError = AuthErrorMapper.map(e, AuthAction.RESET_PASSWORD)
                AuthResult.Error(e, appError.message)
            }
        }

    override suspend fun updatePassword(password: String): AuthResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                AuthResult.Success(Unit)
            } catch (e: Exception) {
                val appError = AuthErrorMapper.map(e, AuthAction.UPDATE_PASSWORD)
                AuthResult.Error(e, appError.message)
            }
        }

    override suspend fun updateUserMetadata(metadata: Map<String, String>): AuthResult<AuthUser> =
        withContext(Dispatchers.IO) {
            try {
                val dummyUser = AuthUser(
                    id = "dummy-id",
                    email = null,
                    phone = null,
                    emailConfirmedAt = null,
                    phoneConfirmedAt = null,
                    lastSignInAt = null,
                    userMetadata = metadata,
                    appMetadata = null
                )
                AuthResult.Success(dummyUser)
            } catch (e: Exception) {
                val appError = AuthErrorMapper.map(e, AuthAction.UPDATE_PROFILE)
                AuthResult.Error(e, appError.message)
            }
        }

    override suspend fun getCurrentUser(): AuthResult<AuthUser?> = withContext(Dispatchers.IO) {
        try {
            val user = sessionManager.authSession.first()?.user
            AuthResult.Success(user)
        } catch (e: Exception) {
            val appError = AuthErrorMapper.map(e, AuthAction.GET_SESSION)
            AuthResult.Error(e, appError.message)
        }
    }

    override suspend fun refreshSession(): AuthResult<AuthSession> =
        withContext(Dispatchers.IO) {
            try {
                tryCompletePendingSignOut()
                val supabaseSession = auth.currentSessionOrNull()
                    ?: throw Exception("No active session")

                val user = AuthUser(
                    id = supabaseSession.user?.id ?: "",
                    email = supabaseSession.user?.email,
                    phone = supabaseSession.user?.phone,
                    emailConfirmedAt = supabaseSession.user?.emailConfirmedAt?.toString(),
                    phoneConfirmedAt = supabaseSession.user?.phoneConfirmedAt?.toString(),
                    lastSignInAt = supabaseSession.user?.lastSignInAt?.toString(),
                    userMetadata = supabaseSession.user?.userMetadata?.mapValues { it.value.toString() },
                    appMetadata = supabaseSession.user?.appMetadata?.mapValues { it.value.toString() }
                )

                val authSession = AuthSession(
                    accessToken = supabaseSession.accessToken,
                    refreshToken = supabaseSession.refreshToken,
                    user = user,
                    expiresAt = supabaseSession.expiresAt.toEpochMilliseconds()
                )

                sessionManager.saveSession(authSession)
                AuthResult.Success(authSession)
            } catch (e: Exception) {
                val appError = AuthErrorMapper.map(e, AuthAction.REFRESH_SESSION)
                AuthResult.Error(e, appError.message)
            }
        }

    override suspend fun verifyOtp(email: String, token: String, type: String): AuthResult<AuthSession> =
        withContext(Dispatchers.IO) {
            try {
                AuthResult.Error(
                    Exception("OTP verification not configured"),
                    "OTP verification is not available yet."
                )
            } catch (e: Exception) {
                val appError = AuthErrorMapper.map(e, AuthAction.VERIFY_OTP)
                AuthResult.Error(e, appError.message)
            }
        }

    override suspend fun resendVerificationEmail(email: String): AuthResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "resendVerificationEmail: starting for $email")
                auth.resendEmail(type = OtpType.Email.SIGNUP, email = email)
                Log.d(TAG, "resendVerificationEmail: success for $email")
                AuthResult.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "resendVerificationEmail: error", e)
                val appError = AuthErrorMapper.map(e, AuthAction.RESEND_VERIFICATION)
                AuthResult.Error(e, appError.message)
            }
        }

    override fun observeAuthState(): Flow<AuthUser?> {
        return sessionManager.authSession.map { it?.user }
    }

    override suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
        sessionManager.isLoggedIn.first()
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        sessionManager.authSession.first()?.accessToken
    }

    private suspend fun tryCompletePendingSignOut() {
        if (!sessionManager.isPendingSignOut()) return
        try {
            auth.signOut()
            sessionManager.clearPendingSignOut()
        } catch (_: Exception) {
            // Still offline or server unreachable; keep flag for later.
        }
    }
}
