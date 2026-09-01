package com.example.firestationops.ui.equipment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentStatus

/**
 * List item card displaying a single equipment unit according to the data schema:
 * - Equipment Name & Category badge
 * - Operational Status Badge
 * - Apparatus / Station Assignment
 * - Serial Number & Barcode
 * - Maintenance Needs & Notes (highlighted for maintenance/OOS items)
 * - Last Inspection & Expiration indicators
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EquipmentListItem(
    equipment: Equipment,
    onItemClick: () -> Unit,
    onChangeStatusClick: () -> Unit,
    onShowQrClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val needsAttention = equipment.status.requiresAttention

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .testTag("equipment_card_${equipment.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (needsAttention) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Name, Category, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("equipment_name_${equipment.id}")
                    )

                    EquipmentCategoryBadge(
                        category = equipment.category,
                        modifier = Modifier.testTag("eq_category_${equipment.id}")
                    )
                }

                EquipmentStatusBadge(
                    status = equipment.status,
                    onClick = onChangeStatusClick,
                    modifier = Modifier.testTag("status_badge_${equipment.id}")
                )
            }

            // Location & Assignment details
            val locationDetails = buildList {
                equipment.apparatusId?.let { add("Apparatus: $it") }
                equipment.stationId?.let { add("Station: $it") }
                equipment.assignedToFirefighterId?.let { add("Assigned to Member #$it") }
            }.joinToString(" • ")

            if (locationDetails.isNotEmpty()) {
                Text(
                    text = locationDetails,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tracking Metadata (Serial Number & Barcode)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                equipment.serialNumber?.takeIf { it.isNotBlank() }?.let { sn ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = "S/N: $sn",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                equipment.barcode?.takeIf { it.isNotBlank() }?.let { bc ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = "Barcode: $bc",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Maintenance Needs & Notes Section
            equipment.notes?.takeIf { it.isNotBlank() }?.let { noteText ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (needsAttention) {
                        Color(0xFFFFF3E0)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("eq_notes_${equipment.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (needsAttention) "⚠️ Maintenance Required" else "Notes / Inspection Info",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (needsAttention) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = noteText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Action row & readiness indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (equipment.status) {
                        EquipmentStatus.IN_SERVICE -> "✓ Operational & In-Service"
                        EquipmentStatus.OUT_OF_SERVICE -> "✕ Out of Service - Unsafe"
                        EquipmentStatus.MAINTENANCE_REQUIRED -> "⚠️ Servicing Needed"
                        EquipmentStatus.RESERVE -> "Standby Reserve"
                        EquipmentStatus.RETIRED -> "Decommissioned"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = when (equipment.status) {
                        EquipmentStatus.IN_SERVICE -> Color(0xFF2E7D32)
                        EquipmentStatus.OUT_OF_SERVICE -> Color(0xFFC62828)
                        EquipmentStatus.MAINTENANCE_REQUIRED -> Color(0xFFE65100)
                        EquipmentStatus.RESERVE -> Color(0xFF1565C0)
                        EquipmentStatus.RETIRED -> Color(0xFF757575)
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    onShowQrClick?.let { onQr ->
                        OutlinedButton(
                            onClick = onQr,
                            modifier = Modifier.testTag("show_qr_btn_${equipment.id}")
                        ) {
                            Text("QR Tag", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    OutlinedButton(
                        onClick = onChangeStatusClick,
                        modifier = Modifier.testTag("change_eq_status_btn_${equipment.id}")
                    ) {
                        Text("Change Status", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
