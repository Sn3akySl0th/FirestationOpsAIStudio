package com.example.firestationops.ui.shift

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentStatus
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Shift
import com.example.firestationops.model.ShiftStatus
import com.example.firestationops.model.Station

/**
 * Filter mode for upcoming shifts displayed on the dashboard.
 */
enum class ShiftDashboardFilter(val label: String) {
    MY_SHIFTS("My Assigned Shifts"),
    STATION_SHIFTS("All Station Shifts"),
    NEEDS_STAFFING("Needs Crew / Open")
}

/**
 * Firefighter Shift & Station Dashboard composable.
 * Displays upcoming shifts for the active firefighter, personal readiness controls,
 * real-time station operational status, on-duty crew, and station staffing levels.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShiftDashboard(
    shifts: List<Shift>,
    firefighters: List<Firefighter>,
    stations: List<Station>,
    equipmentList: List<Equipment> = emptyList(),
    currentFirefighterId: String? = null,
    onFirefighterStatusChange: (firefighterId: String, newStatus: PersonnelStatus) -> Unit = { _, _ -> },
    onAssignFirefighter: (shiftId: String, firefighterId: String) -> Unit = { _, _ -> },
    onRemoveFirefighter: (shiftId: String, firefighterId: String) -> Unit = { _, _ -> },
    onUpdateShiftStatus: (shiftId: String, newStatus: ShiftStatus) -> Unit = { _, _ -> },
    onViewAllSchedulesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Active Firefighter Selection State (defaults to provided ID or first available firefighter)
    var selectedFirefighterId by remember(currentFirefighterId, firefighters) {
        mutableStateOf(currentFirefighterId ?: firefighters.firstOrNull()?.id ?: "ff_01")
    }
    val activeFirefighter = remember(selectedFirefighterId, firefighters) {
        firefighters.firstOrNull { it.id == selectedFirefighterId } ?: firefighters.firstOrNull()
    }

    // Station Selection State (defaults to active firefighter's station or first station)
    var selectedStationId by remember(activeFirefighter, stations) {
        mutableStateOf(activeFirefighter?.stationId ?: stations.firstOrNull()?.id ?: "station_1")
    }
    val activeStation = remember(selectedStationId, stations) {
        stations.firstOrNull { it.id == selectedStationId } ?: stations.firstOrNull()
    }

    // Shift List Filter State
    var shiftFilter by remember { mutableStateOf(ShiftDashboardFilter.MY_SHIFTS) }
    var showFirefighterDropdown by remember { mutableStateOf(false) }
    var showStationDropdown by remember { mutableStateOf(false) }

    // Next upcoming or active shift for the active firefighter
    val firefighterShifts = remember(shifts, selectedFirefighterId) {
        shifts.filter { it.assignedFirefighterIds.contains(selectedFirefighterId) }
    }

    val activeShift = remember(firefighterShifts) {
        firefighterShifts.firstOrNull { it.status == ShiftStatus.ACTIVE }
            ?: shifts.firstOrNull { it.status == ShiftStatus.ACTIVE && it.assignedFirefighterIds.contains(selectedFirefighterId) }
    }

    val nextUpcomingShift = remember(firefighterShifts, activeShift) {
        if (activeShift != null) null
        else firefighterShifts
            .filter { it.status == ShiftStatus.SCHEDULED }
            .minByOrNull { it.startTimeMillis }
    }

    // Station-specific metrics
    val stationShifts = remember(shifts, selectedStationId) {
        shifts.filter { it.stationId == null || it.stationId == selectedStationId }
    }
    val stationActiveShifts = remember(stationShifts) {
        stationShifts.filter { it.status == ShiftStatus.ACTIVE }
    }
    val stationOnDutyFirefighterIds = remember(stationActiveShifts) {
        stationActiveShifts.flatMap { it.assignedFirefighterIds }.distinct()
    }
    val stationOnDutyFirefighters = remember(stationOnDutyFirefighterIds, firefighters) {
        firefighters.filter { stationOnDutyFirefighterIds.contains(it.id) }
    }
    val stationEquipment = remember(equipmentList, selectedStationId) {
        equipmentList.filter { it.stationId == null || it.stationId == selectedStationId }
    }
    val readyEquipmentCount = remember(stationEquipment) {
        stationEquipment.count { it.status == EquipmentStatus.IN_SERVICE }
    }
    val needsAttentionEquipmentCount = remember(stationEquipment) {
        stationEquipment.count { it.status != EquipmentStatus.IN_SERVICE }
    }

    // Filtered shifts for display
    val displayedShifts = remember(shifts, shiftFilter, selectedFirefighterId, selectedStationId) {
        when (shiftFilter) {
            ShiftDashboardFilter.MY_SHIFTS -> {
                shifts.filter { it.assignedFirefighterIds.contains(selectedFirefighterId) }
            }
            ShiftDashboardFilter.STATION_SHIFTS -> {
                shifts.filter { it.stationId == null || it.stationId == selectedStationId }
            }
            ShiftDashboardFilter.NEEDS_STAFFING -> {
                shifts.filter { !it.isAdequatelyStaffed && it.status != ShiftStatus.COMPLETED && it.status != ShiftStatus.CANCELLED }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shift_dashboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------------------------------------------------------------------
        // 1. Firefighter Profile & Readiness Card
        // ---------------------------------------------------------------------
        item {
            if (activeFirefighter != null) {
                FirefighterReadinessCard(
                    firefighter = activeFirefighter,
                    allFirefighters = firefighters,
                    station = activeStation,
                    showDropdown = showFirefighterDropdown,
                    onToggleDropdown = { showFirefighterDropdown = !showFirefighterDropdown },
                    onSelectFirefighter = { ff ->
                        selectedFirefighterId = ff.id
                        showFirefighterDropdown = false
                    },
                    onStatusChange = { newStatus ->
                        onFirefighterStatusChange(activeFirefighter.id, newStatus)
                    }
                )
            }
        }

        // ---------------------------------------------------------------------
        // 2. Next Upcoming Shift / Active Shift Hero Banner
        // ---------------------------------------------------------------------
        item {
            NextShiftHeroCard(
                activeShift = activeShift,
                nextShift = nextUpcomingShift,
                activeFirefighter = activeFirefighter,
                allFirefighters = firefighters,
                station = activeStation,
                onCheckIn = { shiftId ->
                    onUpdateShiftStatus(shiftId, ShiftStatus.ACTIVE)
                },
                onViewSchedules = onViewAllSchedulesClick
            )
        }

        // ---------------------------------------------------------------------
        // 3. Station Status & Operational Readiness Overview
        // ---------------------------------------------------------------------
        item {
            StationStatusCard(
                station = activeStation,
                allStations = stations,
                showStationDropdown = showStationDropdown,
                onToggleStationDropdown = { showStationDropdown = !showStationDropdown },
                onSelectStation = { st ->
                    selectedStationId = st.id
                    showStationDropdown = false
                },
                activeShiftsCount = stationActiveShifts.size,
                onDutyCount = stationOnDutyFirefighters.size,
                totalEquipmentCount = stationEquipment.size,
                readyEquipmentCount = readyEquipmentCount,
                needsAttentionCount = needsAttentionEquipmentCount,
                onDutyFirefighters = stationOnDutyFirefighters
            )
        }

        // ---------------------------------------------------------------------
        // 4. Upcoming Shifts Header & Filters
        // ---------------------------------------------------------------------
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Shift Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = onViewAllSchedulesClick,
                        modifier = Modifier.testTag("btn_view_full_schedule")
                    ) {
                        Text("Manage All")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ShiftDashboardFilter.entries) { filter ->
                        val count = when (filter) {
                            ShiftDashboardFilter.MY_SHIFTS -> firefighterShifts.size
                            ShiftDashboardFilter.STATION_SHIFTS -> stationShifts.size
                            ShiftDashboardFilter.NEEDS_STAFFING -> shifts.count { !it.isAdequatelyStaffed }
                        }
                        FilterChip(
                            selected = shiftFilter == filter,
                            onClick = { shiftFilter = filter },
                            label = { Text("${filter.label} ($count)") },
                            modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 5. Shift Cards List
        // ---------------------------------------------------------------------
        if (displayedShifts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("empty_shifts_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (shiftFilter) {
                                ShiftDashboardFilter.MY_SHIFTS -> "No upcoming shifts assigned to you"
                                ShiftDashboardFilter.STATION_SHIFTS -> "No shifts scheduled for this station"
                                ShiftDashboardFilter.NEEDS_STAFFING -> "All scheduled shifts are currently fully staffed"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (shiftFilter == ShiftDashboardFilter.MY_SHIFTS) {
                            OutlinedButton(
                                onClick = { shiftFilter = ShiftDashboardFilter.NEEDS_STAFFING },
                                modifier = Modifier.testTag("btn_browse_open_shifts")
                            ) {
                                Text("Browse Open Shifts")
                            }
                        }
                    }
                }
            }
        } else {
            items(displayedShifts, key = { it.id }) { shift ->
                DashboardShiftItemCard(
                    shift = shift,
                    currentFirefighterId = selectedFirefighterId,
                    allFirefighters = firefighters,
                    onAssignMyself = {
                        if (activeFirefighter != null) {
                            onAssignFirefighter(shift.id, activeFirefighter.id)
                        }
                    },
                    onRemoveMyself = {
                        if (activeFirefighter != null) {
                            onRemoveFirefighter(shift.id, activeFirefighter.id)
                        }
                    },
                    onCheckIn = {
                        onUpdateShiftStatus(shift.id, ShiftStatus.ACTIVE)
                    }
                )
            }
        }
    }
}

/**
 * Card displaying active firefighter profile, badge, and quick readiness toggle.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FirefighterReadinessCard(
    firefighter: Firefighter,
    allFirefighters: List<Firefighter>,
    station: Station?,
    showDropdown: Boolean,
    onToggleDropdown: () -> Unit,
    onSelectFirefighter: (Firefighter) -> Unit,
    onStatusChange: (PersonnelStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firefighter_readiness_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Firefighter Info & Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Firefighter Initials Avatar
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val initials = "${firefighter.firstName.take(1)}${firefighter.lastName.take(1)}"
                            Text(
                                text = initials.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = firefighter.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (firefighter.isOfficer) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFFF8E1),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD54F))
                                ) {
                                    Text(
                                        text = "OFFICER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${firefighter.rank ?: "Firefighter"} • Badge #${firefighter.badgeNumber ?: "N/A"} • ${station?.name ?: "Station 51"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Firefighter Profile Switcher
                Box {
                    OutlinedButton(
                        onClick = onToggleDropdown,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_switch_firefighter")
                    ) {
                        Text("Switch", style = MaterialTheme.typography.labelSmall)
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = onToggleDropdown
                    ) {
                        allFirefighters.forEach { ff ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(ff.fullName, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${ff.rank ?: "FF"} • Badge #${ff.badgeNumber ?: "-"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = { onSelectFirefighter(ff) },
                                modifier = Modifier.testTag("firefighter_option_${ff.id}")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Current Status & Quick Toggle Row
            Text(
                text = "My Operational Readiness Status",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PersonnelStatus.entries.forEach { status ->
                    val isSelected = firefighter.status == status
                    val (statusColor, textColor) = when (status) {
                        PersonnelStatus.AVAILABLE -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
                        PersonnelStatus.STATION_STANDBY -> Color(0xFF0288D1) to Color(0xFFE1F5FE)
                        PersonnelStatus.RESPONDING -> Color(0xFFD32F2F) to Color(0xFFFFEBEE)
                        PersonnelStatus.ON_SCENE -> Color(0xFFC2185B) to Color(0xFFFCE4EC)
                        PersonnelStatus.TRAINING -> Color(0xFF7B1FA2) to Color(0xFFF3E5F5)
                        PersonnelStatus.UNAVAILABLE -> Color(0xFF616161) to Color(0xFFF5F5F5)
                        PersonnelStatus.LEAVE -> Color(0xFF795548) to Color(0xFFEFEBE9)
                        PersonnelStatus.RETIRED -> Color(0xFF455A64) to Color(0xFFECEFF1)
                    }

                    Surface(
                        modifier = Modifier
                            .clickable { onStatusChange(status) }
                            .testTag("status_chip_${status.name.lowercase()}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) statusColor else textColor,
                        border = BorderStroke(1.dp, if (isSelected) statusColor else statusColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = status.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else statusColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Prominent hero card showing active on-duty shift or next scheduled shift.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NextShiftHeroCard(
    activeShift: Shift?,
    nextShift: Shift?,
    activeFirefighter: Firefighter?,
    allFirefighters: List<Firefighter>,
    station: Station?,
    onCheckIn: (shiftId: String) -> Unit,
    onViewSchedules: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeShift != null) {
        // CURRENTLY ON DUTY
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("active_shift_hero_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                            color = Color(0xFF81C784),
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Text(
                            text = "CURRENTLY ON DUTY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5D6A7),
                            letterSpacing = 1.sp
                        )
                    }

                    ShiftTypeBadge(shiftType = activeShift.shiftType)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = activeShift.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Active Platoon • ${station?.name ?: "Station 51"} • ${activeShift.assignedFirefighterIds.size} Crew Members On Duty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE8F5E9)
                )

                if (activeShift.apparatusIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Assigned Apparatus: ${activeShift.apparatusIds.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC8E6C9),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Crew Chips
                val crew = activeShift.assignedFirefighterIds.mapNotNull { id ->
                    allFirefighters.firstOrNull { it.id == id }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    crew.forEach { ff ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50))
                        ) {
                            Text(
                                text = "${ff.rank ?: "FF"} ${ff.lastName}${if (ff.id == activeShift.officerInChargeId) " (OIC)" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontWeight = if (ff.id == activeFirefighter?.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    } else if (nextShift != null) {
        // NEXT UPCOMING SHIFT
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("next_shift_hero_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR NEXT SCHEDULED SHIFT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    ShiftTypeBadge(shiftType = nextShift.shiftType)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = nextShift.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Scheduled at ${station?.name ?: "Station 51"} • Min Staffing: ${nextShift.minimumStaffing}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                StaffingProgressBar(
                    assignedCount = nextShift.assignedCount,
                    minimumStaffing = nextShift.minimumStaffing
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onCheckIn(nextShift.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_check_in_shift")
                    ) {
                        Text("Check In / Start Duty")
                    }
                }
            }
        }
    } else {
        // NO UPCOMING SHIFTS
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("no_upcoming_shift_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Upcoming Shifts Assigned",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You are not currently scheduled for any upcoming shifts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onViewSchedules,
                    modifier = Modifier.testTag("btn_view_schedules")
                ) {
                    Text("View Full Shift Schedule")
                }
            }
        }
    }
}

/**
 * Card displaying station readiness, active crew count, equipment readiness, and apparatus status.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StationStatusCard(
    station: Station?,
    allStations: List<Station>,
    showStationDropdown: Boolean,
    onToggleStationDropdown: () -> Unit,
    onSelectStation: (Station) -> Unit,
    activeShiftsCount: Int,
    onDutyCount: Int,
    totalEquipmentCount: Int,
    readyEquipmentCount: Int,
    needsAttentionCount: Int,
    onDutyFirefighters: List<Firefighter>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("station_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Station Name & Dropdown Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Station Operational Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = station?.name ?: "Station 51 - Central HQ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Station Switcher Dropdown
                Box {
                    OutlinedButton(
                        onClick = onToggleStationDropdown,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_select_station")
                    ) {
                        Text(station?.stationNumber?.let { "Stn $it" } ?: "Select", style = MaterialTheme.typography.labelSmall)
                    }

                    DropdownMenu(
                        expanded = showStationDropdown,
                        onDismissRequest = onToggleStationDropdown
                    ) {
                        allStations.forEach { st ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(st.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            st.address ?: "Station ${st.stationNumber ?: ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = { onSelectStation(st) },
                                modifier = Modifier.testTag("station_option_${st.id}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Pill Metric Stat Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatTile(
                    title = "On Duty",
                    value = "$onDutyCount FF",
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "Active Shifts",
                    value = "$activeShiftsCount",
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "Equipment",
                    value = "$readyEquipmentCount / $totalEquipmentCount",
                    color = if (needsAttentionCount > 0) Color(0xFFE65100) else Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            // On-Duty Crew List if any
            if (onDutyFirefighters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Currently On-Duty Personnel at Station",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    onDutyFirefighters.forEach { ff ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "${ff.rank ?: "FF"} ${ff.fullName}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Metric stat tile for station status.
 */
@Composable
private fun StatTile(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Individual shift item card in the dashboard list.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardShiftItemCard(
    shift: Shift,
    currentFirefighterId: String?,
    allFirefighters: List<Firefighter>,
    onAssignMyself: () -> Unit,
    onRemoveMyself: () -> Unit,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserAssigned = currentFirefighterId != null && shift.assignedFirefighterIds.contains(currentFirefighterId)
    val assignedFirefighters = shift.assignedFirefighterIds.mapNotNull { id ->
        allFirefighters.firstOrNull { it.id == id }
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_shift_item_${shift.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isUserAssigned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isUserAssigned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Shift Name & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shift.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShiftTypeBadge(shiftType = shift.shiftType)
                        ShiftStatusBadge(status = shift.status)
                    }
                }

                if (isUserAssigned) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF81C784))
                    ) {
                        Text(
                            text = "ASSIGNED TO YOU",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Staffing Progress
            StaffingProgressBar(
                assignedCount = shift.assignedCount,
                minimumStaffing = shift.minimumStaffing
            )

            // Assigned Crew
            if (assignedFirefighters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    assignedFirefighters.forEach { ff ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "${ff.rank ?: "FF"} ${ff.lastName}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUserAssigned) {
                    if (shift.status == ShiftStatus.SCHEDULED) {
                        Button(
                            onClick = onCheckIn,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_check_in_${shift.id}")
                        ) {
                            Text("Check In", style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(
                        onClick = onRemoveMyself,
                        modifier = Modifier.testTag("btn_remove_myself_${shift.id}")
                    ) {
                        Text("Drop Shift", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    OutlinedButton(
                        onClick = onAssignMyself,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_claim_shift_${shift.id}")
                    ) {
                        Text("Claim / Sign Up", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
