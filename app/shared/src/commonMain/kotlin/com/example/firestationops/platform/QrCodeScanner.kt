package com.example.firestationops.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform abstraction for camera hardware access and QR / Barcode scanning.
 */
interface QrCodeScanner {
    /**
     * Whether the current platform supports camera hardware for scanning.
     */
    val isCameraSupported: Boolean

    /**
     * Composable that handles requesting camera runtime permissions on platforms where required (e.g. Android).
     */
    @Composable
    fun RegisterPermissionHandler(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    )

    /**
     * Triggers the platform permission request flow.
     */
    fun requestCameraPermission()

    /**
     * Checks if camera permission is already granted.
     */
    fun hasCameraPermission(): Boolean

    /**
     * Live camera viewfinder and barcode analyzer composable.
     */
    @Composable
    fun CameraScannerPreview(
        onBarcodeDetected: (String) -> Unit,
        isTorchEnabled: Boolean,
        modifier: Modifier
    )
}

/**
 * Remembers a platform-specific instance of [QrCodeScanner].
 */
@Composable
expect fun rememberQrCodeScanner(): QrCodeScanner
