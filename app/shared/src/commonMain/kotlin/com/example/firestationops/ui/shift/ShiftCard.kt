package com.example.firestationops.ui.shift

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.Shift
import com.example.firestationops.model.ShiftStatus

/**
 * Card displaying an operational shift schedule, staffing gauge, assigned crew, and management controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftCard(
    shift: Shift,
    allFirefighters: List<Firefighter>,
    onAssignClick: () -> Unit,
    onRemoveFirefighter: (firefighterId: String) -> Unit,
    onStatusChange: (ShiftStatus) -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assignedFirefighters = shift.assignedFirefighterIds.mapNotNull { id ->
        allFirefighters.firstOrNull { it.id == id }
    }
    val oic = allFirefighters.firstOrNull { it.id == shift.officerInChargeId }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shift_card_${shift.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (shift.status == ShiftStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (shift.status == ShiftStatus.ACTIVE) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Shift Name, Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = shift.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ShiftTypeBadge(shiftType = shift.shiftType)
                        ShiftStatusBadge(status = shift.status)
                    }
                }

                // Edit Button
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_shift_button_${shift.id}"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("Edit", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Officer in Charge & Recurring Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                oic?.let { officer ->
                    Text(
                        text = "Officer in Charge: ${officer.fullName} (${officer.rank ?: "Officer"})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (shift.recurringDays.isNotEmpty()) {
                    Text(
                        text = "Days: ${shift.recurringDays.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Staffing Gauge
            StaffingProgressBar(
                assignedCount = shift.assignedCount,
                minimumStaffing = shift.minimumStaffing,
                modifier = Modifier.fillMaxWidth()
            )

            // Notes if any
            shift.notes?.let { noteText ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = noteText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Crew Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assigned Crew (${assignedFirefighters.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAssignClick,
                    modifier = Modifier.testTag("assign_member_button_${shift.id}"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("+ Assign Member", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (assignedFirefighters.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ No firefighters assigned yet. Tap '+ Assign Member' to schedule crew.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    assignedFirefighters.forEach { ff ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = {
                                Text(
                                    text = "${ff.fullName}${if (ff.rank != null) " (${ff.rank})" else ""}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = "✕",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.testTag("remove_crew_${ff.id}")
                                )
                            },
                            modifier = Modifier.testTag("assigned_crew_chip_${ff.id}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Status Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (shift.status != ShiftStatus.ACTIVE) {
                    TextButton(
                        onClick = { onStatusChange(ShiftStatus.ACTIVE) },
                        modifier = Modifier.testTag("activate_shift_button_${shift.id}")
                    ) {
                        Text("Mark Active / On Duty", color = Color(0xFF2E7D32))
                    }
                }
                if (shift.status == ShiftStatus.ACTIVE) {
                    TextButton(
                        onClick = { onStatusChange(ShiftStatus.COMPLETED) },
                        modifier = Modifier.testTag("complete_shift_button_${shift.id}")
                    ) {
                        Text("Complete Shift")
                    }
                }
            }
        }
    }
}
