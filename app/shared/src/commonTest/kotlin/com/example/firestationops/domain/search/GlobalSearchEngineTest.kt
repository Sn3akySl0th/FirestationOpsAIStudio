package com.example.firestationops.domain.search

import com.example.firestationops.domain.model.DepartmentOperationsStore
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.EquipmentStatus
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Station
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalSearchEngineTest {

    private val sampleFirefighters = listOf(
        Firefighter(
            id = "ff_01",
            departmentId = "dept_lakeview",
            stationId = "station_1",
            badgeNumber = "101",
            firstName = "John",
            lastName = "Gage",
            rank = "Captain",
            email = "jgage@lakeviewvfd.org",
            phone = "555-0201",
            certifications = listOf("FF2", "Paramedic", "Hazmat Ops"),
            isOfficer = true,
            status = PersonnelStatus.AVAILABLE
        ),
        Firefighter(
            id = "ff_02",
            departmentId = "dept_lakeview",
            stationId = "station_1",
            badgeNumber = "102",
            firstName = "Roy",
            lastName = "DeSoto",
            rank = "Lieutenant",
            email = "rdesoto@lakeviewvfd.org",
            phone = "555-0202",
            certifications = listOf("FF2", "Paramedic", "Driver/Operator"),
            isOfficer = true,
            status = PersonnelStatus.AVAILABLE
        ),
        Firefighter(
            id = "ff_03",
            departmentId = "dept_lakeview",
            stationId = "station_2",
            badgeNumber = "103",
            firstName = "Chet",
            lastName = "Kelly",
            rank = "Firefighter",
            email = "ckelly@lakeviewvfd.org",
            phone = "555-0203",
            certifications = listOf("FF2", "EMT-B"),
            isOfficer = false,
            status = PersonnelStatus.TRAINING
        )
    )

    private val sampleEquipment = listOf(
        Equipment(
            id = "eq_01",
            departmentId = "dept_lakeview",
            stationId = "station_1",
            apparatusId = "app_e1",
            name = "Scott Air-Pak X3 SCBA #1",
            category = EquipmentCategory.SCBA,
            serialNumber = "SCBA-2024-001",
            status = EquipmentStatus.IN_SERVICE
        ),
        Equipment(
            id = "eq_02",
            departmentId = "dept_lakeview",
            stationId = "station_1",
            apparatusId = "app_e1",
            name = "Holmatro Hydraulic Combi Tool",
            category = EquipmentCategory.HYDRAULIC_RESCUE,
            serialNumber = "H-COMB-882",
            status = EquipmentStatus.IN_SERVICE,
            notes = "Cutter blades inspected quarterly"
        ),
        Equipment(
            id = "eq_03",
            departmentId = "dept_lakeview",
            stationId = "station_2",
            name = "Bullard Thermal Imaging Camera",
            category = EquipmentCategory.THERMAL_IMAGING,
            serialNumber = "TIC-9041",
            status = EquipmentStatus.MAINTENANCE_REQUIRED
        )
    )

    private val sampleStations = listOf(
        Station(
            id = "station_1",
            departmentId = "dept_lakeview",
            stationNumber = "51",
            name = "Station 51 - Central Headquarters",
            address = "12700 S. Normandie Ave, Los Angeles, CA",
            phoneNumber = "555-0151",
            apparatusIds = listOf("app_e1", "app_e2")
        ),
        Station(
            id = "station_2",
            departmentId = "dept_lakeview",
            stationNumber = "110",
            name = "Station 110 - Valley Substation",
            address = "4500 Valley Blvd, Los Angeles, CA",
            phoneNumber = "555-0110",
            apparatusIds = listOf("app_b1")
        )
    )

    @Test
    fun blankQuery_returnsEmptyResults() {
        val result = GlobalSearchEngine.search(
            query = "   ",
            firefighters = sampleFirefighters,
            equipment = sampleEquipment,
            stations = sampleStations
        )

        assertTrue(result.isEmpty)
        assertEquals(0, result.totalCount)
    }

    @Test
    fun searchByName_matchesFirefighter() {
        val result = GlobalSearchEngine.search(
            query = "Gage",
            firefighters = sampleFirefighters,
            equipment = sampleEquipment,
            stations = sampleStations
        )

        assertEquals(1, result.totalFirefightersCount)
        assertEquals(0, result.totalEquipmentCount)
        assertEquals(0, result.totalStationsCount)
        val match = result.results.first() as GlobalSearchResult.FirefighterMatch
        assertEquals("John Gage", match.title)
    }

    @Test
    fun searchByCertification_matchesMultipleFirefighters() {
        val result = GlobalSearchEngine.search(
            query = "Paramedic",
            firefighters = sampleFirefighters,
            equipment = sampleEquipment,
            stations = sampleStations
        )

        assertEquals(2, result.totalFirefightersCount)
        val matchedIds = result.results.map { it.id }.toSet()
        assertTrue(matchedIds.contains("ff_01"))
        assertTrue(matchedIds.contains("ff_02"))
    }

    @Test
    fun searchBySerialNumber_matchesEquipment() {
        val result = GlobalSearchEngine.search(
            query = "H-COMB-882",
            firefighters = sampleFirefighters,
            equipment = sampleEquipment,
            stations = sampleStations
        )

        assertEquals(1, result.totalEquipmentCount)
        val match = result.results.first() as GlobalSearchResult.EquipmentMatch
        assertEquals("Holmatro Hydraulic Combi Tool", match.title)
    }

    @Test
    fun searchByAddress_matchesStation() {
        val result = GlobalSearchEngine.search(
            query = "Normandie",
            firefighters = sampleFirefighters,
            equipment = sampleEquipment,
            stations = sampleStations
        )

        assertEquals(1, result.totalStationsCount)
        val match = result.results.first() as GlobalSearchResult.StationMatch
        assertEquals("Station 51 - Central Headquarters", match.title)
    }

    @Test
    fun searchCrossCategoryKeyword_matchesAllTypes() {
        val result = GlobalSearchEngine.search(
            query = "51",
            firefighters = sampleFirefighters,
            equipment = sampleEquipment,
            stations = sampleStations
        )

        // Matches Station 51, and Station 51 phone number 555-0151
        assertTrue(result.totalStationsCount >= 1)
        val stationMatch = result.results.any { it is GlobalSearchResult.StationMatch && it.station.stationNumber == "51" }
        assertTrue(stationMatch)
    }

    @Test
    fun categoryFilter_narrowsResults() {
        val allResults = GlobalSearchEngine.search(
            query = "Station",
            firefighters = sampleFirefighters,
            equipment = sampleEquipment,
            stations = sampleStations,
            filter = SearchFilterCategory.ALL
        )
        assertTrue(allResults.results.isNotEmpty())

        val stationOnlyResults = GlobalSearchEngine.search(
            query = "Station",
            firefighters = sampleFirefighters,
            equipment = sampleEquipment,
            stations = sampleStations,
            filter = SearchFilterCategory.STATIONS
        )

        assertTrue(stationOnlyResults.results.all { it is GlobalSearchResult.StationMatch })
        assertEquals(2, stationOnlyResults.results.size)
    }

    @Test
    fun departmentOperationsStore_searchIntegration() {
        val store = DepartmentOperationsStore("dept_lakeview")
        val results = store.search("Captain")

        assertTrue(results.results.isNotEmpty())
        assertTrue(results.results.any { it is GlobalSearchResult.FirefighterMatch && it.firefighter.rank == "Captain" })
    }
}
