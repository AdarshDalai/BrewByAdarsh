package com.cloudsbay.brewbyadarsh.features.mlkit.presentation.components

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

import java.util.Locale

class FaceDetectionAnalyzer(
    private val onFaceDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val result = faces.joinToString("\n") { face ->
                            val id = face.trackingId ?: "Unknown"
                            val smileProb = face.smilingProbability?.let { String.format(Locale.US, "%.2f", it) } ?: "N/A"
                            "Face ID: $id\nSmiling: $smileProb"
                        }
                        onFaceDetected(result)
                    } else {
                        onFaceDetected("No faces detected")
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
