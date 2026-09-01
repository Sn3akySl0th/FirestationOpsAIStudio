package com.example.firestationops.ui.equipment

import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.EquipmentStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EquipmentScreenTest {

    private val sampleEquipmentList = listOf(
        Equipment(
            id = "eq_01",
            departmentId = "dept_test",
            stationId = "station_1",
            apparatusId = "app_engine_1",
            name = "Scott Air-Pak X3 SCBA #1",
            category = EquipmentCategory.SCBA,
            serialNumber = "SCBA-2024-001",
            barcode = "BC-00192",
            status = EquipmentStatus.IN_SERVICE
        ),
        Equipment(
            id = "eq_02",
            departmentId = "dept_test",
            stationId = "station_1",
            apparatusId = "app_engine_1",
            name = "Holmatro Combi Tool (Jaws)",
            category = EquipmentCategory.HYDRAULIC_RESCUE,
            serialNumber = "H-COMB-882",
            barcode = "BC-00882",
            status = EquipmentStatus.IN_SERVICE
        ),
        Equipment(
            id = "eq_03",
            departmentId = "dept_test",
            stationId = "station_1",
            apparatusId = "app_truck_1",
            name = "FLIR K55 Thermal Imaging Camera",
            category = EquipmentCategory.THERMAL_IMAGING,
            serialNumber = "TIC-9932-B",
            barcode = "BC-00993",
            status = EquipmentStatus.IN_SERVICE
        ),
        Equipment(
            id = "eq_04",
            departmentId = "dept_test",
            stationId = "station_1",
            apparatusId = "app_engine_1",
            name = "Super-Vac Gas Smoke Ejector Fan",
            category = EquipmentCategory.VENTILATION,
            serialNumber = "SV-FAN-44",
            barcode = "BC-00441",
            status = EquipmentStatus.MAINTENANCE_REQUIRED,
            notes = "Carburetor requires cleaning and spark plug check"
        ),
        Equipment(
            id = "eq_05",
            departmentId = "dept_test",
            stationId = "station_1",
            name = "Reserve 2.5\" Hose Bundle",
            category = EquipmentCategory.HOSE,
            serialNumber = "HOSE-25-10",
            status = EquipmentStatus.RESERVE
        ),
        Equipment(
            id = "eq_06",
            departmentId = "dept_test",
            stationId = "station_1",
            name = "Old Generator Unit",
            category = EquipmentCategory.GENERATOR,
            serialNumber = "GEN-99-OLD",
            status = EquipmentStatus.OUT_OF_SERVICE,
            notes = "Alternator burnt out"
        )
    )

    @Test
    fun testOperationalStatusCounters() {
        val inServiceCount = sampleEquipmentList.count { it.status == EquipmentStatus.IN_SERVICE }
        val maintenanceCount = sampleEquipmentList.count { it.status == EquipmentStatus.MAINTENANCE_REQUIRED }
        val outOfServiceCount = sampleEquipmentList.count { it.status == EquipmentStatus.OUT_OF_SERVICE }
        val reserveCount = sampleEquipmentList.count { it.status == EquipmentStatus.RESERVE }
        val attentionCount = sampleEquipmentList.count { it.status.requiresAttention }

        assertEquals(3, inServiceCount)
        assertEquals(1, maintenanceCount)
        assertEquals(1, outOfServiceCount)
        assertEquals(1, reserveCount)
        assertEquals(2, attentionCount) // MAINTENANCE_REQUIRED + OUT_OF_SERVICE
    }

    @Test
    fun testEquipmentOperationalReadiness() {
        val scba = sampleEquipmentList[0]
        assertTrue(scba.isOperational)
        assertFalse(scba.status.requiresAttention)

        val fan = sampleEquipmentList[3]
        assertFalse(fan.isOperational)
        assertTrue(fan.status.requiresAttention)
        assertEquals("Carburetor requires cleaning and spark plug check", fan.notes)
    }

    @Test
    fun testCategoryFiltering() {
        val scbaList = sampleEquipmentList.filter { it.category == EquipmentCategory.SCBA }
        assertEquals(1, scbaList.size)
        assertEquals("Scott Air-Pak X3 SCBA #1", scbaList.first().name)

        val rescueList = sampleEquipmentList.filter { it.category == EquipmentCategory.HYDRAULIC_RESCUE }
        assertEquals(1, rescueList.size)
        assertEquals("Holmatro Combi Tool (Jaws)", rescueList.first().name)
    }

    @Test
    fun testSearchByNameAndSerialNumber() {
        val search1 = "flir"
        val matchName = sampleEquipmentList.filter { it.name.lowercase().contains(search1) }
        assertEquals(1, matchName.size)
        assertEquals("FLIR K55 Thermal Imaging Camera", matchName.first().name)

        val search2 = "sv-fan"
        val matchSn = sampleEquipmentList.filter { it.serialNumber?.lowercase()?.contains(search2) == true }
        assertEquals(1, matchSn.size)
        assertEquals("Super-Vac Gas Smoke Ejector Fan", matchSn.first().name)
    }

    @Test
    fun testMaintenanceNeedsIdentification() {
        val needsMaintenance = sampleEquipmentList.filter {
            it.status == EquipmentStatus.MAINTENANCE_REQUIRED ||
                it.status == EquipmentStatus.OUT_OF_SERVICE ||
                !it.notes.isNullOrBlank()
        }

        assertEquals(2, needsMaintenance.size)
        assertTrue(needsMaintenance.any { it.name.contains("Super-Vac") })
        assertTrue(needsMaintenance.any { it.name.contains("Old Generator") })
    }
}
