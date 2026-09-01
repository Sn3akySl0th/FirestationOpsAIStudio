package com.example.firestationops.domain.search

import com.example.firestationops.model.Equipment
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.Station

/**
 * Domain engine responsible for performing fast, token-based global search
 * across Firefighters, Equipment, and Station records.
 */
object GlobalSearchEngine {

    fun search(
        query: String,
        firefighters: List<Firefighter>,
        equipment: List<Equipment>,
        stations: List<Station>,
        filter: SearchFilterCategory = SearchFilterCategory.ALL
    ): GlobalSearchResults {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return GlobalSearchResults(
                query = "",
                activeFilter = filter,
                results = emptyList(),
                totalFirefightersCount = 0,
                totalEquipmentCount = 0,
                totalStationsCount = 0
            )
        }

        val tokens = trimmed.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }

        val ffMatches = firefighters.mapNotNull { ff -> matchFirefighter(ff, tokens, trimmed) }
        val eqMatches = equipment.mapNotNull { eq -> matchEquipment(eq, tokens, trimmed) }
        val stMatches = stations.mapNotNull { st -> matchStation(st, tokens, trimmed) }

        val allResults = buildList {
            if (filter == SearchFilterCategory.ALL || filter == SearchFilterCategory.FIREFIGHTERS) {
                addAll(ffMatches)
            }
            if (filter == SearchFilterCategory.ALL || filter == SearchFilterCategory.EQUIPMENT) {
                addAll(eqMatches)
            }
            if (filter == SearchFilterCategory.ALL || filter == SearchFilterCategory.STATIONS) {
                addAll(stMatches)
            }
        }.sortedWith(
            compareByDescending<GlobalSearchResult> { it.relevanceScore }
                .thenBy { it.title.lowercase() }
        )

        return GlobalSearchResults(
            query = trimmed,
            activeFilter = filter,
            results = allResults,
            totalFirefightersCount = ffMatches.size,
            totalEquipmentCount = eqMatches.size,
            totalStationsCount = stMatches.size
        )
    }

    private fun matchFirefighter(
        ff: Firefighter,
        tokens: List<String>,
        fullQuery: String
    ): GlobalSearchResult.FirefighterMatch? {
        val lowerFullName = ff.fullName.lowercase()
        val lowerFirst = ff.firstName.lowercase()
        val lowerLast = ff.lastName.lowercase()
        val lowerBadge = ff.badgeNumber?.lowercase().orEmpty()
        val lowerRank = ff.rank?.lowercase().orEmpty()
        val lowerEmail = ff.email?.lowercase().orEmpty()
        val lowerPhone = ff.phone?.lowercase().orEmpty()
        val lowerStatus = ff.status.label.lowercase()
        val lowerStation = ff.stationId?.lowercase().orEmpty()
        val certs = ff.certifications

        // Check if all tokens match anywhere
        val allTokensMatch = tokens.all { token ->
            lowerFullName.contains(token) ||
                lowerBadge.contains(token) ||
                lowerRank.contains(token) ||
                lowerEmail.contains(token) ||
                lowerPhone.contains(token) ||
                lowerStatus.contains(token) ||
                lowerStation.contains(token) ||
                certs.any { it.lowercase().contains(token) }
        }

        if (!allTokensMatch) return null

        // Determine primary matched field & relevance score
        val lowerQuery = fullQuery.lowercase()
        var score = 0
        var matchedField = "Name"
        var snippet: String? = null

        when {
            lowerFullName == lowerQuery || lowerLast == lowerQuery -> {
                score = 100
                matchedField = "Full Name"
            }
            lowerFullName.startsWith(lowerQuery) || lowerLast.startsWith(lowerQuery) -> {
                score = 85
                matchedField = "Name Prefix"
            }
            lowerBadge.contains(lowerQuery) -> {
                score = 80
                matchedField = "Badge #"
                snippet = "Badge: ${ff.badgeNumber}"
            }
            lowerRank.contains(lowerQuery) -> {
                score = 70
                matchedField = "Rank"
                snippet = "Rank: ${ff.rank}"
            }
            certs.any { it.lowercase().contains(lowerQuery) } -> {
                score = 65
                matchedField = "Certification"
                val matchedCert = certs.first { it.lowercase().contains(lowerQuery) }
                snippet = "Cert: $matchedCert"
            }
            lowerEmail.contains(lowerQuery) || lowerPhone.contains(lowerQuery) -> {
                score = 50
                matchedField = "Contact"
                snippet = ff.email ?: ff.phone
            }
            lowerFullName.contains(lowerQuery) -> {
                score = 60
                matchedField = "Name"
            }
            else -> {
                score = 40
                matchedField = "Profile Match"
            }
        }

        return GlobalSearchResult.FirefighterMatch(
            firefighter = ff,
            matchedField = matchedField,
            snippet = snippet,
            relevanceScore = score
        )
    }

    private fun matchEquipment(
        eq: Equipment,
        tokens: List<String>,
        fullQuery: String
    ): GlobalSearchResult.EquipmentMatch? {
        val lowerName = eq.name.lowercase()
        val lowerSerial = eq.serialNumber?.lowercase().orEmpty()
        val lowerCategory = eq.category.name.replace('_', ' ').lowercase()
        val lowerStatus = eq.status.label.lowercase()
        val lowerNotes = eq.notes?.lowercase().orEmpty()
        val lowerApparatus = eq.apparatusId?.lowercase().orEmpty()
        val lowerStation = eq.stationId?.lowercase().orEmpty()

        val allTokensMatch = tokens.all { token ->
            lowerName.contains(token) ||
                lowerSerial.contains(token) ||
                lowerCategory.contains(token) ||
                lowerStatus.contains(token) ||
                lowerNotes.contains(token) ||
                lowerApparatus.contains(token) ||
                lowerStation.contains(token)
        }

        if (!allTokensMatch) return null

        val lowerQuery = fullQuery.lowercase()
        var score = 0
        var matchedField = "Equipment"
        var snippet: String? = null

        when {
            lowerName == lowerQuery -> {
                score = 100
                matchedField = "Equipment Name"
            }
            lowerName.startsWith(lowerQuery) -> {
                score = 85
                matchedField = "Name Prefix"
            }
            lowerSerial.contains(lowerQuery) -> {
                score = 80
                matchedField = "Serial Number"
                snippet = "SN: ${eq.serialNumber}"
            }
            lowerCategory.contains(lowerQuery) -> {
                score = 65
                matchedField = "Category"
                snippet = eq.category.name.replace('_', ' ')
            }
            lowerNotes.contains(lowerQuery) -> {
                score = 55
                matchedField = "Notes"
                snippet = eq.notes
            }
            lowerApparatus.contains(lowerQuery) -> {
                score = 50
                matchedField = "Apparatus"
                snippet = "Apparatus: ${eq.apparatusId}"
            }
            lowerName.contains(lowerQuery) -> {
                score = 60
                matchedField = "Name Match"
            }
            else -> {
                score = 40
                matchedField = "Equipment Match"
            }
        }

        return GlobalSearchResult.EquipmentMatch(
            equipment = eq,
            matchedField = matchedField,
            snippet = snippet,
            relevanceScore = score
        )
    }

    private fun matchStation(
        st: Station,
        tokens: List<String>,
        fullQuery: String
    ): GlobalSearchResult.StationMatch? {
        val lowerName = st.name.lowercase()
        val lowerNumber = st.stationNumber?.lowercase().orEmpty()
        val lowerAddress = st.address?.lowercase().orEmpty()
        val lowerPhone = st.phoneNumber?.lowercase().orEmpty()
        val lowerId = st.id.lowercase()

        val allTokensMatch = tokens.all { token ->
            lowerName.contains(token) ||
                lowerNumber.contains(token) ||
                lowerAddress.contains(token) ||
                lowerPhone.contains(token) ||
                lowerId.contains(token)
        }

        if (!allTokensMatch) return null

        val lowerQuery = fullQuery.lowercase()
        var score = 0
        var matchedField = "Station"
        var snippet: String? = null

        when {
            lowerName == lowerQuery -> {
                score = 100
                matchedField = "Station Name"
            }
            lowerName.startsWith(lowerQuery) -> {
                score = 85
                matchedField = "Name Prefix"
            }
            lowerNumber.contains(lowerQuery) -> {
                score = 80
                matchedField = "Station Number"
                snippet = "Station #${st.stationNumber}"
            }
            lowerAddress.contains(lowerQuery) -> {
                score = 65
                matchedField = "Address"
                snippet = st.address
            }
            lowerPhone.contains(lowerQuery) -> {
                score = 50
                matchedField = "Phone"
                snippet = st.phoneNumber
            }
            lowerName.contains(lowerQuery) -> {
                score = 60
                matchedField = "Name Match"
            }
            else -> {
                score = 40
                matchedField = "Station Match"
            }
        }

        return GlobalSearchResult.StationMatch(
            station = st,
            matchedField = matchedField,
            snippet = snippet,
            relevanceScore = score
        )
    }
}
