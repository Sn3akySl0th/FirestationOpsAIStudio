package com.example.firestationops.model

import kotlinx.serialization.Serializable

/**
 * Represents a physical or organizational station within a volunteer fire department.
 */
@Serializable
data class Station(
    val id: String,
    val departmentId: String,
    val stationNumber: String? = null,
    val name: String,
    val address: String? = null,
    val phoneNumber: String? = null,
    val isActive: Boolean = true,
    val apparatusIds: List<String> = emptyList(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
