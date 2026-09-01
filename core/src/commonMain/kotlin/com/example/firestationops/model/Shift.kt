package com.example.firestationops.model

import kotlinx.serialization.Serializable

/**
 * Represents the operational type or platoon of a shift.
 */
@Serializable
enum class ShiftType(val label: String, val code: String) {
    A_SHIFT("A-Shift (Platoon 1)", "A"),
    B_SHIFT("B-Shift (Platoon 2)", "B"),
    C_SHIFT("C-Shift (Platoon 3)", "C"),
    D_SHIFT("D-Shift (Platoon 4)", "D"),
    DAY_DUTY("Day Duty Crew", "DAY"),
    NIGHT_STANDBY("Night Standby Crew", "NIGHT"),
    WEEKEND_CREW("Weekend Coverage", "WKND"),
    VOLUNTEER_ON_CALL("Volunteer On-Call", "VOL"),
    TRAINING_DRILL("Training & Drill", "TRN"),
    CUSTOM("Custom Shift", "CUST")
}

/**
 * Status lifecycle of a shift.
 */
@Serializable
enum class ShiftStatus(val label: String) {
    SCHEDULED("Scheduled"),
    ACTIVE("Active / On Duty"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

/**
 * Availability pattern preference for volunteer and career firefighters.
 */
@Serializable
enum class AvailabilityPattern(val label: String, val description: String) {
    ALWAYS_AVAILABLE("24/7 Availability", "Available for response anytime"),
    WEEKDAY_EVENINGS("Weekday Evenings", "Monday - Friday 18:00 - 06:00"),
    WEEKENDS_ONLY("Weekends Only", "Saturday & Sunday full coverage"),
    DAYTIME_ONLY("Daytime Shifts", "Monday - Friday 07:00 - 18:00"),
    NIGHTS_ONLY("Overnight Standby", "Daily 19:00 - 07:00"),
    SCHEDULED_ROTATION("Scheduled Platoon Rotation", "Follows assigned A/B/C/D shift cycle"),
    ON_CALL_CUSTOM("Custom On-Call", "Availability varies by week/season"),
    UNAVAILABLE("Temporarily Unavailable", "Off-duty, leave, or work commitments")
}

/**
 * Detailed availability profile for a firefighter.
 */
@Serializable
data class FirefighterAvailability(
    val firefighterId: String,
    val pattern: AvailabilityPattern = AvailabilityPattern.ALWAYS_AVAILABLE,
    val availableDays: List<String> = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"),
    val preferredShiftTypes: List<ShiftType> = emptyList(),
    val isAvailableForOvertime: Boolean = true,
    val notes: String? = null
)

/**
 * Represents a scheduled or active operational shift for firefighters and station staffing.
 */
@Serializable
data class Shift(
    val id: String,
    val departmentId: String,
    val stationId: String? = null,
    val name: String,
    val shiftType: ShiftType = ShiftType.DAY_DUTY,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val minimumStaffing: Int = 4,
    val officerInChargeId: String? = null,
    val apparatusIds: List<String> = emptyList(),
    val assignedFirefighterIds: List<String> = emptyList(),
    val status: ShiftStatus = ShiftStatus.SCHEDULED,
    val recurringDays: List<String> = emptyList(),
    val notes: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    val assignedCount: Int get() = assignedFirefighterIds.size
    val isAdequatelyStaffed: Boolean get() = assignedCount >= minimumStaffing
    val staffingShortfall: Int get() = (minimumStaffing - assignedCount).coerceAtLeast(0)
    val staffingPercentage: Float get() = if (minimumStaffing <= 0) 1f else (assignedCount.toFloat() / minimumStaffing).coerceIn(0f, 1f)

    fun isCurrentlyActive(currentTimeMillis: Long): Boolean {
        return status == ShiftStatus.ACTIVE ||
            (status == ShiftStatus.SCHEDULED && currentTimeMillis in startTimeMillis..endTimeMillis)
    }
}
