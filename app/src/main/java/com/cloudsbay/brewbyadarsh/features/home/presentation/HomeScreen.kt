package com.cloudsbay.brewbyadarsh.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    onNavigateToTextRecognition: () -> Unit,
    onNavigateToBarcodeScanning: () -> Unit,
    onNavigateToFaceDetection: () -> Unit,
    onNavigateToImageLabeling: () -> Unit,
    onNavigateToObjectDetection: () -> Unit,
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "BrewByAdarsh ML Demo",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                uiState.authUser?.let { user ->
                    Text(text = "Logged in as: ${user.email ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                MLFeatureButton("Text Recognition", onNavigateToTextRecognition)
                MLFeatureButton("Barcode Scanning", onNavigateToBarcodeScanning)
                MLFeatureButton("Face Detection", onNavigateToFaceDetection)
                MLFeatureButton("Image Labeling", onNavigateToImageLabeling)
                MLFeatureButton("Object Detection", onNavigateToObjectDetection)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { authViewModel.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Sign Out")
                }
            }
        }
    }
}

@Composable
fun MLFeatureButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text)
    }
}
