package com.example.firestationops.ui.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentStatus

/**
 * Rapid status update dialog displayed immediately upon scanning an equipment barcode or QR tag.
 * Designed for high-speed field audits and apparatus checks.
 */
@Composable
fun EquipmentQuickStatusDialog(
    equipment: Equipment,
    onStatusUpdated: (EquipmentStatus, String?, Boolean) -> Unit, // status, notes, scanNext
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf(equipment.status) }
    var notesText by remember { mutableStateOf(equipment.notes ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("equipment_quick_status_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Tag Match Badge & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✓", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                        Column {
                            Text(
                                text = "Tag Recognized",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Equipment Quick Status Update",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("quick_status_close_button")
                    ) {
                        Text("✕", style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Equipment Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = equipment.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = equipment.category.name.replace('_', ' '),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            equipment.barcode?.let { bc ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "🏷️ $bc",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (equipment.serialNumber != null || equipment.apparatusId != null) {
                            Text(
                                text = buildString {
                                    equipment.serialNumber?.let { append("S/N: $it  ") }
                                    equipment.apparatusId?.let { append("• Location: $it") }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Selection Options
                Text(
                    text = "Select Operational Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusOptionCard(
                        status = EquipmentStatus.IN_SERVICE,
                        label = "In Service",
                        subtitle = "Operational, inspected, and ready for response",
                        badgeColor = Color(0xFF2E7D32),
                        iconEmoji = "🟢",
                        isSelected = selectedStatus == EquipmentStatus.IN_SERVICE,
                        onClick = { selectedStatus = EquipmentStatus.IN_SERVICE },
                        tag = "status_option_in_service"
                    )

                    StatusOptionCard(
                        status = EquipmentStatus.MAINTENANCE_REQUIRED,
                        label = "Maintenance Required",
                        subtitle = "Needs service, calibration, or non-critical repair",
                        badgeColor = Color(0xFFF57C00),
                        iconEmoji = "🟡",
                        isSelected = selectedStatus == EquipmentStatus.MAINTENANCE_REQUIRED,
                        onClick = { selectedStatus = EquipmentStatus.MAINTENANCE_REQUIRED },
                        tag = "status_option_maintenance_required"
                    )

                    StatusOptionCard(
                        status = EquipmentStatus.OUT_OF_SERVICE,
                        label = "Out of Service",
                        subtitle = "Critical failure, damaged, or unsafe for field use",
                        badgeColor = Color(0xFFD32F2F),
                        iconEmoji = "🔴",
                        isSelected = selectedStatus == EquipmentStatus.OUT_OF_SERVICE,
                        onClick = { selectedStatus = EquipmentStatus.OUT_OF_SERVICE },
                        tag = "status_option_out_of_service"
                    )

                    StatusOptionCard(
                        status = EquipmentStatus.RESERVE,
                        label = "Reserve / Backup",
                        subtitle = "Stored in station inventory as reserve unit",
                        badgeColor = Color(0xFF1976D2),
                        iconEmoji = "🔵",
                        isSelected = selectedStatus == EquipmentStatus.RESERVE,
                        onClick = { selectedStatus = EquipmentStatus.RESERVE },
                        tag = "status_option_reserve"
                    )
                }

                // Status Note Field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Status / Maintenance Notes (Optional)") },
                    placeholder = { Text("e.g., Hydro test completed, air leak noted on valve, etc.") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("status_note_input")
                )

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onStatusUpdated(
                                selectedStatus,
                                notesText.takeIf { it.isNotBlank() },
                                false
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_status_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedStatus) {
                                EquipmentStatus.IN_SERVICE -> Color(0xFF2E7D32)
                                EquipmentStatus.MAINTENANCE_REQUIRED -> Color(0xFFE65100)
                                EquipmentStatus.OUT_OF_SERVICE -> Color(0xFFC62828)
                                EquipmentStatus.RESERVE -> MaterialTheme.colorScheme.primary
                                EquipmentStatus.RETIRED -> Color(0xFF757575)
                            }
                        )
                    ) {
                        Text(
                            text = "Save Status (${selectedStatus.name.replace('_', ' ')})",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            onStatusUpdated(
                                selectedStatus,
                                notesText.takeIf { it.isNotBlank() },
                                true
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_and_scan_next_button")
                    ) {
                        Text("Save & Scan Next Tag 📷")
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cancel_quick_status_button")
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusOptionCard(
    status: EquipmentStatus,
    label: String,
    subtitle: String,
    badgeColor: Color,
    iconEmoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) badgeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) badgeColor else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = iconEmoji, style = MaterialTheme.typography.titleLarge)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = badgeColor,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
