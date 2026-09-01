package com.example.firestationops.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firestationops.domain.dashboard.AlertCategory
import com.example.firestationops.domain.dashboard.AlertSeverity
import com.example.firestationops.domain.dashboard.CategoryReadiness
import com.example.firestationops.domain.dashboard.StationReadinessSummary
import com.example.firestationops.domain.dashboard.UrgentMaintenanceAlert
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Station
import kotlin.math.roundToInt

/**
 * Modern dashboard view presenting current station operational status,
 * active firefighter headcount, equipment readiness metrics, visual charts,
 * and urgent maintenance alerts.
 */
@Composable
fun StationStatusOverviewCard(
    summary: StationReadinessSummary,
    stations: List<Station>,
    selectedStationId: String?,
    onStationSelect: (String?) -> Unit,
    onApparatusClick: (String) -> Unit = {},
    onDeficiencyClick: (String) -> Unit = {},
    onEquipmentClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCategoryBreakdown by remember { mutableStateOf(false) }
    var selectedAlertFilter by remember { mutableStateOf<AlertCategory?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Station Selector & Title
            StationHeaderSection(
                summary = summary,
                stations = stations,
                selectedStationId = selectedStationId,
                onStationSelect = onStationSelect
            )

            // Top Key Metrics Row: Gauge Chart + Active Personnel Card + Equipment Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular Readiness Gauge Chart
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ReadinessGaugeChart(
                        readinessPercentage = summary.equipmentReadinessPercentage,
                        operationalScore = summary.overallOperationalScore
                    )
                }

                // Metric Cards Column
                Column(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Active Firefighters Metric Card
                    ActiveFirefightersMetricCard(summary = summary)

                    // Equipment Counts Metric Card
                    EquipmentStatusSummaryCard(summary = summary)
                }
            }

            // Personnel Staffing Distribution Bar (Recharts-style Stacked Bar)
            PersonnelStaffingDistributionBar(summary = summary)

            // Equipment Category Readiness Chart (Expandable)
            EquipmentCategoryChartSection(
                categoryList = summary.categoryReadinessList,
                isExpanded = showCategoryBreakdown,
                onToggleExpand = { showCategoryBreakdown = !showCategoryBreakdown }
            )

            // Urgent Maintenance Alerts Section
            UrgentMaintenanceAlertsSection(
                alerts = summary.urgentMaintenanceAlerts,
                selectedFilter = selectedAlertFilter,
                onFilterChange = { selectedAlertFilter = it },
                onApparatusClick = onApparatusClick,
                onDeficiencyClick = onDeficiencyClick,
                onEquipmentClick = onEquipmentClick
            )
        }
    }
}

@Composable
private fun StationHeaderSection(
    summary: StationReadinessSummary,
    stations: List<Station>,
    selectedStationId: String?,
    onStationSelect: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Station Readiness & Operations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedStationId == null) {
                        "Department-wide overview across all stations"
                    } else {
                        summary.stationAddress ?: "Station location active"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Operational Readiness Badge
            val scoreColor = when {
                summary.overallOperationalScore >= 85 -> Color(0xFF2E7D32)
                summary.overallOperationalScore >= 70 -> Color(0xFFE65100)
                else -> MaterialTheme.colorScheme.error
            }
            Surface(
                color = scoreColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(scoreColor)
                    )
                    Text(
                        text = "${summary.overallOperationalScore}% OPS READY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }
        }

        // Station Selection Filter Chips
        if (stations.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStationId == null,
                        onClick = { onStationSelect(null) },
                        label = { Text("All Stations (${stations.size})") }
                    )
                }
                items(stations) { station ->
                    FilterChip(
                        selected = selectedStationId == station.id,
                        onClick = { onStationSelect(station.id) },
                        label = { Text(station.name) }
                    )
                }
            }
        }
    }
}

/**
 * Recharts-style Circular Donut / Gauge Chart in Jetpack Compose
 */
@Composable
fun ReadinessGaugeChart(
    readinessPercentage: Float,
    operationalScore: Int,
    modifier: Modifier = Modifier
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = readinessPercentage,
        animationSpec = tween(durationMillis = 800)
    )

    val gaugeColor = when {
        readinessPercentage >= 90f -> Color(0xFF2E7D32)
        readinessPercentage >= 75f -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.error
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Background Track Arc (260 degrees)
                    drawArc(
                        color = trackColor,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Foreground Value Arc
                    val sweep = (animatedPercentage / 100f) * 260f
                    if (sweep > 0f) {
                        drawArc(
                            color = gaugeColor,
                            startAngle = 140f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Value Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedPercentage.roundToInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = gaugeColor
                    )
                    Text(
                        text = "EQUIPMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    readinessPercentage >= 90f -> "Equipment Ready"
                    readinessPercentage >= 75f -> "Check Deficiencies"
                    else -> "Maintenance Critical"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = gaugeColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActiveFirefightersMetricCard(summary: StationReadinessSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "ACTIVE FIREFIGHTERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${summary.activeFirefighters}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = "/ ${summary.totalFirefighters} roster",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Surface(
                color = Color(0xFF2E7D32),
                shape = CircleShape
            ) {
                Text(
                    text = "${summary.readyToRespondCount} on-call",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EquipmentStatusSummaryCard(summary: StationReadinessSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "EQUIPMENT STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${summary.inServiceEquipment} In Service · ${summary.totalEquipment} Total",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            if (summary.maintenanceRequiredEquipment > 0 || summary.outOfServiceEquipment > 0) {
                Surface(
                    color = if (summary.outOfServiceEquipment > 0) MaterialTheme.colorScheme.errorContainer else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (summary.outOfServiceEquipment > 0) {
                            "${summary.outOfServiceEquipment} OOS"
                        } else {
                            "${summary.maintenanceRequiredEquipment} Maint"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.outOfServiceEquipment > 0) MaterialTheme.colorScheme.onErrorContainer else Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

/**
 * Recharts-style multi-color stacked bar for personnel status distribution
 */
@Composable
private fun PersonnelStaffingDistributionBar(summary: StationReadinessSummary) {
    val total = summary.totalFirefighters.coerceAtLeast(1).toFloat()
    val availablePct = (summary.firefighterStatusCounts[PersonnelStatus.AVAILABLE] ?: 0) / total
    val standbyPct = (summary.firefighterStatusCounts[PersonnelStatus.STATION_STANDBY] ?: 0) / total
    val respondingPct = ((summary.firefighterStatusCounts[PersonnelStatus.RESPONDING] ?: 0) +
        (summary.firefighterStatusCounts[PersonnelStatus.ON_SCENE] ?: 0)) / total
    val trainingPct = (summary.firefighterStatusCounts[PersonnelStatus.TRAINING] ?: 0) / total
    val offDutyPct = (summary.unavailableCount) / total

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Personnel Staffing Distribution",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(availablePct * 100).roundToInt()}% ready now",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )
        }

        // Multi-segment Proportional Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (availablePct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(availablePct)
                            .fillMaxHeight()
                            .background(Color(0xFF4CAF50))
                    )
                }
                if (standbyPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(standbyPct)
                            .fillMaxHeight()
                            .background(Color(0xFF81C784))
                    )
                }
                if (respondingPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(respondingPct)
                            .fillMaxHeight()
                            .background(Color(0xFF2196F3))
                    )
                }
                if (trainingPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(trainingPct)
                            .fillMaxHeight()
                            .background(Color(0xFFAB47BC))
                    )
                }
                if (offDutyPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(offDutyPct)
                            .fillMaxHeight()
                            .background(Color(0xFFBDBDBD))
                    )
                }
            }
        }

        // Legend Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LegendPill(
                color = Color(0xFF4CAF50),
                label = "Available (${summary.firefighterStatusCounts[PersonnelStatus.AVAILABLE] ?: 0})"
            )
            if ((summary.firefighterStatusCounts[PersonnelStatus.STATION_STANDBY] ?: 0) > 0) {
                LegendPill(
                    color = Color(0xFF81C784),
                    label = "Standby (${summary.firefighterStatusCounts[PersonnelStatus.STATION_STANDBY] ?: 0})"
                )
            }
            if ((summary.firefighterStatusCounts[PersonnelStatus.RESPONDING] ?: 0) + (summary.firefighterStatusCounts[PersonnelStatus.ON_SCENE] ?: 0) > 0) {
                LegendPill(
                    color = Color(0xFF2196F3),
                    label = "Active Call (${(summary.firefighterStatusCounts[PersonnelStatus.RESPONDING] ?: 0) + (summary.firefighterStatusCounts[PersonnelStatus.ON_SCENE] ?: 0)})"
                )
            }
            LegendPill(
                color = Color(0xFFBDBDBD),
                label = "Off Duty (${summary.unavailableCount})"
            )
        }
    }
}

@Composable
private fun LegendPill(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Recharts-style Horizontal Bar Chart for Equipment Category Readiness
 */
@Composable
private fun EquipmentCategoryChartSection(
    categoryList: List<CategoryReadiness>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    if (categoryList.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Equipment Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${categoryList.size} categories",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            TextButton(onClick = onToggleExpand) {
                Text(if (isExpanded) "Hide Chart" else "Show Chart")
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categoryList.forEach { categoryItem ->
                    CategoryBarItem(item = categoryItem)
                }
            }
        }
    }
}

@Composable
private fun CategoryBarItem(item: CategoryReadiness) {
    val barColor = when {
        item.readinessPercentage >= 100f -> Color(0xFF2E7D32)
        item.readinessPercentage >= 75f -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.error
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.category.name.replace('_', ' '),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${item.inServiceCount}/${item.totalCount} in-service (${item.readinessPercentage.roundToInt()}%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }

        // Horizontal Bar Chart Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val fraction = (item.readinessPercentage / 100f).coerceIn(0f, 1f)
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }
        }
    }
}

/**
 * Urgent Maintenance Alerts Section displaying critical OOS equipment,
 * overdue inspections, and maintenance requests.
 */
@Composable
private fun UrgentMaintenanceAlertsSection(
    alerts: List<UrgentMaintenanceAlert>,
    selectedFilter: AlertCategory?,
    onFilterChange: (AlertCategory?) -> Unit,
    onApparatusClick: (String) -> Unit,
    onDeficiencyClick: (String) -> Unit,
    onEquipmentClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Urgent Maintenance Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (alerts.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${alerts.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (alerts.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                    Column {
                        Text(
                            text = "All Systems & Equipment Operational",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "No out-of-service apparatus, failed equipment, or overdue inspections.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        } else {
            // Filter Pills for Alerts
            val filteredAlerts = if (selectedFilter == null) {
                alerts
            } else {
                alerts.filter { it.category == selectedFilter }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { onFilterChange(null) },
                        label = { Text("All (${alerts.size})") }
                    )
                }
                val distinctCategories = alerts.map { it.category }.distinct()
                items(distinctCategories) { cat ->
                    val count = alerts.count { it.category == cat }
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { onFilterChange(if (selectedFilter == cat) null else cat) },
                        label = { Text("${cat.label} ($count)") }
                    )
                }
            }

            // Alert Items List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredAlerts.forEach { alert ->
                    UrgentAlertCard(
                        alert = alert,
                        onApparatusClick = onApparatusClick,
                        onDeficiencyClick = onDeficiencyClick,
                        onEquipmentClick = onEquipmentClick
                    )
                }
            }
        }
    }
}

@Composable
private fun UrgentAlertCard(
    alert: UrgentMaintenanceAlert,
    onApparatusClick: (String) -> Unit,
    onDeficiencyClick: (String) -> Unit,
    onEquipmentClick: (String) -> Unit
) {
    val containerColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
        AlertSeverity.HIGH -> Color(0xFFFFF3E0)
        AlertSeverity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
        AlertSeverity.HIGH -> Color(0xFFE65100)
        AlertSeverity.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Severity Indicator Badge
            Surface(
                color = contentColor,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = alert.severity.label.uppercase(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Alert details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }

                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Location / Apparatus Subtitle
                val locationParts = listOfNotNull(alert.stationName, alert.apparatusName)
                if (locationParts.isNotEmpty()) {
                    Text(
                        text = "📍 " + locationParts.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Direct Action Button
            Button(
                onClick = {
                    when {
                        alert.apparatusId != null -> onApparatusClick(alert.apparatusId)
                        alert.deficiencyId != null -> onDeficiencyClick(alert.deficiencyId)
                        alert.equipmentId != null -> onEquipmentClick(alert.equipmentId)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = alert.actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
