package com.example.firestationops.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelSchemaTest {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Test
    fun station_instantiationAndSerialization() {
        val station = Station(
            id = "sta_101",
            departmentId = "dept_calhoun",
            stationNumber = "1",
            name = "Main Station",
            address = "100 Firehouse Lane",
            phoneNumber = "555-0199",
            isActive = true,
            apparatusIds = listOf("app_e1", "app_t1")
        )

        assertEquals("sta_101", station.id)
        assertEquals("Main Station", station.name)
        assertEquals(2, station.apparatusIds.size)

        val serialized = json.encodeToString(station)
        val deserialized = json.decodeFromString<Station>(serialized)

        assertEquals(station, deserialized)
    }

    @Test
    fun firefighter_instantiationAndFullName() {
        val firefighter = Firefighter(
            id = "ff_01",
            departmentId = "dept_calhoun",
            stationId = "sta_101",
            badgeNumber = "104",
            firstName = "Sarah",
            lastName = "Connor",
            rank = "Captain",
            email = "sconnor@calhounvfd.org",
            phone = "555-0123",
            certifications = listOf("FF2", "EMT-B", "Hazmat Ops"),
            isOfficer = true,
            isActive = true
        )

        assertEquals("Sarah Connor", firefighter.fullName)
        assertTrue(firefighter.isOfficer)
        assertEquals(PersonnelStatus.AVAILABLE, firefighter.status)
        assertTrue(firefighter.isReadyToRespond)
        assertEquals(3, firefighter.certifications.size)

        val offDuty = firefighter.copy(status = PersonnelStatus.UNAVAILABLE)
        assertFalse(offDuty.isReadyToRespond)

        val serialized = json.encodeToString(firefighter)
        val deserialized = json.decodeFromString<Firefighter>(serialized)

        assertEquals(firefighter, deserialized)
    }

    @Test
    fun personnelStatus_readinessAndEngagement() {
        assertTrue(PersonnelStatus.AVAILABLE.isReadyToRespond)
        assertFalse(PersonnelStatus.AVAILABLE.isActivelyEngaged)

        assertFalse(PersonnelStatus.RESPONDING.isReadyToRespond)
        assertTrue(PersonnelStatus.RESPONDING.isActivelyEngaged)

        assertFalse(PersonnelStatus.ON_SCENE.isReadyToRespond)
        assertTrue(PersonnelStatus.ON_SCENE.isActivelyEngaged)

        assertTrue(PersonnelStatus.STATION_STANDBY.isReadyToRespond)
        assertFalse(PersonnelStatus.STATION_STANDBY.isActivelyEngaged)

        assertFalse(PersonnelStatus.UNAVAILABLE.isReadyToRespond)
        assertFalse(PersonnelStatus.LEAVE.isReadyToRespond)
        assertFalse(PersonnelStatus.RETIRED.isReadyToRespond)
    }

    @Test
    fun equipmentStatus_readinessAndAttentionRequirements() {
        assertTrue(EquipmentStatus.IN_SERVICE.isOperational)
        assertFalse(EquipmentStatus.IN_SERVICE.requiresAttention)

        assertTrue(EquipmentStatus.RESERVE.isOperational)
        assertFalse(EquipmentStatus.RESERVE.requiresAttention)

        assertFalse(EquipmentStatus.OUT_OF_SERVICE.isOperational)
        assertTrue(EquipmentStatus.OUT_OF_SERVICE.requiresAttention)

        assertFalse(EquipmentStatus.MAINTENANCE_REQUIRED.isOperational)
        assertTrue(EquipmentStatus.MAINTENANCE_REQUIRED.requiresAttention)

        assertFalse(EquipmentStatus.RETIRED.isOperational)
        assertFalse(EquipmentStatus.RETIRED.requiresAttention)
    }

    @Test
    fun equipment_operationalStatusAndSerialization() {
        val scbaPack = Equipment(
            id = "eq_scba_04",
            departmentId = "dept_calhoun",
            stationId = "sta_101",
            apparatusId = "app_e1",
            name = "SCBA Pack #4 (30-min 4500 PSI)",
            category = EquipmentCategory.SCBA,
            serialNumber = "MSA-99281-A",
            barcode = "EQ-004",
            status = EquipmentStatus.IN_SERVICE
        )

        assertTrue(scbaPack.isOperational)

        val oosTool = scbaPack.copy(
            id = "eq_saw_01",
            name = "Cut-off Saw",
            category = EquipmentCategory.HAND_TOOL,
            status = EquipmentStatus.OUT_OF_SERVICE
        )

        assertFalse(oosTool.isOperational)

        val serialized = json.encodeToString(scbaPack)
        val deserialized = json.decodeFromString<Equipment>(serialized)

        assertEquals(scbaPack, deserialized)
    }
}
