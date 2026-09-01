package com.example.firestationops.ui.personnel

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
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus

/**
 * Dialog displaying detailed firefighter profile and operational status change options.
 */
@Composable
fun FirefighterDetailDialog(
    firefighter: Firefighter,
    onDismiss: () -> Unit,
    onStatusSelected: (PersonnelStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf(firefighter.status) }

    AlertDialog(
        modifier = modifier.testTag("firefighter_detail_dialog"),
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = firefighter.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = buildList {
                        firefighter.badgeNumber?.let { add("Badge #$it") }
                        add(firefighter.rank ?: if (firefighter.isOfficer) "Officer" else "Firefighter")
                    }.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Operational Status
                Text(
                    text = "Operational Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PersonnelStatus.entries.forEach { status ->
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
                                .testTag("status_option_${status.name}")
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
                                    val subtitle = when {
                                        status.isActivelyEngaged -> "Actively operating on scene/drill"
                                        status.isReadyToRespond -> "Ready for emergency response"
                                        else -> "Unavailable for dispatch"
                                    }
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Profile Details
                Text(
                    text = "Department Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailRow("Department ID", firefighter.departmentId)
                    firefighter.stationId?.let { DetailRow("Assigned Station", it) }
                    firefighter.email?.let { DetailRow("Email", it) }
                    firefighter.phone?.let { DetailRow("Phone", it) }
                    DetailRow("Officer Rank", if (firefighter.isOfficer) "Yes (${firefighter.rank ?: "Officer"})" else "No")
                    DetailRow("Active Member", if (firefighter.isActive) "Yes" else "No")
                }

                if (firefighter.certifications.isNotEmpty()) {
                    Text(
                        text = "Certifications & Qualifications",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = firefighter.certifications.joinToString(", "),
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
                modifier = Modifier.testTag("confirm_status_update_button")
            ) {
                Text("Update Status")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_dialog_button")
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
