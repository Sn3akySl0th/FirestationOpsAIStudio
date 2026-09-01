package com.example.firestationops.domain.model

import com.example.firestationops.domain.export.PersonnelCsvExporter
import com.example.firestationops.domain.search.GlobalSearchEngine
import com.example.firestationops.domain.search.GlobalSearchResults
import com.example.firestationops.domain.search.SearchFilterCategory
import com.example.firestationops.model.AvailabilityPattern
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentStatus
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.FirefighterAvailability
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Shift
import com.example.firestationops.model.ShiftStatus
import com.example.firestationops.model.ShiftType
import com.example.firestationops.model.Station
import com.example.firestationops.repository.OperationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory / local provider for department personnel, equipment, and station operations,
 * supporting SQLDelight persistence via [OperationsRepository].
 */
class DepartmentOperationsStore(
    private val departmentId: String,
    private val operationsRepository: OperationsRepository? = null
) {
    private val _stations = MutableStateFlow<List<Station>>(
        listOf(
            Station(
                id = "station_1",
                departmentId = departmentId,
                stationNumber = "51",
                name = "Station 51 - Central Headquarters",
                address = "12700 S. Normandie Ave, Los Angeles, CA",
                phoneNumber = "555-0151",
                apparatusIds = listOf("app_e1", "app_e2", "app_t1")
            ),
            Station(
                id = "station_2",
                departmentId = departmentId,
                stationNumber = "110",
                name = "Station 110 - Valley Substation",
                address = "4500 Valley Blvd, Los Angeles, CA",
                phoneNumber = "555-0110",
                apparatusIds = listOf("app_b1", "app_r1")
            ),
            Station(
                id = "station_3",
                departmentId = departmentId,
                stationNumber = "8",
                name = "Station 8 - Mountain Rescue Base",
                address = "820 Canyon Crest Rd, Los Angeles, CA",
                phoneNumber = "555-0108",
                apparatusIds = listOf("app_b2")
            )
        )
    )

    private val _firefighters = MutableStateFlow<List<Firefighter>>(
        listOf(
            Firefighter(
                id = "ff_01",
                departmentId = departmentId,
                stationId = "station_1",
                badgeNumber = "101",
                firstName = "John",
                lastName = "Gage",
                rank = "Captain",
                email = "jgage@firestationops.org",
                phone = "555-0201",
                certifications = listOf("FF2", "Paramedic", "Hazmat Ops"),
                isOfficer = true,
                status = PersonnelStatus.AVAILABLE
            ),
            Firefighter(
                id = "ff_02",
                departmentId = departmentId,
                stationId = "station_1",
                badgeNumber = "102",
                firstName = "Roy",
                lastName = "DeSoto",
                rank = "Lieutenant",
                email = "rdesoto@firestationops.org",
                phone = "555-0202",
                certifications = listOf("FF2", "Paramedic", "Driver/Operator"),
                isOfficer = true,
                status = PersonnelStatus.AVAILABLE
            ),
            Firefighter(
                id = "ff_03",
                departmentId = departmentId,
                stationId = "station_2",
                badgeNumber = "103",
                firstName = "Chet",
                lastName = "Kelly",
                rank = "Firefighter",
                email = "ckelly@firestationops.org",
                phone = "555-0203",
                certifications = listOf("FF2", "EMT-B"),
                isOfficer = false,
                status = PersonnelStatus.TRAINING
            ),
            Firefighter(
                id = "ff_04",
                departmentId = departmentId,
                stationId = "station_1",
                badgeNumber = "104",
                firstName = "Marco",
                lastName = "Lopez",
                rank = "Engineer",
                email = "mlopez@firestationops.org",
                phone = "555-0204",
                certifications = listOf("FF2", "Pump Operator", "EMT-B"),
                isOfficer = false,
                status = PersonnelStatus.AVAILABLE
            ),
            Firefighter(
                id = "ff_05",
                departmentId = departmentId,
                stationId = "station_1",
                badgeNumber = "105",
                firstName = "Mike",
                lastName = "Stoker",
                rank = "Engineer",
                email = "mstoker@firestationops.org",
                phone = "555-0205",
                certifications = listOf("FF2", "Aerial Specialist"),
                isOfficer = false,
                status = PersonnelStatus.STATION_STANDBY
            )
        )
    )

    private val _equipment = MutableStateFlow<List<Equipment>>(
        listOf(
            Equipment(
                id = "eq_01",
                departmentId = departmentId,
                apparatusId = "app_e1",
                name = "Scott Air-Pak X3 SCBA #1",
                category = com.example.firestationops.model.EquipmentCategory.SCBA,
                serialNumber = "SCBA-2024-001",
                barcode = "BC-SCBA-001",
                status = EquipmentStatus.IN_SERVICE
            ),
            Equipment(
                id = "eq_02",
                departmentId = departmentId,
                apparatusId = "app_e1",
                name = "Holmatro Combi Tool (Jaws)",
                category = com.example.firestationops.model.EquipmentCategory.HYDRAULIC_RESCUE,
                serialNumber = "H-COMB-882",
                barcode = "BC-JAWS-882",
                status = EquipmentStatus.IN_SERVICE
            ),
            Equipment(
                id = "eq_03",
                departmentId = departmentId,
                apparatusId = "app_t1",
                name = "FLIR K55 Thermal Imaging Camera",
                category = com.example.firestationops.model.EquipmentCategory.THERMAL_IMAGING,
                serialNumber = "TIC-9932-B",
                barcode = "BC-TIC-993",
                status = EquipmentStatus.IN_SERVICE
            ),
            Equipment(
                id = "eq_04",
                departmentId = departmentId,
                apparatusId = "app_e1",
                name = "Zoll AED Plus Unit",
                category = com.example.firestationops.model.EquipmentCategory.MEDICAL,
                serialNumber = "AED-11029",
                barcode = "BC-AED-110",
                status = EquipmentStatus.IN_SERVICE
            ),
            Equipment(
                id = "eq_05",
                departmentId = departmentId,
                apparatusId = "app_e1",
                name = "Super-Vac Gas Smoke Ejector Fan",
                category = com.example.firestationops.model.EquipmentCategory.VENTILATION,
                serialNumber = "SV-FAN-44",
                barcode = "BC-FAN-441",
                status = EquipmentStatus.MAINTENANCE_REQUIRED,
                notes = "Carburetor requires cleaning and spark plug check"
            ),
            Equipment(
                id = "eq_06",
                departmentId = departmentId,
                apparatusId = "app_e2",
                name = "Elkhart Brass 1.5\" Chief Nozzle",
                category = com.example.firestationops.model.EquipmentCategory.NOZZLE,
                serialNumber = "EB-NOZ-05",
                barcode = "BC-NOZ-005",
                status = EquipmentStatus.IN_SERVICE
            )
        )
    )

    private val _shifts = MutableStateFlow<List<Shift>>(
        listOf(
            Shift(
                id = "shift_01",
                departmentId = departmentId,
                stationId = "station_1",
                name = "Day Duty Crew - Engine 1",
                shiftType = ShiftType.DAY_DUTY,
                startTimeMillis = 1756710000000L, // 07:00
                endTimeMillis = 1756753200000L, // 19:00
                minimumStaffing = 4,
                officerInChargeId = "ff_01", // Capt. John Gage
                apparatusIds = listOf("app_e1"),
                assignedFirefighterIds = listOf("ff_01", "ff_02", "ff_04", "ff_05"),
                status = ShiftStatus.ACTIVE,
                recurringDays = listOf("MON", "TUE", "WED", "THU", "FRI"),
                notes = "Primary daytime structural response engine crew"
            ),
            Shift(
                id = "shift_02",
                departmentId = departmentId,
                stationId = "station_1",
                name = "Night Standby Crew",
                shiftType = ShiftType.NIGHT_STANDBY,
                startTimeMillis = 1756753200000L, // 19:00
                endTimeMillis = 1756796400000L, // 07:00
                minimumStaffing = 4,
                officerInChargeId = "ff_02", // Lt. Roy DeSoto
                apparatusIds = listOf("app_e1", "app_t1"),
                assignedFirefighterIds = listOf("ff_02", "ff_04", "ff_05"),
                status = ShiftStatus.SCHEDULED,
                recurringDays = listOf("MON", "TUE", "WED", "THU", "FRI"),
                notes = "⚠️ Need 1 additional firefighter for complete overnight minimum staffing"
            ),
            Shift(
                id = "shift_03",
                departmentId = departmentId,
                stationId = "station_1",
                name = "Weekend Full Coverage Crew",
                shiftType = ShiftType.WEEKEND_CREW,
                startTimeMillis = 1756882800000L,
                endTimeMillis = 1757055600000L,
                minimumStaffing = 5,
                officerInChargeId = "ff_01",
                apparatusIds = listOf("app_e1", "app_e2", "app_t1"),
                assignedFirefighterIds = listOf("ff_01", "ff_03", "ff_04"),
                status = ShiftStatus.SCHEDULED,
                recurringDays = listOf("SAT", "SUN"),
                notes = "Weekend 48-hour volunteer duty rotation"
            ),
            Shift(
                id = "shift_04",
                departmentId = departmentId,
                stationId = "station_1",
                name = "Weekly Training & Drill Session",
                shiftType = ShiftType.TRAINING_DRILL,
                startTimeMillis = 1756854000000L,
                endTimeMillis = 1756864800000L,
                minimumStaffing = 3,
                officerInChargeId = "ff_01",
                apparatusIds = listOf("app_e1"),
                assignedFirefighterIds = listOf("ff_01", "ff_02", "ff_03", "ff_04", "ff_05"),
                status = ShiftStatus.SCHEDULED,
                recurringDays = listOf("WED"),
                notes = "SCBA confidence course and hose deployment drills"
            )
        )
    )

    private val _availabilities = MutableStateFlow<Map<String, FirefighterAvailability>>(
        mapOf(
            "ff_01" to FirefighterAvailability(
                firefighterId = "ff_01",
                pattern = AvailabilityPattern.ALWAYS_AVAILABLE,
                availableDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"),
                preferredShiftTypes = listOf(ShiftType.DAY_DUTY, ShiftType.A_SHIFT),
                isAvailableForOvertime = true,
                notes = "Captain available for major structure fire callbacks"
            ),
            "ff_02" to FirefighterAvailability(
                firefighterId = "ff_02",
                pattern = AvailabilityPattern.WEEKDAY_EVENINGS,
                availableDays = listOf("MON", "TUE", "WED", "THU", "FRI"),
                preferredShiftTypes = listOf(ShiftType.NIGHT_STANDBY, ShiftType.WEEKEND_CREW),
                isAvailableForOvertime = true,
                notes = "Available after 17:30 weekdays and on weekends"
            ),
            "ff_03" to FirefighterAvailability(
                firefighterId = "ff_03",
                pattern = AvailabilityPattern.WEEKENDS_ONLY,
                availableDays = listOf("SAT", "SUN"),
                preferredShiftTypes = listOf(ShiftType.WEEKEND_CREW, ShiftType.TRAINING_DRILL),
                isAvailableForOvertime = false,
                notes = "College schedule on weekdays"
            ),
            "ff_04" to FirefighterAvailability(
                firefighterId = "ff_04",
                pattern = AvailabilityPattern.DAYTIME_ONLY,
                availableDays = listOf("MON", "TUE", "WED", "THU", "FRI"),
                preferredShiftTypes = listOf(ShiftType.DAY_DUTY),
                isAvailableForOvertime = true,
                notes = "Engineer / pump operator day specialist"
            ),
            "ff_05" to FirefighterAvailability(
                firefighterId = "ff_05",
                pattern = AvailabilityPattern.NIGHTS_ONLY,
                availableDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"),
                preferredShiftTypes = listOf(ShiftType.NIGHT_STANDBY, ShiftType.WEEKEND_CREW),
                isAvailableForOvertime = true,
                notes = "Available overnight and for second-alarm calls"
            )
        )
    )

    init {
        operationsRepository?.let { repo ->
            val existingStations = repo.getStationsByDepartment(departmentId)
            if (existingStations.isNotEmpty()) {
                _stations.value = existingStations
            } else {
                _stations.value.forEach { repo.saveStation(it) }
            }

            val existingFirefighters = repo.getFirefightersByDepartment(departmentId)
            if (existingFirefighters.isNotEmpty()) {
                _firefighters.value = existingFirefighters
            } else {
                _firefighters.value.forEach { repo.saveFirefighter(it) }
            }

            val existingEquipment = repo.getEquipmentByDepartment(departmentId)
            if (existingEquipment.isNotEmpty()) {
                _equipment.value = existingEquipment
            } else {
                _equipment.value.forEach { repo.saveEquipment(it) }
            }
        }
    }

    fun getFirefighters(): Flow<List<Firefighter>> = _firefighters.asStateFlow()

    fun updateFirefighterStatus(firefighterId: String, newStatus: PersonnelStatus) {
        _firefighters.value = _firefighters.value.map { ff ->
            if (ff.id == firefighterId) {
                val updated = ff.copy(status = newStatus)
                operationsRepository?.updateFirefighter(updated)
                updated
            } else {
                ff
            }
        }
    }

    fun saveFirefighter(firefighter: Firefighter) {
        val current = _firefighters.value
        val index = current.indexOfFirst { it.id == firefighter.id }
        _firefighters.value = if (index >= 0) {
            current.toMutableList().apply { set(index, firefighter) }
        } else {
            current + firefighter
        }
        operationsRepository?.saveFirefighter(firefighter)
    }

    fun exportRosterCsv(departmentName: String? = null): String {
        val roster = operationsRepository?.getFirefightersByDepartment(departmentId)?.ifEmpty { _firefighters.value } ?: _firefighters.value
        return PersonnelCsvExporter.export(roster, departmentName)
    }

    fun getEquipment(): Flow<List<Equipment>> = _equipment.asStateFlow()

    fun updateEquipmentStatus(equipmentId: String, newStatus: EquipmentStatus, notes: String? = null) {
        _equipment.value = _equipment.value.map { eq ->
            if (eq.id == equipmentId) {
                val updated = eq.copy(
                    status = newStatus,
                    notes = notes ?: eq.notes
                )
                operationsRepository?.updateEquipment(updated)
                updated
            } else {
                eq
            }
        }
    }

    fun assignEquipmentBarcode(equipmentId: String, barcode: String) {
        _equipment.value = _equipment.value.map { eq ->
            if (eq.id == equipmentId) {
                val updated = eq.copy(barcode = barcode)
                operationsRepository?.updateEquipment(updated)
                updated
            } else {
                eq
            }
        }
    }

    fun saveEquipment(equipment: Equipment) {
        val current = _equipment.value
        val index = current.indexOfFirst { it.id == equipment.id }
        _equipment.value = if (index >= 0) {
            current.toMutableList().apply { set(index, equipment) }
        } else {
            current + equipment
        }
        operationsRepository?.saveEquipment(equipment)
    }

    fun getStations(): Flow<List<Station>> = _stations.asStateFlow()

    fun saveStation(station: Station) {
        val current = _stations.value
        val index = current.indexOfFirst { it.id == station.id }
        _stations.value = if (index >= 0) {
            current.toMutableList().apply { set(index, station) }
        } else {
            current + station
        }
        operationsRepository?.saveStation(station)
    }

    fun search(
        query: String,
        filter: SearchFilterCategory = SearchFilterCategory.ALL
    ): GlobalSearchResults {
        return GlobalSearchEngine.search(
            query = query,
            firefighters = _firefighters.value,
            equipment = _equipment.value,
            stations = _stations.value,
            filter = filter
        )
    }

    fun getShifts(): Flow<List<Shift>> = _shifts.asStateFlow()

    fun updateShiftStatus(shiftId: String, newStatus: ShiftStatus) {
        _shifts.value = _shifts.value.map { shift ->
            if (shift.id == shiftId) shift.copy(status = newStatus) else shift
        }
    }

    fun assignFirefighterToShift(shiftId: String, firefighterId: String) {
        _shifts.value = _shifts.value.map { shift ->
            if (shift.id == shiftId && !shift.assignedFirefighterIds.contains(firefighterId)) {
                shift.copy(assignedFirefighterIds = shift.assignedFirefighterIds + firefighterId)
            } else {
                shift
            }
        }
    }

    fun removeFirefighterFromShift(shiftId: String, firefighterId: String) {
        _shifts.value = _shifts.value.map { shift ->
            if (shift.id == shiftId) {
                shift.copy(assignedFirefighterIds = shift.assignedFirefighterIds - firefighterId)
            } else {
                shift
            }
        }
    }

    fun saveShift(shift: Shift) {
        val current = _shifts.value
        val index = current.indexOfFirst { it.id == shift.id }
        _shifts.value = if (index >= 0) {
            current.toMutableList().apply { set(index, shift) }
        } else {
            current + shift
        }
    }

    fun getAvailabilities(): Flow<Map<String, FirefighterAvailability>> = _availabilities.asStateFlow()

    fun updateFirefighterAvailability(availability: FirefighterAvailability) {
        _availabilities.value = _availabilities.value + (availability.firefighterId to availability)
    }
}
