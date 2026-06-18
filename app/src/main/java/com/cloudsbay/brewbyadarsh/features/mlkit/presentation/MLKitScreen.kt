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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cloudsbay.brewbyadarsh.features.mlkit.presentation.components.*

enum class MLKitMode {
    TEXT_RECOGNITION,
    BARCODE_SCANNING,
    FACE_DETECTION,
    IMAGE_LABELING,
    OBJECT_DETECTION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MLKitScreen(
    mode: MLKitMode,
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
                title = { 
                    Text(
                        when (mode) {
                            MLKitMode.TEXT_RECOGNITION -> "Text Recognition"
                            MLKitMode.BARCODE_SCANNING -> "Barcode Scanning"
                            MLKitMode.FACE_DETECTION -> "Face Detection"
                            MLKitMode.IMAGE_LABELING -> "Image Labeling"
                            MLKitMode.OBJECT_DETECTION -> "Object Detection"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<-")
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
                
                Column(modifier = Modifier.fillMaxSize()) {
                    val analyzer = remember(mode) {
                        when (mode) {
                            MLKitMode.TEXT_RECOGNITION -> TextRecognitionAnalyzer { detectedResult = it }
                            MLKitMode.BARCODE_SCANNING -> BarcodeScannerAnalyzer { detectedResult = it }
                            MLKitMode.FACE_DETECTION -> FaceDetectionAnalyzer { detectedResult = it }
                            MLKitMode.IMAGE_LABELING -> ImageLabelingAnalyzer { detectedResult = it }
                            MLKitMode.OBJECT_DETECTION -> ObjectDetectionAnalyzer { detectedResult = it }
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
                                text = "Detection Result:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = detectedResult.ifBlank { "Point camera at target..." },
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
