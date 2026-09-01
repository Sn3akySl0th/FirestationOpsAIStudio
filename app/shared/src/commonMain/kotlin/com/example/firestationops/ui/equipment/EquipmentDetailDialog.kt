package com.example.firestationops.ui.equipment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentStatus

/**
 * Dialog displaying detailed equipment metadata, status options, and maintenance notes editing.
 */
@Composable
fun EquipmentDetailDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onStatusSelected: (EquipmentStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf(equipment.status) }

    AlertDialog(
        modifier = modifier.testTag("equipment_detail_dialog"),
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = equipment.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                EquipmentCategoryBadge(category = equipment.category)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Selection
                Text(
                    text = "Operational Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EquipmentStatus.entries.forEach { status ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedStatus == status) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = status }
                                .testTag("eq_status_option_${status.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedStatus == status,
                                    onClick = { selectedStatus = status }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selectedStatus == status) FontWeight.Bold else FontWeight.Normal
                                    )
                                    val subText = when (status) {
                                        EquipmentStatus.IN_SERVICE -> "Inspected & ready for active deployment"
                                        EquipmentStatus.OUT_OF_SERVICE -> "Failed inspection, damaged, or unsafe"
                                        EquipmentStatus.MAINTENANCE_REQUIRED -> "Needs scheduled maintenance or repairs"
                                        EquipmentStatus.RESERVE -> "Standby backup in station storage"
                                        EquipmentStatus.RETIRED -> "Decommissioned or surplus"
                                    }
                                    Text(
                                        text = subText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Equipment Info
                Text(
                    text = "Equipment Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailRow("Equipment ID", equipment.id)
                    DetailRow("Department ID", equipment.departmentId)
                    equipment.stationId?.let { DetailRow("Station ID", it) }
                    equipment.apparatusId?.let { DetailRow("Apparatus Assigned", it) }
                    equipment.serialNumber?.let { DetailRow("Serial Number", it) }
                    equipment.barcode?.let { DetailRow("Barcode / Tag", it) }
                    equipment.assignedToFirefighterId?.let { DetailRow("Assigned Member", it) }
                }

                equipment.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Maintenance Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStatusSelected(selectedStatus)
                },
                modifier = Modifier.testTag("confirm_eq_status_button")
            ) {
                Text("Update Status")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_eq_dialog_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
