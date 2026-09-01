package com.example.firestationops.domain.dashboard

import com.example.firestationops.domain.model.Apparatus
import com.example.firestationops.domain.model.ApparatusInspectionStatus
import com.example.firestationops.domain.model.ApparatusStatus
import com.example.firestationops.domain.model.Deficiency
import com.example.firestationops.domain.model.DeficiencySeverity
import com.example.firestationops.domain.model.DeficiencyStatus
import com.example.firestationops.domain.model.InspectionComplianceStatus
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.EquipmentStatus
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Station
import com.example.firestationops.ui.dashboard.OverdueInspectionItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StationStatusAnalyticsTest {

    @Test
    fun computeSummary_calculatesCorrectActiveFirefighterCountAndStaffing() {
        val stations = listOf(
            Station(id = "st1", departmentId = "dept1", name = "Station 51")
        )
        val firefighters = listOf(
            Firefighter(id = "ff1", departmentId = "dept1", stationId = "st1", firstName = "John", lastName = "Gage", status = PersonnelStatus.AVAILABLE),
            Firefighter(id = "ff2", departmentId = "dept1", stationId = "st1", firstName = "Roy", lastName = "DeSoto", status = PersonnelStatus.RESPONDING),
            Firefighter(id = "ff3", departmentId = "dept1", stationId = "st1", firstName = "Chet", lastName = "Kelly", status = PersonnelStatus.ON_SCENE),
            Firefighter(id = "ff4", departmentId = "dept1", stationId = "st1", firstName = "Marco", lastName = "Lopez", status = PersonnelStatus.UNAVAILABLE),
            Firefighter(id = "ff5", departmentId = "dept1", stationId = "st1", firstName = "Mike", lastName = "Stoker", status = PersonnelStatus.LEAVE)
        )

        val summary = StationStatusAnalytics.computeSummary(
            selectedStationId = null,
            stations = stations,
            firefighters = firefighters,
            equipmentList = emptyList(),
            apparatusList = emptyList(),
            openDeficiencies = emptyList(),
            overdueInspections = emptyList()
        )

        // 3 active: AVAILABLE, RESPONDING, ON_SCENE
        assertEquals(3, summary.activeFirefighters)
        assertEquals(5, summary.totalFirefighters)
        assertEquals(1, summary.firefighterStatusCounts[PersonnelStatus.AVAILABLE])
        assertEquals(1, summary.firefighterStatusCounts[PersonnelStatus.RESPONDING])
        assertEquals(1, summary.firefighterStatusCounts[PersonnelStatus.ON_SCENE])
        assertEquals(1, summary.firefighterStatusCounts[PersonnelStatus.UNAVAILABLE])
        assertEquals(1, summary.firefighterStatusCounts[PersonnelStatus.LEAVE])
    }

    @Test
    fun computeSummary_calculatesEquipmentReadinessPercentageCorrectly() {
        val equipment = listOf(
            Equipment(id = "eq1", departmentId = "dept1", name = "SCBA 1", category = EquipmentCategory.SCBA, status = EquipmentStatus.IN_SERVICE),
            Equipment(id = "eq2", departmentId = "dept1", name = "SCBA 2", category = EquipmentCategory.SCBA, status = EquipmentStatus.IN_SERVICE),
            Equipment(id = "eq3", departmentId = "dept1", name = "Tool 1", category = EquipmentCategory.HAND_TOOL, status = EquipmentStatus.OUT_OF_SERVICE),
            Equipment(id = "eq4", departmentId = "dept1", name = "Radio 1", category = EquipmentCategory.RADIO, status = EquipmentStatus.MAINTENANCE_REQUIRED)
        )

        val summary = StationStatusAnalytics.computeSummary(
            selectedStationId = null,
            stations = emptyList(),
            firefighters = emptyList(),
            equipmentList = equipment,
            apparatusList = emptyList(),
            openDeficiencies = emptyList(),
            overdueInspections = emptyList()
        )

        // 2 out of 4 in service = 50.0%
        assertEquals(50f, summary.equipmentReadinessPercentage)
        assertEquals(2, summary.inServiceEquipment)
        assertEquals(4, summary.totalEquipment)
        assertEquals(1, summary.outOfServiceEquipment)
        assertEquals(1, summary.maintenanceRequiredEquipment)
    }

    @Test
    fun computeSummary_identifiesUrgentMaintenanceAlerts() {
        val outOfServiceApparatus = Apparatus(
            id = "app_1",
            departmentId = "dept1",
            stationId = "st1",
            name = "Engine 51",
            radioName = "Engine 51",
            type = "ENGINE",
            status = ApparatusStatus.OUT_OF_SERVICE
        )
        val urgentDeficiency = Deficiency(
            id = "def_1",
            departmentId = "dept1",
            apparatusId = "app_1",
            inspectionId = "insp_1",
            title = "Air brake leak",
            description = "Pressure drops under 60 PSI",
            severity = DeficiencySeverity.OUT_OF_SERVICE,
            status = DeficiencyStatus.OPEN,
            createdAt = 1000L,
            createdByUserId = "user_1"
        )
        val overdueInspection = OverdueInspectionItem(
            apparatus = outOfServiceApparatus,
            compliance = ApparatusInspectionStatus(
                apparatusId = "app_1",
                templateId = "t1",
                templateName = "Daily Engine Check",
                status = InspectionComplianceStatus.OVERDUE,
                lastCompletedAt = null,
                dueAt = 500L,
                daysOverdue = 2
            )
        )
        val oosEquipment = Equipment(
            id = "eq_1",
            departmentId = "dept1",
            name = "Jaws of Life",
            category = EquipmentCategory.HYDRAULIC_RESCUE,
            status = EquipmentStatus.OUT_OF_SERVICE,
            notes = "Hydraulic fluid line ruptured"
        )

        val summary = StationStatusAnalytics.computeSummary(
            selectedStationId = null,
            stations = listOf(Station(id = "st1", departmentId = "dept1", name = "Station 51")),
            firefighters = emptyList(),
            equipmentList = listOf(oosEquipment),
            apparatusList = listOf(outOfServiceApparatus),
            openDeficiencies = listOf(urgentDeficiency),
            overdueInspections = listOf(overdueInspection)
        )

        assertTrue(summary.urgentMaintenanceAlerts.isNotEmpty())
        assertEquals(4, summary.urgentMaintenanceAlerts.size)
        assertEquals(1, summary.outOfServiceApparatus)
    }
}
