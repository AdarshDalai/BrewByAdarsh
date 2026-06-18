package com.cloudsbay.brewbyadarsh.features.mlkit.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cloudsbay.brewbyadarsh.features.mlkit.presentation.components.BarcodeScannerAnalyzer
import com.cloudsbay.brewbyadarsh.features.mlkit.presentation.components.CameraPreview
import com.cloudsbay.brewbyadarsh.features.mlkit.presentation.components.TextRecognitionAnalyzer

enum class MLKitMode {
    TEXT_RECOGNITION,
    BARCODE_SCANNING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MLKitScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var mode by remember { mutableStateOf(MLKitMode.TEXT_RECOGNITION) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ML Kit Demo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<-")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        mode = if (mode == MLKitMode.TEXT_RECOGNITION) MLKitMode.BARCODE_SCANNING
                        else MLKitMode.TEXT_RECOGNITION
                    }) {
                        Text(if (mode == MLKitMode.TEXT_RECOGNITION) "Switch to Barcode" else "Switch to Text")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                var detectedResult by remember { mutableStateOf("") }
                
                // Clear result when switching mode
                LaunchedEffect(mode) {
                    detectedResult = ""
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    val analyzer = remember(mode) {
                        if (mode == MLKitMode.TEXT_RECOGNITION) {
                            TextRecognitionAnalyzer { detectedResult = it }
                        } else {
                            BarcodeScannerAnalyzer { detectedResult = it }
                        }
                    }

                    CameraPreview(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        analyzer = analyzer
                    )
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = if (mode == MLKitMode.TEXT_RECOGNITION) "Detected Text:" else "Detected Barcode:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = detectedResult.ifBlank { "Point camera at ${if (mode == MLKitMode.TEXT_RECOGNITION) "text" else "barcode"}..." },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission required")
                }
            }
        }
    }
}
