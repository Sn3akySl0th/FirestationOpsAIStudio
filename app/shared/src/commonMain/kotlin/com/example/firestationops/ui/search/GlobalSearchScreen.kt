package com.example.firestationops.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.domain.search.GlobalSearchResult
import com.example.firestationops.domain.search.GlobalSearchResults
import com.example.firestationops.domain.search.SearchFilterCategory
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentStatus
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.model.Station
import com.example.firestationops.ui.equipment.EquipmentDetailDialog
import com.example.firestationops.ui.personnel.FirefighterDetailDialog
import com.example.firestationops.ui.station.StationDetailDialog

/**
 * Global Search Screen for finding firefighters, equipment, and station records
 * across the department with instant filtering and detail inspection.
 */
@Composable
fun GlobalSearchScreen(
    searchResults: GlobalSearchResults,
    onQueryChange: (String) -> Unit,
    onFilterChange: (SearchFilterCategory) -> Unit,
    onUpdateFirefighterStatus: (String, PersonnelStatus) -> Unit,
    onUpdateEquipmentStatus: (String, EquipmentStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFirefighter by remember { mutableStateOf<Firefighter?>(null) }
    var selectedEquipment by remember { mutableStateOf<Equipment?>(null) }
    var selectedStation by remember { mutableStateOf<Station?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("global_search_screen")
    ) {
        // Header & Search Input
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Global Search",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Search firefighters, equipment inventory, and station records",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Search Bar Input
                OutlinedTextField(
                    value = searchResults.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input"),
                    placeholder = {
                        Text("Search by name, badge #, serial #, cert, rank, address...")
                    },
                    leadingIcon = {
                        SearchCanvasIcon(
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchResults.query.isNotEmpty()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.testTag("global_search_clear_button")
                            ) {
                                ClearCanvasIcon(
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )

                // Category Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_filter_chips_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SearchFilterCategory.entries) { category ->
                        val count = when (category) {
                            SearchFilterCategory.ALL -> searchResults.totalCount
                            SearchFilterCategory.FIREFIGHTERS -> searchResults.totalFirefightersCount
                            SearchFilterCategory.EQUIPMENT -> searchResults.totalEquipmentCount
                            SearchFilterCategory.STATIONS -> searchResults.totalStationsCount
                        }
                        val isSelected = searchResults.activeFilter == category
                        val chipTestTag = when (category) {
                            SearchFilterCategory.ALL -> "search_filter_chip_all"
                            SearchFilterCategory.FIREFIGHTERS -> "search_filter_chip_firefighters"
                            SearchFilterCategory.EQUIPMENT -> "search_filter_chip_equipment"
                            SearchFilterCategory.STATIONS -> "search_filter_chip_stations"
                        }

                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { onFilterChange(category) },
                            label = {
                                Text(
                                    text = if (searchResults.query.isNotBlank()) "${category.label} ($count)" else category.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(chipTestTag),
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Content Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                searchResults.query.isBlank() -> {
                    SearchEmptyQueryPrompt(
                        onSuggestionClick = { onQueryChange(it) }
                    )
                }
                searchResults.isEmpty -> {
                    SearchNoResultsPrompt(
                        query = searchResults.query,
                        onClearQuery = { onQueryChange("") }
                    )
                }
                else -> {
                    SearchResultsList(
                        results = searchResults.results,
                        totalCount = searchResults.results.size,
                        query = searchResults.query,
                        onResultClick = { result ->
                            when (result) {
                                is GlobalSearchResult.FirefighterMatch -> selectedFirefighter = result.firefighter
                                is GlobalSearchResult.EquipmentMatch -> selectedEquipment = result.equipment
                                is GlobalSearchResult.StationMatch -> selectedStation = result.station
                            }
                        }
                    )
                }
            }
        }
    }

    // Detail Dialogs
    selectedFirefighter?.let { ff ->
        FirefighterDetailDialog(
            firefighter = ff,
            onDismiss = { selectedFirefighter = null },
            onStatusSelected = { newStatus ->
                onUpdateFirefighterStatus(ff.id, newStatus)
                selectedFirefighter = null
            }
        )
    }

    selectedEquipment?.let { eq ->
        EquipmentDetailDialog(
            equipment = eq,
            onDismiss = { selectedEquipment = null },
            onStatusSelected = { newStatus ->
                onUpdateEquipmentStatus(eq.id, newStatus)
                selectedEquipment = null
            }
        )
    }

    selectedStation?.let { st ->
        StationDetailDialog(
            station = st,
            onDismiss = { selectedStation = null }
        )
    }
}

@Composable
private fun SearchResultsList(
    results: List<GlobalSearchResult>,
    totalCount: Int,
    query: String,
    onResultClick: (GlobalSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_results_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Found $totalCount match${if (totalCount == 1) "" else "es"} for \"$query\"",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp).testTag("search_results_count_header")
            )
        }

        items(results, key = { "${it.category.name}_${it.id}" }) { result ->
            SearchResultCard(
                result = result,
                onClick = { onResultClick(result) }
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    result: GlobalSearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search_result_card_${result.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Badge
                CategoryBadge(category = result.category)

                // Matched Field Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Match: ${result.matchedField}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = result.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Snippet if available
            result.snippet?.let { snippet ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = snippet,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(
    category: SearchFilterCategory,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (category) {
        SearchFilterCategory.FIREFIGHTERS -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF1B5E20),
            "Firefighter"
        )
        SearchFilterCategory.EQUIPMENT -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF0D47A1),
            "Equipment"
        )
        SearchFilterCategory.STATIONS -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            "Station"
        )
        SearchFilterCategory.ALL -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Record"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SearchEmptyQueryPrompt(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("search_empty_query_prompt"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                SearchCanvasIcon(
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Department Global Search",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Type any keyword to instantly query personnel, apparatus equipment, certifications, serial numbers, and station locations.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Popular Searches",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        val suggestions = listOf("Captain", "Paramedic", "SCBA", "Thermal", "Station 51", "Engineer", "Ventilation")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(suggestions) { suggestion ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onSuggestionClick(suggestion) }
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchNoResultsPrompt(
    query: String,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("search_no_results_prompt"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No matches found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "No firefighters, equipment, or stations matched \"$query\". Check your spelling or try broader search terms.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.clickable(onClick = onClearQuery)
        ) {
            Text(
                text = "Clear Query",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SearchCanvasIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val radius = size.minDimension * 0.32f
        val center = Offset(size.width * 0.42f, size.height * 0.42f)
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        val handleStart = Offset(
            x = center.x + radius * 0.7071f,
            y = center.y + radius * 0.7071f
        )
        val handleEnd = Offset(
            x = size.width * 0.88f,
            y = size.height * 0.88f
        )
        drawLine(
            color = tint,
            start = handleStart,
            end = handleEnd,
            strokeWidth = strokeWidth * 1.2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ClearCanvasIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val pad = size.minDimension * 0.22f
        drawLine(
            color = tint,
            start = Offset(pad, pad),
            end = Offset(size.width - pad, size.height - pad),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width - pad, pad),
            end = Offset(pad, size.height - pad),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
