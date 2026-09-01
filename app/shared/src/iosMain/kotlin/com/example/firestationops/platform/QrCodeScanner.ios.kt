package com.example.firestationops.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class IosQrCodeScanner : QrCodeScanner {
    override val isCameraSupported: Boolean = false

    @Composable
    override fun RegisterPermissionHandler(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
    }

    override fun requestCameraPermission() {
    }

    override fun hasCameraPermission(): Boolean = true

    @Composable
    override fun CameraScannerPreview(
        onBarcodeDetected: (String) -> Unit,
        isTorchEnabled: Boolean,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera scanner unavailable on this platform.\nPlease use manual tag entry.",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
actual fun rememberQrCodeScanner(): QrCodeScanner {
    return remember { IosQrCodeScanner() }
}
