package com.example.firestationops.domain.dashboard

import com.example.firestationops.domain.model.Apparatus
import com.example.firestationops.domain.model.ApparatusStatus
import com.example.firestationops.domain.model.Deficiency
import com.example.firestationops.domain.model.DeficiencySeverity
import com.example.firestationops.domain.model.InspectionComplianceStatus
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.EquipmentStatus
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Station
import com.example.firestationops.ui.dashboard.OverdueInspectionItem

/**
 * Severity level of an urgent maintenance alert.
 */
enum class AlertSeverity(val label: String, val sortOrder: Int) {
    CRITICAL("Critical", 0),
    HIGH("High", 1),
    MEDIUM("Medium", 2)
}

/**
 * Categorization of the maintenance alert.
 */
enum class AlertCategory(val label: String) {
    EQUIPMENT_OUT_OF_SERVICE("Equipment OOS"),
    EQUIPMENT_MAINTENANCE("Equipment Maintenance"),
    APPARATUS_OUT_OF_SERVICE("Apparatus OOS"),
    APPARATUS_OVERDUE_INSPECTION("Overdue Inspection"),
    DEFICIENCY_OOS("Critical Deficiency"),
    DEFICIENCY_REPAIR("Repair Needed")
}

/**
 * Represents an urgent maintenance alert requiring officer or firefighter attention.
 */
data class UrgentMaintenanceAlert(
    val id: String,
    val title: String,
    val category: AlertCategory,
    val severity: AlertSeverity,
    val description: String,
    val stationId: String? = null,
    val stationName: String? = null,
    val apparatusId: String? = null,
    val apparatusName: String? = null,
    val equipmentId: String? = null,
    val deficiencyId: String? = null,
    val createdAt: Long? = null,
    val actionLabel: String = "View"
)

/**
 * Readiness metrics for an individual equipment category.
 */
data class CategoryReadiness(
    val category: EquipmentCategory,
    val inServiceCount: Int,
    val totalCount: Int,
    val readinessPercentage: Float
)

/**
 * Comprehensive station status summary and readiness metrics.
 */
data class StationReadinessSummary(
    val stationId: String?,
    val stationName: String,
    val stationAddress: String?,
    val totalFirefighters: Int,
    val activeFirefighters: Int,
    val readyToRespondCount: Int,
    val activelyEngagedCount: Int,
    val unavailableCount: Int,
    val firefighterStatusCounts: Map<PersonnelStatus, Int>,
    val totalEquipment: Int,
    val inServiceEquipment: Int,
    val maintenanceRequiredEquipment: Int,
    val outOfServiceEquipment: Int,
    val equipmentReadinessPercentage: Float,
    val categoryReadinessList: List<CategoryReadiness>,
    val totalApparatus: Int,
    val inServiceApparatus: Int,
    val outOfServiceApparatus: Int,
    val apparatusReadinessPercentage: Float,
    val urgentMaintenanceAlerts: List<UrgentMaintenanceAlert>,
    val overallOperationalScore: Int
)

/**
 * Computes station status summaries, equipment readiness, active personnel counts,
 * and urgent maintenance alerts.
 */
object StationStatusAnalytics {

    fun computeSummary(
        selectedStationId: String?,
        stations: List<Station>,
        firefighters: List<Firefighter>,
        equipmentList: List<Equipment>,
        apparatusList: List<Apparatus>,
        openDeficiencies: List<Deficiency> = emptyList(),
        overdueInspections: List<OverdueInspectionItem> = emptyList()
    ): StationReadinessSummary {
        val targetStation = selectedStationId?.let { sId -> stations.find { it.id == sId } }
        val stationName = targetStation?.name ?: "All Department Stations"
        val stationAddress = targetStation?.address

        // Filter records by station if a specific station is selected
        val filteredFirefighters = if (selectedStationId == null) {
            firefighters
        } else {
            firefighters.filter { it.stationId == selectedStationId }
        }

        val filteredEquipment = if (selectedStationId == null) {
            equipmentList
        } else {
            equipmentList.filter { it.stationId == selectedStationId }
        }

        val filteredApparatus = if (selectedStationId == null) {
            apparatusList
        } else {
            apparatusList.filter { it.stationId == selectedStationId }
        }

        val apparatusMap = apparatusList.associateBy { it.id }
        val stationsMap = stations.associateBy { it.id }

        // Personnel status calculations
        val firefighterStatusCounts = mutableMapOf<PersonnelStatus, Int>()
        PersonnelStatus.entries.forEach { status ->
            firefighterStatusCounts[status] = 0
        }
        filteredFirefighters.forEach { ff ->
            firefighterStatusCounts[ff.status] = (firefighterStatusCounts[ff.status] ?: 0) + 1
        }

        val readyToRespondCount = filteredFirefighters.count { it.status.isReadyToRespond }
        val activelyEngagedCount = filteredFirefighters.count { it.status.isActivelyEngaged }
        val activeFirefighters = readyToRespondCount + activelyEngagedCount
        val totalFirefighters = filteredFirefighters.size
        val unavailableCount = totalFirefighters - activeFirefighters

        // Equipment readiness calculations
        val nonRetiredEquipment = filteredEquipment.filter { it.status != EquipmentStatus.RETIRED }
        val totalEquipment = nonRetiredEquipment.size
        val inServiceEquipment = nonRetiredEquipment.count { it.status == EquipmentStatus.IN_SERVICE }
        val maintenanceRequiredEquipment = nonRetiredEquipment.count { it.status == EquipmentStatus.MAINTENANCE_REQUIRED }
        val outOfServiceEquipment = nonRetiredEquipment.count { it.status == EquipmentStatus.OUT_OF_SERVICE }

        val equipmentReadinessPercentage = if (totalEquipment > 0) {
            (inServiceEquipment.toFloat() / totalEquipment.toFloat()) * 100f
        } else {
            100f
        }

        // Category breakdown
        val categoryReadinessList = EquipmentCategory.entries.mapNotNull { category ->
            val catItems = nonRetiredEquipment.filter { it.category == category }
            if (catItems.isEmpty()) {
                null
            } else {
                val catInService = catItems.count { it.status == EquipmentStatus.IN_SERVICE }
                val catTotal = catItems.size
                val catPct = (catInService.toFloat() / catTotal.toFloat()) * 100f
                CategoryReadiness(
                    category = category,
                    inServiceCount = catInService,
                    totalCount = catTotal,
                    readinessPercentage = catPct
                )
            }
        }.sortedBy { it.readinessPercentage }

        // Apparatus readiness calculations
        val totalApparatus = filteredApparatus.size
        val inServiceApparatus = filteredApparatus.count { it.status == ApparatusStatus.IN_SERVICE }
        val outOfServiceApparatus = filteredApparatus.count { it.status == ApparatusStatus.OUT_OF_SERVICE }
        val apparatusReadinessPercentage = if (totalApparatus > 0) {
            (inServiceApparatus.toFloat() / totalApparatus.toFloat()) * 100f
        } else {
            100f
        }

        // Urgent maintenance alerts collection
        val alerts = mutableListOf<UrgentMaintenanceAlert>()

        // 1. Equipment out of service
        filteredEquipment.filter { it.status == EquipmentStatus.OUT_OF_SERVICE }.forEach { eq ->
            alerts.add(
                UrgentMaintenanceAlert(
                    id = "alert_eq_oos_${eq.id}",
                    title = eq.name,
                    category = AlertCategory.EQUIPMENT_OUT_OF_SERVICE,
                    severity = AlertSeverity.CRITICAL,
                    description = eq.notes?.takeIf { it.isNotBlank() } ?: "Equipment marked Out of Service (SN: ${eq.serialNumber ?: "N/A"})",
                    stationId = eq.stationId,
                    stationName = eq.stationId?.let { stationsMap[it]?.name },
                    apparatusId = eq.apparatusId,
                    apparatusName = eq.apparatusId?.let { apparatusMap[it]?.radioName },
                    equipmentId = eq.id,
                    actionLabel = "Inspect Item"
                )
            )
        }

        // 2. Equipment maintenance required
        filteredEquipment.filter { it.status == EquipmentStatus.MAINTENANCE_REQUIRED }.forEach { eq ->
            alerts.add(
                UrgentMaintenanceAlert(
                    id = "alert_eq_maint_${eq.id}",
                    title = eq.name,
                    category = AlertCategory.EQUIPMENT_MAINTENANCE,
                    severity = AlertSeverity.HIGH,
                    description = eq.notes?.takeIf { it.isNotBlank() } ?: "Maintenance or inspection check required (SN: ${eq.serialNumber ?: "N/A"})",
                    stationId = eq.stationId,
                    stationName = eq.stationId?.let { stationsMap[it]?.name },
                    apparatusId = eq.apparatusId,
                    apparatusName = eq.apparatusId?.let { apparatusMap[it]?.radioName },
                    equipmentId = eq.id,
                    actionLabel = "Service Item"
                )
            )
        }

        // 3. Apparatus Out of Service
        filteredApparatus.filter { it.status == ApparatusStatus.OUT_OF_SERVICE }.forEach { app ->
            alerts.add(
                UrgentMaintenanceAlert(
                    id = "alert_app_oos_${app.id}",
                    title = app.radioName,
                    category = AlertCategory.APPARATUS_OUT_OF_SERVICE,
                    severity = AlertSeverity.CRITICAL,
                    description = "${app.type} is marked Out of Service and cannot respond to alarms",
                    stationId = app.stationId,
                    stationName = app.stationId?.let { stationsMap[it]?.name },
                    apparatusId = app.id,
                    apparatusName = app.radioName,
                    actionLabel = "View Apparatus"
                )
            )
        }

        // 4. Overdue Apparatus Inspections
        val filteredOverdue = if (selectedStationId == null) {
            overdueInspections
        } else {
            overdueInspections.filter { it.apparatus.stationId == selectedStationId }
        }

        filteredOverdue.forEach { overdue ->
            val days = overdue.compliance.daysOverdue
            val desc = if (overdue.compliance.status == InspectionComplianceStatus.NEVER_INSPECTED) {
                "Apparatus has never been inspected with active checklist"
            } else if (days > 0) {
                "Daily apparatus inspection is $days day(s) overdue"
            } else {
                "Daily apparatus inspection is overdue"
            }

            alerts.add(
                UrgentMaintenanceAlert(
                    id = "alert_app_insp_${overdue.apparatus.id}",
                    title = "${overdue.apparatus.radioName} Inspection",
                    category = AlertCategory.APPARATUS_OVERDUE_INSPECTION,
                    severity = if (days >= 2) AlertSeverity.CRITICAL else AlertSeverity.HIGH,
                    description = desc,
                    stationId = overdue.apparatus.stationId,
                    stationName = overdue.apparatus.stationId?.let { stationsMap[it]?.name },
                    apparatusId = overdue.apparatus.id,
                    apparatusName = overdue.apparatus.radioName,
                    actionLabel = "Start Inspection"
                )
            )
        }

        // 5. Critical Deficiencies
        val filteredDeficiencies = if (selectedStationId == null) {
            openDeficiencies
        } else {
            openDeficiencies.filter { def ->
                val app = apparatusMap[def.apparatusId]
                app?.stationId == selectedStationId
            }
        }

        filteredDeficiencies.forEach { def ->
            val app = apparatusMap[def.apparatusId]
            if (def.severity == DeficiencySeverity.OUT_OF_SERVICE) {
                alerts.add(
                    UrgentMaintenanceAlert(
                        id = "alert_def_oos_${def.id}",
                        title = def.title,
                        category = AlertCategory.DEFICIENCY_OOS,
                        severity = AlertSeverity.CRITICAL,
                        description = def.description,
                        stationId = app?.stationId,
                        stationName = app?.stationId?.let { stationsMap[it]?.name },
                        apparatusId = def.apparatusId,
                        apparatusName = app?.radioName,
                        deficiencyId = def.id,
                        createdAt = def.createdAt,
                        actionLabel = "Resolve OOS"
                    )
                )
            } else if (def.severity == DeficiencySeverity.REPAIR_NEEDED) {
                alerts.add(
                    UrgentMaintenanceAlert(
                        id = "alert_def_repair_${def.id}",
                        title = def.title,
                        category = AlertCategory.DEFICIENCY_REPAIR,
                        severity = AlertSeverity.HIGH,
                        description = def.description,
                        stationId = app?.stationId,
                        stationName = app?.stationId?.let { stationsMap[it]?.name },
                        apparatusId = def.apparatusId,
                        apparatusName = app?.radioName,
                        deficiencyId = def.id,
                        createdAt = def.createdAt,
                        actionLabel = "View Repair"
                    )
                )
            }
        }

        // Sort alerts by severity first, then by title
        val sortedAlerts = alerts.sortedWith(
            compareBy<UrgentMaintenanceAlert> { it.severity.sortOrder }
                .thenBy { it.title }
        )

        // Overall operational score (0-100)
        val staffingScore = if (totalFirefighters > 0) {
            ((activeFirefighters.toFloat() / totalFirefighters.toFloat()) * 100f).toInt()
        } else 100

        val overallScore = ((equipmentReadinessPercentage * 0.45f) +
            (apparatusReadinessPercentage * 0.40f) +
            (staffingScore * 0.15f)).toInt().coerceIn(0, 100)

        return StationReadinessSummary(
            stationId = selectedStationId,
            stationName = stationName,
            stationAddress = stationAddress,
            totalFirefighters = totalFirefighters,
            activeFirefighters = activeFirefighters,
            readyToRespondCount = readyToRespondCount,
            activelyEngagedCount = activelyEngagedCount,
            unavailableCount = unavailableCount,
            firefighterStatusCounts = firefighterStatusCounts,
            totalEquipment = totalEquipment,
            inServiceEquipment = inServiceEquipment,
            maintenanceRequiredEquipment = maintenanceRequiredEquipment,
            outOfServiceEquipment = outOfServiceEquipment,
            equipmentReadinessPercentage = equipmentReadinessPercentage,
            categoryReadinessList = categoryReadinessList,
            totalApparatus = totalApparatus,
            inServiceApparatus = inServiceApparatus,
            outOfServiceApparatus = outOfServiceApparatus,
            apparatusReadinessPercentage = apparatusReadinessPercentage,
            urgentMaintenanceAlerts = sortedAlerts,
            overallOperationalScore = overallScore
        )
    }
}
