package com.example.firestationops.model

import kotlinx.serialization.Serializable

/**
 * Category for equipment categorization.
 */
@Serializable
enum class EquipmentCategory {
    SCBA,
    HOSE,
    HAND_TOOL,
    HYDRAULIC_RESCUE,
    MEDICAL,
    RADIO,
    PPE,
    GENERATOR,
    THERMAL_IMAGING,
    NOZZLE,
    LADDER,
    VENTILATION,
    OTHER
}

/**
 * Represents equipment, tools, PPE, or apparatus-mounted gear in a volunteer fire department.
 */
@Serializable
data class Equipment(
    val id: String,
    val departmentId: String,
    val stationId: String? = null,
    val apparatusId: String? = null,
    val name: String,
    val category: EquipmentCategory = EquipmentCategory.OTHER,
    val serialNumber: String? = null,
    val barcode: String? = null,
    val status: EquipmentStatus = EquipmentStatus.IN_SERVICE,
    val assignedToFirefighterId: String? = null,
    val lastInspectionDate: Long? = null,
    val expirationDate: Long? = null,
    val notes: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    val isOperational: Boolean get() = status.isOperational
}

