package com.example.firestationops.domain.search

import com.example.firestationops.model.Equipment
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.Station

/**
 * Filter categories for narrowing global search queries.
 */
enum class SearchFilterCategory(val label: String) {
    ALL("All"),
    FIREFIGHTERS("Firefighters"),
    EQUIPMENT("Equipment"),
    STATIONS("Stations")
}

/**
 * Polymorphic search result item across firefighters, equipment, and station records.
 */
sealed interface GlobalSearchResult {
    val id: String
    val title: String
    val subtitle: String
    val category: SearchFilterCategory
    val matchedField: String
    val snippet: String?
    val relevanceScore: Int

    data class FirefighterMatch(
        val firefighter: Firefighter,
        override val matchedField: String,
        override val snippet: String? = null,
        override val relevanceScore: Int = 0
    ) : GlobalSearchResult {
        override val id: String get() = firefighter.id
        override val title: String get() = firefighter.fullName
        override val subtitle: String
            get() = buildString {
                append(firefighter.rank ?: "Firefighter")
                firefighter.badgeNumber?.let { append(" • Badge #$it") }
                append(" • ")
                append(firefighter.status.label)
            }
        override val category: SearchFilterCategory get() = SearchFilterCategory.FIREFIGHTERS
    }

    data class EquipmentMatch(
        val equipment: Equipment,
        override val matchedField: String,
        override val snippet: String? = null,
        override val relevanceScore: Int = 0
    ) : GlobalSearchResult {
        override val id: String get() = equipment.id
        override val title: String get() = equipment.name
        override val subtitle: String
            get() = buildString {
                append(equipment.category.name.replace('_', ' '))
                equipment.serialNumber?.let { append(" • SN: $it") }
                append(" • ")
                append(equipment.status.label)
            }
        override val category: SearchFilterCategory get() = SearchFilterCategory.EQUIPMENT
    }

    data class StationMatch(
        val station: Station,
        override val matchedField: String,
        override val snippet: String? = null,
        override val relevanceScore: Int = 0
    ) : GlobalSearchResult {
        override val id: String get() = station.id
        override val title: String get() = station.name
        override val subtitle: String
            get() = listOfNotNull(
                station.stationNumber?.let { "Station #$it" },
                station.address,
                station.phoneNumber
            ).joinToString(" • ")
        override val category: SearchFilterCategory get() = SearchFilterCategory.STATIONS
    }
}

/**
 * Result aggregate of a global search execution.
 */
data class GlobalSearchResults(
    val query: String = "",
    val activeFilter: SearchFilterCategory = SearchFilterCategory.ALL,
    val results: List<GlobalSearchResult> = emptyList(),
    val totalFirefightersCount: Int = 0,
    val totalEquipmentCount: Int = 0,
    val totalStationsCount: Int = 0
) {
    val totalCount: Int get() = totalFirefightersCount + totalEquipmentCount + totalStationsCount
    val isEmpty: Boolean get() = results.isEmpty()
}
