package com.example.firestationops.model

import kotlinx.serialization.Serializable

/**
 * Tracks the operational readiness and response availability of volunteer firefighters and officers.
 */
@Serializable
enum class PersonnelStatus(
    val label: String,
    val isReadyToRespond: Boolean,
    val isActivelyEngaged: Boolean
) {
    /**
     * Member is active, on-call, and available to respond immediately to alarms.
     */
    AVAILABLE("Available / On Call", isReadyToRespond = true, isActivelyEngaged = false),

    /**
     * Member has acknowledged an alarm and is actively en route to the station or scene.
     */
    RESPONDING("Responding", isReadyToRespond = false, isActivelyEngaged = true),

    /**
     * Member is currently operating on scene at an incident.
     */
    ON_SCENE("On Scene", isReadyToRespond = false, isActivelyEngaged = true),

    /**
     * Member is physically staffing the station or apparatus bay on standby.
     */
    STATION_STANDBY("Station Standby", isReadyToRespond = true, isActivelyEngaged = false),

    /**
     * Member is participating in department training drills or classes.
     */
    TRAINING("In Training", isReadyToRespond = false, isActivelyEngaged = true),

    /**
     * Member is off-duty, out of district, or unavailable for response.
     */
    UNAVAILABLE("Unavailable / Off Duty", isReadyToRespond = false, isActivelyEngaged = false),

    /**
     * Member is on approved medical, administrative, or personal leave.
     */
    LEAVE("On Leave", isReadyToRespond = false, isActivelyEngaged = false),

    /**
     * Member is retired or an honorary non-operational member.
     */
    RETIRED("Retired", isReadyToRespond = false, isActivelyEngaged = false)
}
