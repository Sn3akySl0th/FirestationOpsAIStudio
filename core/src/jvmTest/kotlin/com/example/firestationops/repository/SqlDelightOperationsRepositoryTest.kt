package com.example.firestationops.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.firestationops.core.db.CoreDatabase
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.EquipmentStatus
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Station
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlDelightOperationsRepositoryTest {

    private lateinit var repository: SqlDelightOperationsRepository

    @BeforeTest
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CoreDatabase.Schema.create(driver)
        repository = SqlDelightOperationsRepository(driver)
    }

    @Test
    fun station_fullCrudOperations() {
        val station1 = Station(
            id = "sta_01",
            departmentId = "dept_calhoun",
            stationNumber = "1",
            name = "Headquarters",
            address = "123 Main St",
            phoneNumber = "555-0101",
            isActive = true,
            apparatusIds = listOf("app_e1", "app_t1")
        )
        val station2 = Station(
            id = "sta_02",
            departmentId = "dept_calhoun",
            stationNumber = "2",
            name = "North Station",
            address = "456 North Rd",
            phoneNumber = "555-0102",
            isActive = true,
            apparatusIds = listOf("app_e2")
        )

        // Create
        repository.saveStation(station1)
        repository.saveStation(station2)

        // Read all & by department
        val all = repository.getAllStations()
        assertEquals(2, all.size)
        val byDept = repository.getStationsByDepartment("dept_calhoun")
        assertEquals(2, byDept.size)

        // Read by id
        val fetched = repository.getStationById("sta_01")
        assertNotNull(fetched)
        assertEquals("Headquarters", fetched.name)
        assertEquals(listOf("app_e1", "app_t1"), fetched.apparatusIds)

        // Update
        val updatedStation = station1.copy(name = "Station 1 HQ", isActive = false)
        repository.updateStation(updatedStation)
        val afterUpdate = repository.getStationById("sta_01")
        assertNotNull(afterUpdate)
        assertEquals("Station 1 HQ", afterUpdate.name)
        assertFalse(afterUpdate.isActive)

        // Delete
        repository.deleteStation("sta_02")
        assertNull(repository.getStationById("sta_02"))
        assertEquals(1, repository.getAllStations().size)

        // Delete by department
        repository.deleteStationsByDepartment("dept_calhoun")
        assertEquals(0, repository.getStationsByDepartment("dept_calhoun").size)
    }

    @Test
    fun firefighter_fullCrudOperations() {
        val ff1 = Firefighter(
            id = "ff_01",
            departmentId = "dept_calhoun",
            stationId = "sta_01",
            badgeNumber = "101",
            firstName = "John",
            lastName = "Gage",
            rank = "Lieutenant",
            email = "jgage@calhounvfd.org",
            phone = "555-0201",
            certifications = listOf("FF2", "Paramedic"),
            isOfficer = true,
            isActive = true
        )
        val ff2 = Firefighter(
            id = "ff_02",
            departmentId = "dept_calhoun",
            stationId = "sta_02",
            badgeNumber = "102",
            firstName = "Roy",
            lastName = "DeSoto",
            rank = "Captain",
            email = "rdesoto@calhounvfd.org",
            phone = "555-0202",
            certifications = listOf("FF2", "Paramedic", "Instructor"),
            isOfficer = true,
            isActive = true
        )

        // Create
        repository.saveFirefighter(ff1)
        repository.saveFirefighter(ff2)

        // Read
        assertEquals(2, repository.getAllFirefighters().size)
        assertEquals(2, repository.getFirefightersByDepartment("dept_calhoun").size)
        assertEquals(1, repository.getFirefightersByStation("sta_01").size)

        val fetched = repository.getFirefighterById("ff_01")
        assertNotNull(fetched)
        assertEquals("John Gage", fetched.fullName)
        assertTrue(fetched.isOfficer)
        assertEquals(PersonnelStatus.AVAILABLE, fetched.status)
        assertTrue(fetched.isReadyToRespond)
        assertEquals(listOf("FF2", "Paramedic"), fetched.certifications)

        // Update status & rank
        val updatedFf = ff1.copy(rank = "Captain", status = PersonnelStatus.RESPONDING)
        repository.updateFirefighter(updatedFf)
        val afterUpdate = repository.getFirefighterById("ff_01")
        assertNotNull(afterUpdate)
        assertEquals("Captain", afterUpdate.rank)
        assertEquals(PersonnelStatus.RESPONDING, afterUpdate.status)
        assertTrue(afterUpdate.status.isActivelyEngaged)
        assertFalse(afterUpdate.isReadyToRespond)

        // Delete
        repository.deleteFirefighter("ff_02")
        assertNull(repository.getFirefighterById("ff_02"))
        assertEquals(1, repository.getAllFirefighters().size)
    }

    @Test
    fun equipment_fullCrudOperations() {
        val eq1 = Equipment(
            id = "eq_01",
            departmentId = "dept_calhoun",
            stationId = "sta_01",
            apparatusId = "app_e1",
            name = "Thermal Imaging Camera 1",
            category = EquipmentCategory.THERMAL_IMAGING,
            serialNumber = "TIC-9921",
            barcode = "EQ-TIC-1",
            status = EquipmentStatus.IN_SERVICE,
            assignedToFirefighterId = "ff_01"
        )
        val eq2 = Equipment(
            id = "eq_02",
            departmentId = "dept_calhoun",
            stationId = "sta_01",
            apparatusId = "app_e1",
            name = "Hydraulic Spreader",
            category = EquipmentCategory.HYDRAULIC_RESCUE,
            serialNumber = "HYD-1029",
            barcode = "EQ-HYD-1",
            status = EquipmentStatus.MAINTENANCE_REQUIRED
        )

        // Create
        repository.saveEquipment(eq1)
        repository.saveEquipment(eq2)

        // Read
        assertEquals(2, repository.getAllEquipment().size)
        assertEquals(2, repository.getEquipmentByDepartment("dept_calhoun").size)
        assertEquals(2, repository.getEquipmentByStation("sta_01").size)
        assertEquals(2, repository.getEquipmentByApparatus("app_e1").size)
        assertEquals(1, repository.getEquipmentByFirefighter("ff_01").size)

        val fetched = repository.getEquipmentById("eq_01")
        assertNotNull(fetched)
        assertEquals("Thermal Imaging Camera 1", fetched.name)
        assertEquals(EquipmentCategory.THERMAL_IMAGING, fetched.category)
        assertTrue(fetched.isOperational)

        // Update
        val updatedEq = eq1.copy(status = EquipmentStatus.OUT_OF_SERVICE, notes = "Battery replacement needed")
        repository.updateEquipment(updatedEq)
        val afterUpdate = repository.getEquipmentById("eq_01")
        assertNotNull(afterUpdate)
        assertEquals(EquipmentStatus.OUT_OF_SERVICE, afterUpdate.status)
        assertFalse(afterUpdate.isOperational)
        assertEquals("Battery replacement needed", afterUpdate.notes)

        // Delete
        repository.deleteEquipment("eq_02")
        assertNull(repository.getEquipmentById("eq_02"))
        assertEquals(1, repository.getAllEquipment().size)
    }
}
