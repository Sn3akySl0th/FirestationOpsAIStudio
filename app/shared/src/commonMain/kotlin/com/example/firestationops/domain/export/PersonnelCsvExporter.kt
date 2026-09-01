package com.example.firestationops.domain.export

import com.example.firestationops.model.Firefighter

/**
 * Exporter responsible for generating CSV formatted reports of firefighter rosters
 * and personnel statuses for department administrative, operational, and accountability workflows.
 */
object PersonnelCsvExporter {

    fun export(
        firefighters: List<Firefighter>,
        departmentName: String? = null,
        includeMetadataHeader: Boolean = false
    ): String {
        val lines = buildList {
            if (includeMetadataHeader || !departmentName.isNullOrBlank()) {
                add(row("Report Type", "Firefighter Personnel Roster"))
                if (!departmentName.isNullOrBlank()) {
                    add(row("Department", departmentName))
                }
                add(row("Total Firefighters", firefighters.size.toString()))
                add("")
            }

            add(
                row(
                    "Badge #",
                    "First Name",
                    "Last Name",
                    "Full Name",
                    "Rank",
                    "Status",
                    "Readiness",
                    "Officer",
                    "Active",
                    "Certifications",
                    "Station ID",
                    "Email",
                    "Phone"
                )
            )

            firefighters.forEach { ff ->
                val readinessLabel = when {
                    ff.isReadyToRespond -> "Ready for Duty"
                    ff.status.isActivelyEngaged -> "Active / Engaged"
                    else -> "Off Duty / Unavailable"
                }

                add(
                    row(
                        ff.badgeNumber.orEmpty(),
                        ff.firstName,
                        ff.lastName,
                        ff.fullName,
                        ff.rank.orEmpty(),
                        ff.status.label,
                        readinessLabel,
                        if (ff.isOfficer) "Yes" else "No",
                        if (ff.isActive) "Yes" else "No",
                        ff.certifications.joinToString("; "),
                        ff.stationId.orEmpty(),
                        ff.email.orEmpty(),
                        ff.phone.orEmpty()
                    )
                )
            }
        }
        return lines.joinToString("\n")
    }

    private fun row(vararg values: String): String =
        values.joinToString(",") { escapeCsv(it) }

    internal fun escapeCsv(value: String): String {
        if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            return value
        }
        return "\"${value.replace("\"", "\"\"")}\""
    }
}
