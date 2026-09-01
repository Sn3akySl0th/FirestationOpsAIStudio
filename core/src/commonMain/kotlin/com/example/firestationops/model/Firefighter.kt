package com.example.firestationops.model

import kotlinx.serialization.Serializable

/**
 * Represents a volunteer firefighter or emergency responder in a department.
 */
@Serializable
data class Firefighter(
    val id: String,
    val departmentId: String,
    val stationId: String? = null,
    val badgeNumber: String? = null,
    val firstName: String,
    val lastName: String,
    val rank: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val certifications: List<String> = emptyList(),
    val isOfficer: Boolean = false,
    val isActive: Boolean = true,
    val status: PersonnelStatus = PersonnelStatus.AVAILABLE,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    val fullName: String get() = if (firstName.isBlank()) lastName else "$firstName $lastName"
    val isReadyToRespond: Boolean get() = isActive && status.isReadyToRespond
}

