package com.cloudsbay.brewbyadarsh.features.mlkit.presentation.components

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.Locale

class ObjectDetectionAnalyzer(
    private val onObjectsDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .build()

    private val detector = ObjectDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    if (detectedObjects.isNotEmpty()) {
                        val result = detectedObjects.joinToString("\n") { obj ->
                            val category = obj.labels.firstOrNull()?.text ?: "Unknown"
                            val confidence = obj.labels.firstOrNull()?.confidence?.let { String.format(Locale.US, "%.2f", it) } ?: "N/A"
                            "Object: $category ($confidence)"
                        }
                        onObjectsDetected(result)
                    } else {
                        onObjectsDetected("Searching for objects...")
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
