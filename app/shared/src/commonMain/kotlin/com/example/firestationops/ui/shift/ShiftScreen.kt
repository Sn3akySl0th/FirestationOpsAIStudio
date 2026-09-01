package com.example.firestationops.ui.shift

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.FirefighterAvailability
import com.example.firestationops.model.Shift
import com.example.firestationops.model.ShiftStatus
import com.example.firestationops.model.ShiftType

enum class ShiftTab(val title: String) {
    SCHEDULE("Shift Schedules"),
    AVAILABILITY("Availability Patterns")
}

/**
 * Primary screen to manage firefighter shift scheduling, crew staffing levels, and availability patterns.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftScreen(
    shifts: List<Shift>,
    firefighters: List<Firefighter>,
    availabilities: Map<String, FirefighterAvailability>,
    departmentId: String,
    onSaveShift: (Shift) -> Unit = {},
    onUpdateShiftStatus: (shiftId: String, newStatus: ShiftStatus) -> Unit = { _, _ -> },
    onAssignFirefighter: (shiftId: String, firefighterId: String) -> Unit = { _, _ -> },
    onRemoveFirefighter: (shiftId: String, firefighterId: String) -> Unit = { _, _ -> },
    onUpdateAvailability: (FirefighterAvailability) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ShiftTab.SCHEDULE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<ShiftStatus?>(null) }
    var onlyNeedsStaffingFilter by remember { mutableStateOf(false) }

    // Dialog States
    var shiftToEdit by remember { mutableStateOf<Shift?>(null) }
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var shiftForAssignment by remember { mutableStateOf<Shift?>(null) }
    var firefighterForAvailabilityEdit by remember { mutableStateOf<Firefighter?>(null) }

    // Metrics Calculations
    val totalShifts = shifts.size
    val activeShifts = shifts.count { it.status == ShiftStatus.ACTIVE }
    val understaffedShifts = shifts.count { !it.isAdequatelyStaffed }
    val staffedShifts = totalShifts - understaffedShifts

    val filteredShifts = remember(shifts, searchQuery, selectedStatusFilter, onlyNeedsStaffingFilter) {
        shifts.filter { shift ->
            val matchesStatus = selectedStatusFilter == null || shift.status == selectedStatusFilter
            val matchesStaffing = !onlyNeedsStaffingFilter || !shift.isAdequatelyStaffed
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                shift.name.lowercase().contains(q) ||
                    shift.shiftType.label.lowercase().contains(q) ||
                    (shift.notes?.lowercase()?.contains(q) == true)
            }
            matchesStatus && matchesStaffing && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.testTag("shift_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Shift Scheduling & Availability",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$activeShifts Active Shifts • $staffedShifts/$totalShifts Fully Staffed" +
                                if (understaffedShifts > 0) " • ⚠️ $understaffedShifts Need Crew" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (understaffedShifts > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == ShiftTab.SCHEDULE) {
                FloatingActionButton(
                    onClick = { isCreateDialogOpen = true },
                    modifier = Modifier.testTag("create_shift_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("+ Shift", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Metrics Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Active Shifts",
                    value = activeShifts.toString(),
                    subtitle = "On Duty Crew",
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Staffing Met",
                    value = "$staffedShifts / $totalShifts",
                    subtitle = if (understaffedShifts == 0) "All Ready" else "$understaffedShifts Short",
                    containerColor = if (understaffedShifts == 0) Color(0xFFE3F2FD) else Color(0xFFFFF3E0),
                    contentColor = if (understaffedShifts == 0) Color(0xFF0D47A1) else Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Firefighters",
                    value = firefighters.size.toString(),
                    subtitle = "Roster Total",
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF6A1B9A),
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth().testTag("shift_tab_row")
            ) {
                ShiftTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            when (selectedTab) {
                ShiftTab.SCHEDULE -> {
                    // Shift Schedules Content
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search shifts, platoons, or notes...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("shift_search_input"),
                            singleLine = true
                        )

                        // Filter Chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("shift_filter_chips_row")
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedStatusFilter == null && !onlyNeedsStaffingFilter,
                                    onClick = {
                                        selectedStatusFilter = null
                                        onlyNeedsStaffingFilter = false
                                    },
                                    label = { Text("All Shifts ($totalShifts)") },
                                    modifier = Modifier.testTag("filter_all_shifts")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedStatusFilter == ShiftStatus.ACTIVE,
                                    onClick = {
                                        selectedStatusFilter = if (selectedStatusFilter == ShiftStatus.ACTIVE) null else ShiftStatus.ACTIVE
                                        onlyNeedsStaffingFilter = false
                                    },
                                    label = { Text("Active Duty ($activeShifts)") },
                                    modifier = Modifier.testTag("filter_active_shifts")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedStatusFilter == ShiftStatus.SCHEDULED,
                                    onClick = {
                                        selectedStatusFilter = if (selectedStatusFilter == ShiftStatus.SCHEDULED) null else ShiftStatus.SCHEDULED
                                        onlyNeedsStaffingFilter = false
                                    },
                                    label = { Text("Scheduled (${shifts.count { it.status == ShiftStatus.SCHEDULED }})") },
                                    modifier = Modifier.testTag("filter_scheduled_shifts")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = onlyNeedsStaffingFilter,
                                    onClick = {
                                        onlyNeedsStaffingFilter = !onlyNeedsStaffingFilter
                                        selectedStatusFilter = null
                                    },
                                    label = { Text("⚠️ Needs Crew ($understaffedShifts)") },
                                    modifier = Modifier.testTag("filter_needs_staffing")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Shifts List
                        if (filteredShifts.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No shifts match the current search or filters.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            searchQuery = ""
                                            selectedStatusFilter = null
                                            onlyNeedsStaffingFilter = false
                                        }
                                    ) {
                                        Text("Reset Filters")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(filteredShifts) { shift ->
                                    ShiftCard(
                                        shift = shift,
                                        allFirefighters = firefighters,
                                        onAssignClick = { shiftForAssignment = shift },
                                        onRemoveFirefighter = { ffId -> onRemoveFirefighter(shift.id, ffId) },
                                        onStatusChange = { newStatus -> onUpdateShiftStatus(shift.id, newStatus) },
                                        onEditClick = { shiftToEdit = shift }
                                    )
                                }
                            }
                        }
                    }
                }

                ShiftTab.AVAILABILITY -> {
                    // Firefighter Availability Patterns Content
                    AvailabilityPatternView(
                        firefighters = firefighters,
                        availabilities = availabilities,
                        onEditAvailability = { ff -> firefighterForAvailabilityEdit = ff },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Create / Edit Shift Dialog
    if (isCreateDialogOpen || shiftToEdit != null) {
        CreateOrEditShiftDialog(
            shift = shiftToEdit,
            departmentId = departmentId,
            allFirefighters = firefighters,
            onDismiss = {
                isCreateDialogOpen = false
                shiftToEdit = null
            },
            onSave = { savedShift ->
                onSaveShift(savedShift)
                isCreateDialogOpen = false
                shiftToEdit = null
            }
        )
    }

    // Assign Firefighter to Shift Dialog
    shiftForAssignment?.let { shift ->
        AssignFirefighterDialog(
            shift = shift,
            allFirefighters = firefighters,
            availabilities = availabilities,
            onDismiss = { shiftForAssignment = null },
            onAssign = { firefighterId ->
                onAssignFirefighter(shift.id, firefighterId)
                shiftForAssignment = null
            }
        )
    }

    // Edit Firefighter Availability Dialog
    firefighterForAvailabilityEdit?.let { ff ->
        EditAvailabilityDialog(
            firefighter = ff,
            currentAvailability = availabilities[ff.id],
            onDismiss = { firefighterForAvailabilityEdit = null },
            onSave = { updatedAvailability ->
                onUpdateAvailability(updatedAvailability)
                firefighterForAvailabilityEdit = null
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = contentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.8f))
        }
    }
}
