package com.example.firestationops.domain.equipment

import com.example.firestationops.model.Equipment

/**
 * Domain utility for matching and resolving scanned QR codes and barcodes to Equipment records.
 * Supports various tag formats used in fire service inventory management.
 */
object EquipmentTagMatcher {

    private const val PREFIX_FIRESTATIONOPS_URI = "firestationops://equipment/"
    private const val PREFIX_FIREOPS_COLON = "fireops:equipment:"
    private const val PREFIX_EQ_COLON = "eq:"
    private const val PREFIX_EQUIPMENT_COLON = "equipment:"
    private const val PREFIX_HTTPS_URL = "https://firestationops.app/equipment/"

    /**
     * Attempts to match a raw scanned QR or barcode string against a list of Equipment.
     * Matches against ID, Barcode, Serial Number, or standardized URI payloads.
     */
    fun matchEquipmentByTag(scannedCode: String, equipmentList: List<Equipment>): Equipment? {
        val trimmed = scannedCode.trim()
        if (trimmed.isBlank()) return null

        val extractedId = extractEquipmentId(trimmed)

        // 1. Direct or extracted ID match
        if (extractedId != null) {
            equipmentList.firstOrNull { it.id.equals(extractedId, ignoreCase = true) }?.let {
                return it
            }
        }

        // 2. Exact or Case-insensitive ID match
        equipmentList.firstOrNull { it.id.equals(trimmed, ignoreCase = true) }?.let {
            return it
        }

        // 3. Exact or Case-insensitive Barcode match
        equipmentList.firstOrNull { it.barcode?.trim()?.equals(trimmed, ignoreCase = true) == true }?.let {
            return it
        }

        // 4. Exact or Case-insensitive Serial Number match
        equipmentList.firstOrNull { it.serialNumber?.trim()?.equals(trimmed, ignoreCase = true) == true }?.let {
            return it
        }

        // 5. Partial fallback for prefix-stripped codes
        val sanitized = trimmed.removePrefix("#").trim()
        equipmentList.firstOrNull {
            it.id.equals(sanitized, ignoreCase = true) ||
                it.barcode?.trim()?.equals(sanitized, ignoreCase = true) == true ||
                it.serialNumber?.trim()?.equals(sanitized, ignoreCase = true) == true
        }?.let {
            return it
        }

        return null
    }

    /**
     * Extracts an Equipment ID from structured QR code payloads.
     */
    fun extractEquipmentId(rawTag: String): String? {
        val clean = rawTag.trim()
        return when {
            clean.startsWith(PREFIX_FIRESTATIONOPS_URI, ignoreCase = true) ->
                clean.substring(PREFIX_FIRESTATIONOPS_URI.length).trim().takeIf { it.isNotEmpty() }
            clean.startsWith(PREFIX_FIREOPS_COLON, ignoreCase = true) ->
                clean.substring(PREFIX_FIREOPS_COLON.length).trim().takeIf { it.isNotEmpty() }
            clean.startsWith(PREFIX_EQ_COLON, ignoreCase = true) ->
                clean.substring(PREFIX_EQ_COLON.length).trim().takeIf { it.isNotEmpty() }
            clean.startsWith(PREFIX_EQUIPMENT_COLON, ignoreCase = true) ->
                clean.substring(PREFIX_EQUIPMENT_COLON.length).trim().takeIf { it.isNotEmpty() }
            clean.startsWith(PREFIX_HTTPS_URL, ignoreCase = true) ->
                clean.substring(PREFIX_HTTPS_URL.length).trim().takeIf { it.isNotEmpty() }
            else -> null
        }
    }

    /**
     * Formats a standardized QR code data string for an equipment item.
     */
    fun generateEquipmentQrData(equipment: Equipment): String {
        return "$PREFIX_FIREOPS_COLON${equipment.id}"
    }
}
