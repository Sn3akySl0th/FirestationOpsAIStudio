package com.example.firestationops.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.example.firestationops.core.db.CoreDatabase
import com.example.firestationops.core.db.EquipmentEntity
import com.example.firestationops.core.db.FirefighterEntity
import com.example.firestationops.core.db.StationEntity
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.EquipmentStatus
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Station
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository interface defining CRUD and query operations for Stations, Firefighters,
 * and Equipment within volunteer fire department operations.
 */
interface OperationsRepository {

    // --- Station CRUD ---
    fun getAllStations(): List<Station>
    fun getStationsByDepartment(departmentId: String): List<Station>
    fun getStationById(id: String): Station?
    fun saveStation(station: Station)
    fun updateStation(station: Station) = saveStation(station)
    fun deleteStation(id: String)
    fun deleteStationsByDepartment(departmentId: String)
    fun getStationsByDepartmentFlow(departmentId: String, dispatcher: CoroutineDispatcher = Dispatchers.Default): Flow<List<Station>>

    // --- Firefighter CRUD ---
    fun getAllFirefighters(): List<Firefighter>
    fun getFirefightersByDepartment(departmentId: String): List<Firefighter>
    fun getFirefightersByStation(stationId: String): List<Firefighter>
    fun getFirefighterById(id: String): Firefighter?
    fun saveFirefighter(firefighter: Firefighter)
    fun updateFirefighter(firefighter: Firefighter) = saveFirefighter(firefighter)
    fun deleteFirefighter(id: String)
    fun deleteFirefightersByDepartment(departmentId: String)
    fun getFirefightersByDepartmentFlow(departmentId: String, dispatcher: CoroutineDispatcher = Dispatchers.Default): Flow<List<Firefighter>>

    // --- Equipment CRUD ---
    fun getAllEquipment(): List<Equipment>
    fun getEquipmentByDepartment(departmentId: String): List<Equipment>
    fun getEquipmentByStation(stationId: String): List<Equipment>
    fun getEquipmentByApparatus(apparatusId: String): List<Equipment>
    fun getEquipmentByFirefighter(firefighterId: String): List<Equipment>
    fun getEquipmentById(id: String): Equipment?
    fun saveEquipment(equipment: Equipment)
    fun updateEquipment(equipment: Equipment) = saveEquipment(equipment)
    fun deleteEquipment(id: String)
    fun deleteEquipmentByDepartment(departmentId: String)
    fun getEquipmentByDepartmentFlow(departmentId: String, dispatcher: CoroutineDispatcher = Dispatchers.Default): Flow<List<Equipment>>
}

/**
 * Production-ready SQLDelight implementation of [OperationsRepository].
 */
class SqlDelightOperationsRepository(
    private val database: CoreDatabase,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : OperationsRepository {

    constructor(driver: SqlDriver, json: Json = Json { ignoreUnknownKeys = true }) : this(
        CoreDatabase(driver),
        json
    )

    private val queries = database.coreOperationsQueries

    // -------------------------------------------------------------------------
    // Station Operations
    // -------------------------------------------------------------------------

    override fun getAllStations(): List<Station> =
        queries.selectAllStations().executeAsList().map(::mapStation)

    override fun getStationsByDepartment(departmentId: String): List<Station> =
        queries.selectStationsByDepartment(departmentId).executeAsList().map(::mapStation)

    override fun getStationById(id: String): Station? =
        queries.selectStationById(id).executeAsOneOrNull()?.let(::mapStation)

    override fun saveStation(station: Station) {
        queries.insertStation(
            id = station.id,
            departmentId = station.departmentId,
            stationNumber = station.stationNumber,
            name = station.name,
            address = station.address,
            phoneNumber = station.phoneNumber,
            isActive = if (station.isActive) 1L else 0L,
            apparatusIdsJson = json.encodeToString(station.apparatusIds),
            createdAt = station.createdAt,
            updatedAt = station.updatedAt
        )
    }

    override fun deleteStation(id: String) {
        queries.deleteStationById(id)
    }

    override fun deleteStationsByDepartment(departmentId: String) {
        queries.deleteStationsByDepartment(departmentId)
    }

    override fun getStationsByDepartmentFlow(
        departmentId: String,
        dispatcher: CoroutineDispatcher
    ): Flow<List<Station>> =
        queries.selectStationsByDepartment(departmentId)
            .asFlow()
            .mapToList(dispatcher)
            .map { list -> list.map(::mapStation) }

    // -------------------------------------------------------------------------
    // Firefighter Operations
    // -------------------------------------------------------------------------

    override fun getAllFirefighters(): List<Firefighter> =
        queries.selectAllFirefighters().executeAsList().map(::mapFirefighter)

    override fun getFirefightersByDepartment(departmentId: String): List<Firefighter> =
        queries.selectFirefightersByDepartment(departmentId).executeAsList().map(::mapFirefighter)

    override fun getFirefightersByStation(stationId: String): List<Firefighter> =
        queries.selectFirefightersByStation(stationId).executeAsList().map(::mapFirefighter)

    override fun getFirefighterById(id: String): Firefighter? =
        queries.selectFirefighterById(id).executeAsOneOrNull()?.let(::mapFirefighter)

    override fun saveFirefighter(firefighter: Firefighter) {
        queries.insertFirefighter(
            id = firefighter.id,
            departmentId = firefighter.departmentId,
            stationId = firefighter.stationId,
            badgeNumber = firefighter.badgeNumber,
            firstName = firefighter.firstName,
            lastName = firefighter.lastName,
            rank = firefighter.rank,
            email = firefighter.email,
            phone = firefighter.phone,
            certificationsJson = json.encodeToString(firefighter.certifications),
            isOfficer = if (firefighter.isOfficer) 1L else 0L,
            isActive = if (firefighter.isActive) 1L else 0L,
            status = firefighter.status.name,
            createdAt = firefighter.createdAt,
            updatedAt = firefighter.updatedAt
        )
    }

    override fun deleteFirefighter(id: String) {
        queries.deleteFirefighterById(id)
    }

    override fun deleteFirefightersByDepartment(departmentId: String) {
        queries.deleteFirefightersByDepartment(departmentId)
    }

    override fun getFirefightersByDepartmentFlow(
        departmentId: String,
        dispatcher: CoroutineDispatcher
    ): Flow<List<Firefighter>> =
        queries.selectFirefightersByDepartment(departmentId)
            .asFlow()
            .mapToList(dispatcher)
            .map { list -> list.map(::mapFirefighter) }

    // -------------------------------------------------------------------------
    // Equipment Operations
    // -------------------------------------------------------------------------

    override fun getAllEquipment(): List<Equipment> =
        queries.selectAllEquipment().executeAsList().map(::mapEquipment)

    override fun getEquipmentByDepartment(departmentId: String): List<Equipment> =
        queries.selectEquipmentByDepartment(departmentId).executeAsList().map(::mapEquipment)

    override fun getEquipmentByStation(stationId: String): List<Equipment> =
        queries.selectEquipmentByStation(stationId).executeAsList().map(::mapEquipment)

    override fun getEquipmentByApparatus(apparatusId: String): List<Equipment> =
        queries.selectEquipmentByApparatus(apparatusId).executeAsList().map(::mapEquipment)

    override fun getEquipmentByFirefighter(firefighterId: String): List<Equipment> =
        queries.selectEquipmentByFirefighter(firefighterId).executeAsList().map(::mapEquipment)

    override fun getEquipmentById(id: String): Equipment? =
        queries.selectEquipmentById(id).executeAsOneOrNull()?.let(::mapEquipment)

    override fun saveEquipment(equipment: Equipment) {
        queries.insertEquipment(
            id = equipment.id,
            departmentId = equipment.departmentId,
            stationId = equipment.stationId,
            apparatusId = equipment.apparatusId,
            name = equipment.name,
            category = equipment.category.name,
            serialNumber = equipment.serialNumber,
            barcode = equipment.barcode,
            status = equipment.status.name,
            assignedToFirefighterId = equipment.assignedToFirefighterId,
            lastInspectionDate = equipment.lastInspectionDate,
            expirationDate = equipment.expirationDate,
            notes = equipment.notes,
            createdAt = equipment.createdAt,
            updatedAt = equipment.updatedAt
        )
    }

    override fun deleteEquipment(id: String) {
        queries.deleteEquipmentById(id)
    }

    override fun deleteEquipmentByDepartment(departmentId: String) {
        queries.deleteEquipmentByDepartment(departmentId)
    }

    override fun getEquipmentByDepartmentFlow(
        departmentId: String,
        dispatcher: CoroutineDispatcher
    ): Flow<List<Equipment>> =
        queries.selectEquipmentByDepartment(departmentId)
            .asFlow()
            .mapToList(dispatcher)
            .map { list -> list.map(::mapEquipment) }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private fun mapStation(entity: StationEntity): Station =
        Station(
            id = entity.id,
            departmentId = entity.departmentId,
            stationNumber = entity.stationNumber,
            name = entity.name,
            address = entity.address,
            phoneNumber = entity.phoneNumber,
            isActive = entity.isActive == 1L,
            apparatusIds = try {
                json.decodeFromString(entity.apparatusIdsJson)
            } catch (e: Exception) {
                emptyList()
            },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

    private fun mapFirefighter(entity: FirefighterEntity): Firefighter =
        Firefighter(
            id = entity.id,
            departmentId = entity.departmentId,
            stationId = entity.stationId,
            badgeNumber = entity.badgeNumber,
            firstName = entity.firstName,
            lastName = entity.lastName,
            rank = entity.rank,
            email = entity.email,
            phone = entity.phone,
            certifications = try {
                json.decodeFromString(entity.certificationsJson)
            } catch (e: Exception) {
                emptyList()
            },
            isOfficer = entity.isOfficer == 1L,
            isActive = entity.isActive == 1L,
            status = try {
                PersonnelStatus.valueOf(entity.status)
            } catch (e: Exception) {
                PersonnelStatus.AVAILABLE
            },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

    private fun mapEquipment(entity: EquipmentEntity): Equipment =
        Equipment(
            id = entity.id,
            departmentId = entity.departmentId,
            stationId = entity.stationId,
            apparatusId = entity.apparatusId,
            name = entity.name,
            category = try {
                EquipmentCategory.valueOf(entity.category)
            } catch (e: Exception) {
                EquipmentCategory.OTHER
            },
            serialNumber = entity.serialNumber,
            barcode = entity.barcode,
            status = try {
                EquipmentStatus.valueOf(entity.status)
            } catch (e: Exception) {
                EquipmentStatus.IN_SERVICE
            },
            assignedToFirefighterId = entity.assignedToFirefighterId,
            lastInspectionDate = entity.lastInspectionDate,
            expirationDate = entity.expirationDate,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
}
