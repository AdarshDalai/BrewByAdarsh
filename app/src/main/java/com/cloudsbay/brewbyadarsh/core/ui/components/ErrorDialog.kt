package com.cloudsbay.brewbyadarsh.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * Reusable Error Dialog Component for displaying errors and edge cases
 */
@Composable
fun ErrorDialog(
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit,
    confirmButtonText: String = "OK",
    dismissButtonText: String? = null,
    onConfirm: () -> Unit = onDismiss,
    isDismissible: Boolean = true
) {
    AlertDialog(
        onDismissRequest = {
            if (isDismissible) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmButtonText,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = if (dismissButtonText != null) {
            {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = dismissButtonText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            null
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Reusable Warning Dialog Component for displaying warnings
 */
@Composable
fun WarningDialog(
    title: String = "Warning",
    message: String,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Proceed",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    isDismissible: Boolean = true
) {
    AlertDialog(
        onDismissRequest = {
            if (isDismissible) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmButtonText,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = dismissButtonText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Reusable Info Dialog Component for displaying information
 */
@Composable
fun InfoDialog(
    title: String = "Information",
    message: String,
    onDismiss: () -> Unit,
    buttonText: String = "OK",
    isDismissible: Boolean = true
) {
    ErrorDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
        confirmButtonText = buttonText,
        isDismissible = isDismissible
    )
}

