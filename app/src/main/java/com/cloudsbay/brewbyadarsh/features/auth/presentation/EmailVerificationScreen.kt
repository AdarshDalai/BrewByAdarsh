package com.cloudsbay.brewbyadarsh.features.auth.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.cloudsbay.brewbyadarsh.core.ui.components.ErrorDialog
import com.cloudsbay.brewbyadarsh.ui.theme.BrewByAdarshTheme

@Composable
fun EmailVerificationScreen(
    viewModel: AuthViewModel,
    userEmail: String,
    onVerificationComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Periodic background check
    LaunchedEffect(Unit) {
        while (isActive && !uiState.isAuthenticated) {
            viewModel.checkEmailVerification(isManual = false)
            delay(4000)
        }
    }

    // Navigate to Home when verified and authenticated
    LaunchedEffect(uiState.authUser, uiState.isAuthenticated) {
        if (uiState.authUser != null && uiState.isAuthenticated) {
            onVerificationComplete()
        }
    }

    // Show snackbar when there's a message (e.g. "Email is still not verified")
    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Intercept back button: check verification first
    val onBackPressed: () -> Unit = {
        viewModel.checkEmailVerification(isManual = true)
        // If password is not available (e.g. app was backgrounded), show a snackbar
        if (viewModel.signUpPassword == null && uiState.unverifiedUserEmail != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Please use 'Go to Sign In' to log in with your verified email."
                )
            }
        }
    }

    BackHandler { onBackPressed() }

    EmailVerificationContent(
        userEmail = userEmail,
        isLoading = uiState.isLoading,
        error = uiState.error,
        snackbarHostState = snackbarHostState,
        onRetry = { viewModel.clearError() },
        onNavigateBack = { onBackPressed() },
        onNavigateToLogin = onNavigateToLogin,
        onManualCheck = { viewModel.checkEmailVerification(isManual = true) },
        onResendEmail = viewModel::resendVerificationEmail
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmailVerificationContent(
    userEmail: String,
    isLoading: Boolean = false,
    error: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onManualCheck: () -> Unit = {},
    onResendEmail: () -> Unit = {}
) {
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        if (error != null) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog && error != null) {
        ErrorDialog(
            title = "Verification Error",
            message = error,
            onDismiss = {
                showErrorDialog = false
                onRetry()
            },
            confirmButtonText = "Try Again"
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(200.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialShapes.Cookie7Sided.toShape()
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Email Icon
                Icon(
                    imageVector = Icons.Filled.MailOutline,
                    contentDescription = "Email Verification",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "Verify your email",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle
                Text(
                    text = "Please check your email",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Address
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Instructions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "What's next?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    VerificationStep(number = "1", description = "Check the verification email")
                    VerificationStep(number = "2", description = "Click the link in the email")
                    VerificationStep(number = "3", description = "Return to complete setup")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Note about spam folder
                Text(
                    text = "\uD83D\uDCA1 Tip: Don't see it? Check your spam or junk folder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onManualCheck,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isLoading) {
                            Text("Checking...")
                        } else {
                            Text("I've Verified My Email", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    TextButton(
                        onClick = onResendEmail,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(
                            "Resend Verification Email",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Go to Sign In",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun VerificationStep(number: String, description: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterStart),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "$number. $description",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmailVerificationScreenPreview() {
    BrewByAdarshTheme {
        EmailVerificationContent(
            userEmail = "user@example.com",
            isLoading = false,
            error = null,
            onNavigateToLogin = {},
            onManualCheck = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmailVerificationScreenErrorPreview() {
    BrewByAdarshTheme {
        EmailVerificationContent(
            userEmail = "user@example.com",
            isLoading = false,
            error = "Failed to verify email. Please try again.",
            onNavigateToLogin = {},
            onManualCheck = {}
        )
    }
}
