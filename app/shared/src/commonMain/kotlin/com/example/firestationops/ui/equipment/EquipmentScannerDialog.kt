package com.example.firestationops.ui.equipment

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.firestationops.model.Equipment
import com.example.firestationops.platform.rememberQrCodeScanner

/**
 * Fullscreen or modal dialog providing live camera QR/Barcode scanning and manual tag entry
 * to quickly locate and update equipment records in the field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScannerDialog(
    equipmentList: List<Equipment>,
    onTagScanned: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scanner = rememberQrCodeScanner()
    var isTorchOn by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(if (scanner.isCameraSupported) 0 else 1) }
    var manualTagInput by remember { mutableStateOf("") }
    var hasCameraPermission by remember { mutableStateOf(scanner.hasCameraPermission()) }

    scanner.RegisterPermissionHandler(
        onPermissionGranted = { hasCameraPermission = true },
        onPermissionDenied = { hasCameraPermission = false }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("equipment_scanner_dialog"),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Scan Equipment Tag",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "QR Codes • Barcodes • NFC/Tags",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("scanner_close_button")
                        ) {
                            Text(
                                text = "✕",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        if (scanner.isCameraSupported && selectedTab == 0 && hasCameraPermission) {
                            IconButton(
                                onClick = { isTorchOn = !isTorchOn },
                                colors = if (isTorchOn) {
                                    IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    IconButtonDefaults.iconButtonColors()
                                },
                                modifier = Modifier.testTag("scanner_torch_button")
                            ) {
                                Text(
                                    text = if (isTorchOn) "🔦 On" else "🔦 Off",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Tab Selection: Camera vs Manual
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📷 Camera Scanner") },
                        modifier = Modifier.testTag("tab_camera_scanner")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("⌨️ Manual / Preset") },
                        modifier = Modifier.testTag("tab_manual_scanner")
                    )
                }

                if (selectedTab == 0) {
                    // Camera Scanner View
                    if (!scanner.isCameraSupported) {
                        NoCameraAvailableView(onSwitchToManual = { selectedTab = 1 })
                    } else if (!hasCameraPermission) {
                        CameraPermissionRationaleView(
                            onRequestPermission = { scanner.requestCameraPermission() },
                            onSwitchToManual = { selectedTab = 1 }
                        )
                    } else {
                        CameraViewfinderView(
                            scanner = scanner,
                            isTorchOn = isTorchOn,
                            onBarcodeDetected = onTagScanned,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Manual Tag Input & Test Preset View
                    ManualTagInputView(
                        equipmentList = equipmentList,
                        tagInput = manualTagInput,
                        onTagInputChange = { manualTagInput = it },
                        onSubmit = {
                            if (manualTagInput.isNotBlank()) {
                                onTagScanned(manualTagInput.trim())
                            }
                        },
                        onPresetClick = { tag ->
                            manualTagInput = tag
                            onTagScanned(tag)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Live camera preview with animated scanning laser reticle and dark backdrop.
 */
@Composable
private fun CameraViewfinderView(
    scanner: com.example.firestationops.platform.QrCodeScanner,
    isTorchOn: Boolean,
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_viewfinder_container")
    ) {
        // Camera Preview Feed
        scanner.CameraScannerPreview(
            onBarcodeDetected = onBarcodeDetected,
            isTorchEnabled = isTorchOn,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay with Target Frame & Corner Reticles
        val boxWidth = maxWidth
        val boxHeight = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val frameSize = minOf(size.width * 0.75f, size.height * 0.55f, 320.dp.toPx())
            val left = (size.width - frameSize) / 2f
            val top = (size.height - frameSize) / 2f
            val right = left + frameSize
            val bottom = top + frameSize

            // Dark semi-transparent scrim around frame
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )

            // Clear cutout for scanner box
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(frameSize, frameSize),
                cornerRadius = CornerRadius(16.dp.toPx()),
                blendMode = BlendMode.Clear
            )

            // Scanning Target Border
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(left, top),
                size = Size(frameSize, frameSize),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Corner Accents (Red/Orange Tactical Style)
            val cornerLength = 28.dp.toPx()
            val strokeWidth = 5.dp.toPx()
            val accentColor = Color(0xFFFF5722)

            // Top Left
            drawLine(accentColor, Offset(left - 2, top), Offset(left + cornerLength, top), strokeWidth)
            drawLine(accentColor, Offset(left, top - 2), Offset(left, top + cornerLength), strokeWidth)

            // Top Right
            drawLine(accentColor, Offset(right + 2, top), Offset(right - cornerLength, top), strokeWidth)
            drawLine(accentColor, Offset(right, top - 2), Offset(right, top + cornerLength), strokeWidth)

            // Bottom Left
            drawLine(accentColor, Offset(left - 2, bottom), Offset(left + cornerLength, bottom), strokeWidth)
            drawLine(accentColor, Offset(left, bottom + 2), Offset(left, bottom - cornerLength), strokeWidth)

            // Bottom Right
            drawLine(accentColor, Offset(right + 2, bottom), Offset(right - cornerLength, bottom), strokeWidth)
            drawLine(accentColor, Offset(right, bottom + 2), Offset(right, bottom - cornerLength), strokeWidth)

            // Animated Laser Beam
            val currentLaserY = top + (frameSize * laserPosition)
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0x00FF3D00),
                        Color(0xFFFF3D00),
                        Color(0xFFFFEA00),
                        Color(0xFFFF3D00),
                        Color(0x00FF3D00)
                    ),
                    startX = left,
                    endX = right
                ),
                start = Offset(left + 8.dp.toPx(), currentLaserY),
                end = Offset(right - 8.dp.toPx(), currentLaserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Instruction Pill Badge at Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Align barcode or QR tag within the frame",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Screen shown when camera hardware is not supported on the target platform.
 */
@Composable
private fun NoCameraAvailableView(onSwitchToManual: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📷",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "Camera Scanning Unavailable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live camera scanning is supported on mobile devices. Use manual entry or preset tags to lookup equipment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onSwitchToManual,
                    modifier = Modifier.testTag("switch_manual_entry_button")
                ) {
                    Text("Enter Tag Manually")
                }
            }
        }
    }
}

/**
 * Screen requesting camera hardware permissions with clear volunteer fire department rationale.
 */
@Composable
private fun CameraPermissionRationaleView(
    onRequestPermission: () -> Unit,
    onSwitchToManual: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "📸", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "FirestationOps uses the camera to scan barcode and QR tags on apparatus gear, SCBA packs, and station tools for instant field status updates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grant_camera_permission_button")
                ) {
                    Text("Grant Camera Access")
                }
                OutlinedButton(
                    onClick = onSwitchToManual,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use Manual Tag Entry")
                }
            }
        }
    }
}

/**
 * Manual tag input and preset sample tag picker view.
 */
@Composable
private fun ManualTagInputView(
    equipmentList: List<Equipment>,
    tagInput: String,
    onTagInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onPresetClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter Equipment Tag / Barcode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Type or paste the equipment serial number, barcode, ID, or QR payload.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = onTagInputChange,
                    label = { Text("Equipment Tag / Barcode / S/N") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_tag_input_field")
                )
                Button(
                    onClick = onSubmit,
                    enabled = tagInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_manual_tag_button")
                ) {
                    Text("Lookup & Update Status")
                }
            }
        }

        // Quick Preset Tags from current inventory
        if (equipmentList.isNotEmpty()) {
            Text(
                text = "Quick Test Presets (Station Inventory)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any tag below to simulate a live QR/barcode scan:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                equipmentList.take(6).forEach { eq ->
                    val tagToUse = eq.barcode ?: eq.serialNumber ?: eq.id
                    Card(
                        onClick = { onPresetClick(tagToUse) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preset_tag_${eq.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = eq.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tag: $tagToUse • ${eq.category.name.replace('_', ' ')}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FilledTonalButton(
                                onClick = { onPresetClick(tagToUse) }
                            ) {
                                Text("Scan")
                            }
                        }
                    }
                }
            }
        }
    }
}
