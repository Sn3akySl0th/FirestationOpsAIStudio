package com.example.firestationops.ui.personnel

import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersonnelScreenTest {

    private val sampleFirefighters = listOf(
        Firefighter(
            id = "ff_01",
            departmentId = "dept_test",
            badgeNumber = "101",
            firstName = "John",
            lastName = "Gage",
            rank = "Captain",
            certifications = listOf("FF2", "Paramedic", "Hazmat Ops"),
            isOfficer = true,
            status = PersonnelStatus.AVAILABLE
        ),
        Firefighter(
            id = "ff_02",
            departmentId = "dept_test",
            badgeNumber = "102",
            firstName = "Roy",
            lastName = "DeSoto",
            rank = "Lieutenant",
            certifications = listOf("FF2", "Paramedic", "Driver/Operator"),
            isOfficer = true,
            status = PersonnelStatus.RESPONDING
        ),
        Firefighter(
            id = "ff_03",
            departmentId = "dept_test",
            badgeNumber = "103",
            firstName = "Chet",
            lastName = "Kelly",
            rank = "Firefighter",
            certifications = listOf("FF2", "EMT-B"),
            isOfficer = false,
            status = PersonnelStatus.TRAINING
        ),
        Firefighter(
            id = "ff_04",
            departmentId = "dept_test",
            badgeNumber = "104",
            firstName = "Marco",
            lastName = "Lopez",
            rank = "Engineer",
            certifications = listOf("FF2", "Pump Operator"),
            isOfficer = false,
            status = PersonnelStatus.STATION_STANDBY
        ),
        Firefighter(
            id = "ff_05",
            departmentId = "dept_test",
            badgeNumber = "105",
            firstName = "Mike",
            lastName = "Stoker",
            rank = "Engineer",
            certifications = listOf("FF2", "Aerial Specialist"),
            isOfficer = false,
            status = PersonnelStatus.UNAVAILABLE
        )
    )

    @Test
    fun testFirefighterFullNameAndRole() {
        val ff = sampleFirefighters[0]
        assertEquals("John Gage", ff.fullName)
        assertEquals("Captain", ff.rank)
        assertTrue(ff.isOfficer)
        assertEquals(PersonnelStatus.AVAILABLE, ff.status)
        assertTrue(ff.isReadyToRespond)
    }

    @Test
    fun testOperationalReadinessCounters() {
        val readyCount = sampleFirefighters.count { it.isReadyToRespond }
        val engagedCount = sampleFirefighters.count { it.status.isActivelyEngaged }

        // AVAILABLE (ff_01) and STATION_STANDBY (ff_04) are readyToRespond = true
        assertEquals(2, readyCount)

        // RESPONDING (ff_02) and TRAINING (ff_03) are isActivelyEngaged = true
        assertEquals(2, engagedCount)
    }

    @Test
    fun testFilterByStatus() {
        val available = sampleFirefighters.filter { it.status == PersonnelStatus.AVAILABLE }
        assertEquals(1, available.size)
        assertEquals("John Gage", available.first().fullName)

        val responding = sampleFirefighters.filter { it.status == PersonnelStatus.RESPONDING }
        assertEquals(1, responding.size)
        assertEquals("Roy DeSoto", responding.first().fullName)
    }

    @Test
    fun testSearchFiltering() {
        val searchParam = "paramedic"
        val paramedicMatches = sampleFirefighters.filter { ff ->
            ff.fullName.lowercase().contains(searchParam) ||
                (ff.rank?.lowercase()?.contains(searchParam) == true) ||
                (ff.badgeNumber?.lowercase()?.contains(searchParam) == true) ||
                ff.certifications.any { it.lowercase().contains(searchParam) }
        }

        assertEquals(2, paramedicMatches.size)
        assertTrue(paramedicMatches.any { it.fullName == "John Gage" })
        assertTrue(paramedicMatches.any { it.fullName == "Roy DeSoto" })
    }

    @Test
    fun testSearchByBadgeNumber() {
        val badge = "103"
        val match = sampleFirefighters.filter { ff ->
            ff.badgeNumber?.contains(badge) == true
        }

        assertEquals(1, match.size)
        assertEquals("Chet Kelly", match.first().fullName)
    }

    @Test
    fun testFilterByMultipleStatuses() {
        // Toggle multiple statuses simultaneously: AVAILABLE and STATION_STANDBY
        val selectedStatuses = setOf(PersonnelStatus.AVAILABLE, PersonnelStatus.STATION_STANDBY)
        val filtered = sampleFirefighters.filter { selectedStatuses.contains(it.status) }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.fullName == "John Gage" })
        assertTrue(filtered.any { it.fullName == "Marco Lopez" })
    }

    @Test
    fun testFilterPresetReadyToRespond() {
        val readyStatuses = setOf(PersonnelStatus.AVAILABLE, PersonnelStatus.STATION_STANDBY)
        val filtered = sampleFirefighters.filter { readyStatuses.contains(it.status) }

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.isReadyToRespond })
    }

    @Test
    fun testFilterPresetActiveIncidents() {
        val activeStatuses = setOf(PersonnelStatus.RESPONDING, PersonnelStatus.ON_SCENE)
        val filtered = sampleFirefighters.filter { activeStatuses.contains(it.status) }

        assertEquals(1, filtered.size)
        assertEquals("Roy DeSoto", filtered.first().fullName)
    }

    @Test
    fun testPerStatusCountsMapping() {
        val statusCounts = PersonnelStatus.entries.associateWith { status ->
            sampleFirefighters.count { it.status == status }
        }

        assertEquals(1, statusCounts[PersonnelStatus.AVAILABLE])
        assertEquals(1, statusCounts[PersonnelStatus.RESPONDING])
        assertEquals(1, statusCounts[PersonnelStatus.TRAINING])
        assertEquals(1, statusCounts[PersonnelStatus.STATION_STANDBY])
        assertEquals(1, statusCounts[PersonnelStatus.UNAVAILABLE])
        assertEquals(0, statusCounts[PersonnelStatus.ON_SCENE])
        assertEquals(0, statusCounts[PersonnelStatus.LEAVE])
        assertEquals(0, statusCounts[PersonnelStatus.RETIRED])
    }

    @Test
    fun testToggleStatusSelection() {
        var selected = setOf<PersonnelStatus>()
        // Toggle on AVAILABLE
        selected = if (selected.contains(PersonnelStatus.AVAILABLE)) selected - PersonnelStatus.AVAILABLE else selected + PersonnelStatus.AVAILABLE
        assertEquals(setOf(PersonnelStatus.AVAILABLE), selected)

        // Toggle on RESPONDING
        selected = if (selected.contains(PersonnelStatus.RESPONDING)) selected - PersonnelStatus.RESPONDING else selected + PersonnelStatus.RESPONDING
        assertEquals(setOf(PersonnelStatus.AVAILABLE, PersonnelStatus.RESPONDING), selected)

        // Toggle off AVAILABLE
        selected = if (selected.contains(PersonnelStatus.AVAILABLE)) selected - PersonnelStatus.AVAILABLE else selected + PersonnelStatus.AVAILABLE
        assertEquals(setOf(PersonnelStatus.RESPONDING), selected)
    }

    @Test
    fun testRosterExportCsvOutput() {
        val csv = com.example.firestationops.domain.export.PersonnelCsvExporter.export(
            firefighters = sampleFirefighters,
            departmentName = "Engine Company 51"
        )

        assertTrue(csv.contains("Badge #,First Name,Last Name"))
        assertTrue(csv.contains("John,Gage"))
        assertTrue(csv.contains("Roy,DeSoto"))
        assertTrue(csv.contains("Chet,Kelly"))
        assertTrue(csv.contains("Marco,Lopez"))
        assertTrue(csv.contains("Mike,Stoker"))
    }
}

