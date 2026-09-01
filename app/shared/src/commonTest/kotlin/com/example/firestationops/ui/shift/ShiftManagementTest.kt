package com.example.firestationops.ui.shift

import com.example.firestationops.domain.model.DepartmentOperationsStore
import com.example.firestationops.model.AvailabilityPattern
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.FirefighterAvailability
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Shift
import com.example.firestationops.model.ShiftStatus
import com.example.firestationops.model.ShiftType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShiftManagementTest {

    private val sampleFirefighters = listOf(
        Firefighter(
            id = "ff_01",
            departmentId = "dept_1",
            badgeNumber = "101",
            firstName = "John",
            lastName = "Gage",
            rank = "Captain",
            isOfficer = true,
            status = PersonnelStatus.AVAILABLE
        ),
        Firefighter(
            id = "ff_02",
            departmentId = "dept_1",
            badgeNumber = "102",
            firstName = "Roy",
            lastName = "DeSoto",
            rank = "Lieutenant",
            isOfficer = true,
            status = PersonnelStatus.AVAILABLE
        ),
        Firefighter(
            id = "ff_03",
            departmentId = "dept_1",
            badgeNumber = "103",
            firstName = "Chet",
            lastName = "Kelly",
            rank = "Firefighter",
            isOfficer = false,
            status = PersonnelStatus.TRAINING
        )
    )

    @Test
    fun testShiftStaffingCalculations_adequateStaffing() {
        val shift = Shift(
            id = "shift_1",
            departmentId = "dept_1",
            name = "Engine 1 Day Crew",
            shiftType = ShiftType.DAY_DUTY,
            startTimeMillis = 1000L,
            endTimeMillis = 5000L,
            minimumStaffing = 3,
            assignedFirefighterIds = listOf("ff_01", "ff_02", "ff_03")
        )

        assertEquals(3, shift.assignedCount)
        assertTrue(shift.isAdequatelyStaffed)
        assertEquals(0, shift.staffingShortfall)
        assertEquals(1.0f, shift.staffingPercentage)
    }

    @Test
    fun testShiftStaffingCalculations_understaffed() {
        val shift = Shift(
            id = "shift_2",
            departmentId = "dept_1",
            name = "Night Standby Crew",
            shiftType = ShiftType.NIGHT_STANDBY,
            startTimeMillis = 1000L,
            endTimeMillis = 5000L,
            minimumStaffing = 4,
            assignedFirefighterIds = listOf("ff_01", "ff_02")
        )

        assertEquals(2, shift.assignedCount)
        assertFalse(shift.isAdequatelyStaffed)
        assertEquals(2, shift.staffingShortfall)
        assertEquals(0.5f, shift.staffingPercentage)
    }

    @Test
    fun testShiftActiveStatusCheck() {
        val shift = Shift(
            id = "shift_active",
            departmentId = "dept_1",
            name = "Day Duty",
            shiftType = ShiftType.DAY_DUTY,
            startTimeMillis = 10000L,
            endTimeMillis = 20000L,
            status = ShiftStatus.SCHEDULED
        )

        assertTrue(shift.isCurrentlyActive(15000L))
        assertFalse(shift.isCurrentlyActive(5000L))
        assertFalse(shift.isCurrentlyActive(25000L))

        val manualActiveShift = shift.copy(status = ShiftStatus.ACTIVE)
        assertTrue(manualActiveShift.isCurrentlyActive(5000L))
    }

    @Test
    fun testDepartmentOperationsStore_shiftAssignmentAndStatus() = runTest {
        val store = DepartmentOperationsStore("dept_test")

        val initialShifts = store.getShifts().first()
        val targetShift = initialShifts.first()
        val initialCrewCount = targetShift.assignedCount

        // Add a new firefighter assignment
        store.assignFirefighterToShift(targetShift.id, "ff_99")
        val updatedShifts = store.getShifts().first()
        val updatedShift = updatedShifts.first { it.id == targetShift.id }
        assertEquals(initialCrewCount + 1, updatedShift.assignedCount)
        assertTrue(updatedShift.assignedFirefighterIds.contains("ff_99"))

        // Avoid duplicate assignment
        store.assignFirefighterToShift(targetShift.id, "ff_99")
        assertEquals(initialCrewCount + 1, store.getShifts().first().first { it.id == targetShift.id }.assignedCount)

        // Remove firefighter
        store.removeFirefighterFromShift(targetShift.id, "ff_99")
        val finalShifts = store.getShifts().first()
        assertEquals(initialCrewCount, finalShifts.first { it.id == targetShift.id }.assignedCount)
        assertFalse(finalShifts.first { it.id == targetShift.id }.assignedFirefighterIds.contains("ff_99"))

        // Update shift status
        store.updateShiftStatus(targetShift.id, ShiftStatus.COMPLETED)
        assertEquals(ShiftStatus.COMPLETED, store.getShifts().first().first { it.id == targetShift.id }.status)
    }

    @Test
    fun testDepartmentOperationsStore_availabilityPatterns() = runTest {
        val store = DepartmentOperationsStore("dept_test")

        val availabilities = store.getAvailabilities().first()
        assertTrue(availabilities.containsKey("ff_01"))
        assertEquals(AvailabilityPattern.ALWAYS_AVAILABLE, availabilities["ff_01"]?.pattern)

        // Update availability
        val updated = FirefighterAvailability(
            firefighterId = "ff_01",
            pattern = AvailabilityPattern.WEEKENDS_ONLY,
            availableDays = listOf("SAT", "SUN"),
            isAvailableForOvertime = false,
            notes = "Updated training schedule"
        )
        store.updateFirefighterAvailability(updated)

        val updatedMap = store.getAvailabilities().first()
        assertEquals(AvailabilityPattern.WEEKENDS_ONLY, updatedMap["ff_01"]?.pattern)
        assertEquals(listOf("SAT", "SUN"), updatedMap["ff_01"]?.availableDays)
        assertFalse(updatedMap["ff_01"]?.isAvailableForOvertime ?: true)
    }

    @Test
    fun testShiftFilteringLogic() {
        val shifts = listOf(
            Shift(
                id = "s1",
                departmentId = "dept_1",
                name = "Engine 1 Day Crew",
                shiftType = ShiftType.DAY_DUTY,
                startTimeMillis = 1000L,
                endTimeMillis = 2000L,
                minimumStaffing = 4,
                assignedFirefighterIds = listOf("ff_1", "ff_2", "ff_3", "ff_4"),
                status = ShiftStatus.ACTIVE
            ),
            Shift(
                id = "s2",
                departmentId = "dept_1",
                name = "Night Standby",
                shiftType = ShiftType.NIGHT_STANDBY,
                startTimeMillis = 2000L,
                endTimeMillis = 3000L,
                minimumStaffing = 4,
                assignedFirefighterIds = listOf("ff_1", "ff_2"),
                status = ShiftStatus.SCHEDULED
            )
        )

        // Filter active
        val active = shifts.filter { it.status == ShiftStatus.ACTIVE }
        assertEquals(1, active.size)
        assertEquals("s1", active.first().id)

        // Filter needs crew / understaffed
        val understaffed = shifts.filter { !it.isAdequatelyStaffed }
        assertEquals(1, understaffed.size)
        assertEquals("s2", understaffed.first().id)
    }
}
