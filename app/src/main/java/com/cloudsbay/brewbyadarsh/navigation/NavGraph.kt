package com.cloudsbay.brewbyadarsh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cloudsbay.brewbyadarsh.features.auth.presentation.AuthViewModel
import com.cloudsbay.brewbyadarsh.features.auth.presentation.EmailVerificationScreen
import com.cloudsbay.brewbyadarsh.features.auth.presentation.ForgotPasswordScreen
import com.cloudsbay.brewbyadarsh.features.auth.presentation.LoginScreen
import com.cloudsbay.brewbyadarsh.features.auth.presentation.SignUpScreen
import com.cloudsbay.brewbyadarsh.features.home.presentation.HomeScreen
import com.cloudsbay.brewbyadarsh.features.mlkit.presentation.MLKitScreen
import kotlinx.serialization.Serializable
import androidx.navigation.toRoute

// Auth Routes
@Serializable
object Login

@Serializable
object SignUp

@Serializable
data class EmailVerification(val email: String)

@Serializable
object ForgotPassword

// App Routes
@Serializable
object Home

@Serializable
object MLKit

@Serializable
data class Details(val id: String)

@Composable
fun BrewNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    ) {
        composable<Login> {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onNavigateToEmailVerification = { email ->
                    navController.navigate(EmailVerification(email)) {
                        popUpTo(Login) { inclusive = false }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(SignUp)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(ForgotPassword)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<SignUp> {
            val authViewModel: AuthViewModel = hiltViewModel()
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate(Home) {
                        popUpTo(SignUp) { inclusive = true }
                    }
                },
                onNavigateToEmailVerification = { email ->
                    navController.navigate(EmailVerification(email)) {
                        popUpTo(SignUp) { inclusive = false }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable<EmailVerification> { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel()
            val route = backStackEntry.toRoute<EmailVerification>()
            val email = route.email
            EmailVerificationScreen(
                viewModel = authViewModel,
                userEmail = email,
                onVerificationComplete = {
                    navController.navigate(Home) {
                        popUpTo(EmailVerification::class) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Login) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }
        composable<ForgotPassword> {
            val authViewModel: AuthViewModel = hiltViewModel()
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable<Home> {
            val authViewModel: AuthViewModel = hiltViewModel()
            HomeScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Login) {
                        popUpTo(Home) { inclusive = true }
                    }
                },
                onNavigateToMLKit = {
                    navController.navigate(MLKit)
                }
            )
        }
        composable<MLKit> {
            MLKitScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<Details> {
            // Details Screen
        }
    }
}
