package com.example.firestationops.model

import kotlinx.serialization.Serializable

/**
 * Tracks the operational readiness and availability status of department equipment and gear.
 */
@Serializable
enum class EquipmentStatus(
    val label: String,
    val isOperational: Boolean,
    val requiresAttention: Boolean
) {
    /**
     * Equipment is inspected, fully functional, and ready for emergency deployment.
     */
    IN_SERVICE("In Service", isOperational = true, requiresAttention = false),

    /**
     * Equipment is non-functional, damaged, expired, or failed inspection; not available for use.
     */
    OUT_OF_SERVICE("Out of Service", isOperational = false, requiresAttention = true),

    /**
     * Equipment needs routine servicing, calibration, or minor repairs before next deployment.
     */
    MAINTENANCE_REQUIRED("Maintenance Required", isOperational = false, requiresAttention = true),

    /**
     * Operational backup equipment kept in station reserve storage.
     */
    RESERVE("Reserve / Standby", isOperational = true, requiresAttention = false),

    /**
     * Decommissioned, condemned, or surplus equipment no longer in department inventory.
     */
    RETIRED("Retired", isOperational = false, requiresAttention = false)
}
