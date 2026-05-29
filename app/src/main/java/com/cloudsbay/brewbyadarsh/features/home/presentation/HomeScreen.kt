package com.cloudsbay.brewbyadarsh.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cloudsbay.brewbyadarsh.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.flow.map

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = true)

    LaunchedEffect(isLoggedIn, uiState.isLoading) {
        if (!isLoggedIn && !uiState.isLoading) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Welcome to BrewByAdarsh",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                uiState.authUser?.let { user ->
                    Text(text = "Logged in as: ${user.email ?: "Unknown"}")
                    Text(text = "User ID: ${user.id}")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { authViewModel.signOut() }
                ) {
                    Text(text = "Sign Out")
                }
            }
        }
    }
}
