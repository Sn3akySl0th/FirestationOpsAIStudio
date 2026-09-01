package com.example.firestationops.ui.shift

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.firestationops.model.AvailabilityPattern
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.FirefighterAvailability
import com.example.firestationops.model.Shift
import com.example.firestationops.model.ShiftStatus
import com.example.firestationops.model.ShiftType

/**
 * Dialog to create or edit a shift definition.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateOrEditShiftDialog(
    shift: Shift?,
    departmentId: String,
    allFirefighters: List<Firefighter>,
    onDismiss: () -> Unit,
    onSave: (Shift) -> Unit
) {
    var name by remember { mutableStateOf(shift?.name ?: "") }
    var selectedType by remember { mutableStateOf(shift?.shiftType ?: ShiftType.DAY_DUTY) }
    var minStaffing by remember { mutableStateOf(shift?.minimumStaffing ?: 4) }
    var selectedOicId by remember { mutableStateOf(shift?.officerInChargeId ?: allFirefighters.firstOrNull { it.isOfficer }?.id) }
    var status by remember { mutableStateOf(shift?.status ?: ShiftStatus.SCHEDULED) }
    var notes by remember { mutableStateOf(shift?.notes ?: "") }
    var selectedDays by remember {
        mutableStateOf(shift?.recurringDays?.toSet() ?: setOf("MON", "TUE", "WED", "THU", "FRI"))
    }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var oicDropdownExpanded by remember { mutableStateOf(false) }

    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("create_or_edit_shift_dialog"),
        title = {
            Text(
                text = if (shift == null) "Create Operational Shift" else "Edit Shift Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Shift Name") },
                        placeholder = { Text("e.g. Engine 1 Day Duty Crew") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shift_name_input"),
                        singleLine = true
                    )
                }

                item {
                    // Shift Type selector
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedType.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Shift Type / Platoon") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("shift_type_selector")
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            ShiftType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.label) },
                                    onClick = {
                                        selectedType = type
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    // Minimum Staffing Counter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Minimum Staffing Required", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Crew threshold for response readiness", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (minStaffing > 1) minStaffing-- },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("-", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = minStaffing.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("min_staffing_value")
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = { minStaffing++ },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                item {
                    // Officer In Charge (OIC)
                    ExposedDropdownMenuBox(
                        expanded = oicDropdownExpanded,
                        onExpandedChange = { oicDropdownExpanded = !oicDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentOic = allFirefighters.firstOrNull { it.id == selectedOicId }
                        OutlinedTextField(
                            value = currentOic?.let { "${it.fullName} (${it.rank ?: "FF"})" } ?: "Select Officer in Charge",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Officer in Charge (OIC)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = oicDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("shift_oic_selector")
                        )
                        ExposedDropdownMenu(
                            expanded = oicDropdownExpanded,
                            onDismissRequest = { oicDropdownExpanded = false }
                        ) {
                            allFirefighters.forEach { ff ->
                                DropdownMenuItem(
                                    text = { Text("${ff.fullName} (${ff.rank ?: "FF"}${if (ff.isOfficer) " - Officer" else ""})") },
                                    onClick = {
                                        selectedOicId = ff.id
                                        oicDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    // Recurring Days of Week Chips
                    Text("Recurring Active Days", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        daysOfWeek.forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                },
                                label = { Text(day, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Operational Notes / Coverage Notes") },
                        placeholder = { Text("Apparatus assignment, duties, or requirements") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shift_notes_input"),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newShift = Shift(
                        id = shift?.id ?: "shift_${System.currentTimeMillis()}",
                        departmentId = departmentId,
                        stationId = shift?.stationId ?: "station_1",
                        name = name.ifBlank { "${selectedType.label} Crew" },
                        shiftType = selectedType,
                        startTimeMillis = shift?.startTimeMillis ?: System.currentTimeMillis(),
                        endTimeMillis = shift?.endTimeMillis ?: (System.currentTimeMillis() + 43200000L),
                        minimumStaffing = minStaffing,
                        officerInChargeId = selectedOicId,
                        assignedFirefighterIds = shift?.assignedFirefighterIds ?: emptyList(),
                        status = status,
                        recurringDays = selectedDays.toList(),
                        notes = notes.ifBlank { null },
                        createdAt = shift?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(newShift)
                },
                modifier = Modifier.testTag("save_shift_button")
            ) {
                Text("Save Shift")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_shift_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog to assign an available firefighter to a specific shift.
 */
@Composable
fun AssignFirefighterDialog(
    shift: Shift,
    allFirefighters: List<Firefighter>,
    availabilities: Map<String, FirefighterAvailability>,
    onDismiss: () -> Unit,
    onAssign: (firefighterId: String) -> Unit
) {
    val unassigned = allFirefighters.filter { !shift.assignedFirefighterIds.contains(it.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("assign_firefighter_dialog"),
        title = {
            Column {
                Text("Assign to ${shift.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Current: ${shift.assignedCount} / ${shift.minimumStaffing} Crew Members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (unassigned.isEmpty()) {
                Text("All department personnel are currently assigned to this shift.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(unassigned) { ff ->
                        val avail = availabilities[ff.id]
                        Card(
                            onClick = { onAssign(ff.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firefighter_assign_item_${ff.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = ff.fullName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (ff.isOfficer) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "★ ${ff.rank ?: "Officer"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Badge #${ff.badgeNumber ?: "N/A"} • ${ff.status.label}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    avail?.let {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Pattern: ${it.pattern.label}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onAssign(ff.id) },
                                    modifier = Modifier.testTag("assign_button_${ff.id}")
                                ) {
                                    Text("Assign", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Dialog to view and adjust a firefighter's availability pattern preference.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditAvailabilityDialog(
    firefighter: Firefighter,
    currentAvailability: FirefighterAvailability?,
    onDismiss: () -> Unit,
    onSave: (FirefighterAvailability) -> Unit
) {
    var selectedPattern by remember {
        mutableStateOf(currentAvailability?.pattern ?: AvailabilityPattern.ALWAYS_AVAILABLE)
    }
    var availableDays by remember {
        mutableStateOf(
            currentAvailability?.availableDays?.toSet()
                ?: setOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        )
    }
    var isAvailableForOvertime by remember {
        mutableStateOf(currentAvailability?.isAvailableForOvertime ?: true)
    }
    var notes by remember {
        mutableStateOf(currentAvailability?.notes ?: "")
    }
    var patternDropdownExpanded by remember { mutableStateOf(false) }

    val allDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("edit_availability_dialog"),
        title = {
            Column {
                Text("Availability Pattern", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "${firefighter.fullName} (Badge #${firefighter.badgeNumber ?: "N/A"})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    ExposedDropdownMenuBox(
                        expanded = patternDropdownExpanded,
                        onExpandedChange = { patternDropdownExpanded = !patternDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedPattern.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Primary Availability Pattern") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patternDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("availability_pattern_selector")
                        )
                        ExposedDropdownMenu(
                            expanded = patternDropdownExpanded,
                            onDismissRequest = { patternDropdownExpanded = false }
                        ) {
                            AvailabilityPattern.entries.forEach { pattern ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(pattern.label, fontWeight = FontWeight.SemiBold)
                                            Text(pattern.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedPattern = pattern
                                        patternDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Designated Available Days", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allDays.forEach { day ->
                            val isSelected = availableDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    availableDays = if (isSelected) availableDays - day else availableDays + day
                                },
                                label = { Text(day, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Overtime / 2nd-Alarm Callback", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Available for emergency call-backs during off-hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isAvailableForOvertime,
                            onCheckedChange = { isAvailableForOvertime = it },
                            modifier = Modifier.testTag("overtime_switch")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Availability Notes") },
                        placeholder = { Text("e.g. Available after 17:30 weekdays, work commute") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("availability_notes_input"),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = FirefighterAvailability(
                        firefighterId = firefighter.id,
                        pattern = selectedPattern,
                        availableDays = availableDays.toList(),
                        preferredShiftTypes = currentAvailability?.preferredShiftTypes ?: emptyList(),
                        isAvailableForOvertime = isAvailableForOvertime,
                        notes = notes.ifBlank { null }
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_availability_button")
            ) {
                Text("Save Availability")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_availability_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
