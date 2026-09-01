package com.example.firestationops.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class AndroidQrCodeScanner(private val context: Context) : QrCodeScanner {

    override val isCameraSupported: Boolean = true

    private var permissionLauncher: ManagedActivityResultLauncher<String, Boolean>? = null
    private var onGrantedCallback: (() -> Unit)? = null
    private var onDeniedCallback: (() -> Unit)? = null

    @Composable
    override fun RegisterPermissionHandler(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        onGrantedCallback = onPermissionGranted
        onDeniedCallback = onPermissionDenied

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGrantedCallback?.invoke()
            } else {
                onDeniedCallback?.invoke()
            }
        }
        permissionLauncher = launcher
    }

    override fun requestCameraPermission() {
        permissionLauncher?.launch(Manifest.permission.CAMERA)
    }

    override fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    @OptIn(ExperimentalGetImage::class)
    @Composable
    override fun CameraScannerPreview(
        onBarcodeDetected: (String) -> Unit,
        isTorchEnabled: Boolean,
        modifier: Modifier
    ) {
        val currentContext = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var cameraInstance by remember { mutableStateOf<Camera?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Update torch status whenever isTorchEnabled or cameraInstance changes
        LaunchedEffect(isTorchEnabled, cameraInstance) {
            val cam = cameraInstance
            if (cam != null && cam.cameraInfo.hasFlashUnit()) {
                runCatching {
                    cam.cameraControl.enableTorch(isTorchEnabled)
                }
            }
        }

        Box(modifier = modifier) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                    Barcode.FORMAT_QR_CODE,
                                    Barcode.FORMAT_CODE_128,
                                    Barcode.FORMAT_CODE_39,
                                    Barcode.FORMAT_EAN_13,
                                    Barcode.FORMAT_EAN_8,
                                    Barcode.FORMAT_UPC_A,
                                    Barcode.FORMAT_UPC_E,
                                    Barcode.FORMAT_DATA_MATRIX,
                                    Barcode.FORMAT_PDF417
                                )
                                .build()

                            val scanner = BarcodeScanning.getClient(barcodeScannerOptions)

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            var lastScannedCode = ""
                            var lastScannedTimestamp = 0L

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    scanner.process(inputImage)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                val rawValue = barcode.rawValue
                                                val now = System.currentTimeMillis()
                                                if (!rawValue.isNullOrBlank() && (rawValue != lastScannedCode || now - lastScannedTimestamp > 2000)) {
                                                    lastScannedCode = rawValue
                                                    lastScannedTimestamp = now
                                                    onBarcodeDetected(rawValue)
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("QrCodeScanner", "Barcode scan failure", e)
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            cameraProvider.unbindAll()
                            val boundCamera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraInstance = boundCamera

                        } catch (exc: Exception) {
                            Log.e("QrCodeScanner", "Camera initialization failed", exc)
                            errorMessage = "Camera error: ${exc.localizedMessage}"
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                try {
                    val cameraProvider = ProcessCameraProvider.getInstance(currentContext).get()
                    cameraProvider.unbindAll()
                } catch (_: Exception) {
                }
            }
        }
    }
}

@Composable
actual fun rememberQrCodeScanner(): QrCodeScanner {
    val context = LocalContext.current
    return remember { AndroidQrCodeScanner(context.applicationContext) }
}
