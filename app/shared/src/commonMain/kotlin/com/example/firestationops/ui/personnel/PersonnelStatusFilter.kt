package com.example.firestationops.ui.personnel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.PersonnelStatus

/**
 * Filter UI component allowing users to toggle visibility of firefighters based on their PersonnelStatus.
 *
 * Supports:
 * - "All" preset to view all personnel regardless of status
 * - "Ready to Respond" preset (Available + Standby)
 * - "Active Incidents" preset (Responding + On Scene)
 * - Individual status toggles with live member counts and high-contrast status dots
 */
@Composable
fun PersonnelStatusFilterBar(
    selectedStatuses: Set<PersonnelStatus>,
    onStatusToggled: (PersonnelStatus) -> Unit,
    onSelectAll: () -> Unit,
    onSelectPreset: (Set<PersonnelStatus>) -> Unit,
    statusCounts: Map<PersonnelStatus, Int>,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val isAllSelected = selectedStatuses.isEmpty() || selectedStatuses.size == PersonnelStatus.entries.size

    val readyStatuses = setOf(PersonnelStatus.AVAILABLE, PersonnelStatus.STATION_STANDBY)
    val isReadyPresetActive = !isAllSelected && selectedStatuses == readyStatuses

    val activeIncidentStatuses = setOf(PersonnelStatus.RESPONDING, PersonnelStatus.ON_SCENE)
    val isActiveIncidentPresetActive = !isAllSelected && selectedStatuses == activeIncidentStatuses

    val readyCount = (statusCounts[PersonnelStatus.AVAILABLE] ?: 0) + (statusCounts[PersonnelStatus.STATION_STANDBY] ?: 0)
    val activeIncidentCount = (statusCounts[PersonnelStatus.RESPONDING] ?: 0) + (statusCounts[PersonnelStatus.ON_SCENE] ?: 0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("personnel_status_filter_bar"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Quick Presets Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("personnel_preset_filter_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = isAllSelected,
                    onClick = onSelectAll,
                    label = {
                        Text(
                            text = "All ($totalCount)",
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("filter_chip_all")
                )
            }

            item {
                FilterChip(
                    selected = isReadyPresetActive,
                    onClick = {
                        if (isReadyPresetActive) {
                            onSelectAll()
                        } else {
                            onSelectPreset(readyStatuses)
                        }
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ready to Respond ($readyCount)",
                                fontWeight = if (isReadyPresetActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("filter_chip_ready_group")
                )
            }

            item {
                FilterChip(
                    selected = isActiveIncidentPresetActive,
                    onClick = {
                        if (isActiveIncidentPresetActive) {
                            onSelectAll()
                        } else {
                            onSelectPreset(activeIncidentStatuses)
                        }
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "On Call / Scene ($activeIncidentCount)",
                                fontWeight = if (isActiveIncidentPresetActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("filter_chip_active_group")
                )
            }
        }

        // Individual Status Toggles Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("personnel_individual_status_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PersonnelStatus.entries) { status ->
                val count = statusCounts[status] ?: 0
                val isSelected = !isAllSelected && selectedStatuses.contains(status)
                val dotColor = getStatusDotColor(status)

                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusToggled(status) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = dotColor,
                                modifier = Modifier.size(6.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${status.label} ($count)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getStatusSelectedContainerColor(status),
                        selectedLabelColor = getStatusSelectedLabelColor(status)
                    ),
                    modifier = Modifier.testTag("filter_chip_${status.name.lowercase()}")
                )
            }
        }
    }
}

private fun getStatusDotColor(status: PersonnelStatus): Color {
    return when (status) {
        PersonnelStatus.AVAILABLE -> Color(0xFF2E7D32)
        PersonnelStatus.RESPONDING -> Color(0xFFEF6C00)
        PersonnelStatus.ON_SCENE -> Color(0xFFD32F2F)
        PersonnelStatus.STATION_STANDBY -> Color(0xFF1976D2)
        PersonnelStatus.TRAINING -> Color(0xFF7B1FA2)
        PersonnelStatus.UNAVAILABLE -> Color(0xFF757575)
        PersonnelStatus.LEAVE -> Color(0xFF546E7A)
        PersonnelStatus.RETIRED -> Color(0xFF424242)
    }
}

private fun getStatusSelectedContainerColor(status: PersonnelStatus): Color {
    return when (status) {
        PersonnelStatus.AVAILABLE -> Color(0xFFE8F5E9)
        PersonnelStatus.RESPONDING -> Color(0xFFFFF3E0)
        PersonnelStatus.ON_SCENE -> Color(0xFFFFEBEE)
        PersonnelStatus.STATION_STANDBY -> Color(0xFFE3F2FD)
        PersonnelStatus.TRAINING -> Color(0xFFF3E5F5)
        PersonnelStatus.UNAVAILABLE -> Color(0xFFEEEEEE)
        PersonnelStatus.LEAVE -> Color(0xFFECEFF1)
        PersonnelStatus.RETIRED -> Color(0xFFE0E0E0)
    }
}

private fun getStatusSelectedLabelColor(status: PersonnelStatus): Color {
    return when (status) {
        PersonnelStatus.AVAILABLE -> Color(0xFF1B5E20)
        PersonnelStatus.RESPONDING -> Color(0xFFE65100)
        PersonnelStatus.ON_SCENE -> Color(0xFFB71C1C)
        PersonnelStatus.STATION_STANDBY -> Color(0xFF0D47A1)
        PersonnelStatus.TRAINING -> Color(0xFF4A148C)
        PersonnelStatus.UNAVAILABLE -> Color(0xFF212121)
        PersonnelStatus.LEAVE -> Color(0xFF263238)
        PersonnelStatus.RETIRED -> Color(0xFF212121)
    }
}
