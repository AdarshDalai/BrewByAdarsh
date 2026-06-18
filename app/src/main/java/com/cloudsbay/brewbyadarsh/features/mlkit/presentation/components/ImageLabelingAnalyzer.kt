package com.cloudsbay.brewbyadarsh.features.mlkit.presentation.components

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.Locale

class ImageLabelingAnalyzer(
    private val onLabelsDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    if (labels.isNotEmpty()) {
                        val result = labels.joinToString("\n") { label ->
                            "${label.text} (${String.format(Locale.US, "%.2f", label.confidence)})"
                        }
                        onLabelsDetected(result)
                    } else {
                        onLabelsDetected("Identifying objects...")
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
