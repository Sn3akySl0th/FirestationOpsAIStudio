package com.example.firestationops.ui.shift

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.model.AvailabilityPattern
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.FirefighterAvailability

/**
 * View to inspect and manage department firefighter availability patterns, preferences, and callback eligibility.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvailabilityPatternView(
    firefighters: List<Firefighter>,
    availabilities: Map<String, FirefighterAvailability>,
    onEditAvailability: (Firefighter) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPatternFilter by remember { mutableStateOf<AvailabilityPattern?>(null) }

    val filteredFirefighters = remember(firefighters, availabilities, searchQuery, selectedPatternFilter) {
        firefighters.filter { ff ->
            val avail = availabilities[ff.id]
            val pattern = avail?.pattern ?: AvailabilityPattern.ALWAYS_AVAILABLE
            val matchesPattern = selectedPatternFilter == null || pattern == selectedPatternFilter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                ff.fullName.lowercase().contains(q) ||
                    (ff.rank?.lowercase()?.contains(q) == true) ||
                    (ff.badgeNumber?.lowercase()?.contains(q) == true) ||
                    (avail?.notes?.lowercase()?.contains(q) == true)
            }
            matchesPattern && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("availability_pattern_view")
    ) {
        // Search and Pattern Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search firefighter availability, rank, or notes...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("availability_search_input"),
            singleLine = true
        )

        // Pattern filter chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = selectedPatternFilter == null,
                onClick = { selectedPatternFilter = null },
                label = { Text("All Patterns (${firefighters.size})", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.testTag("filter_all_patterns")
            )
            AvailabilityPattern.entries.forEach { pattern ->
                val count = firefighters.count { ff ->
                    (availabilities[ff.id]?.pattern ?: AvailabilityPattern.ALWAYS_AVAILABLE) == pattern
                }
                if (count > 0) {
                    FilterChip(
                        selected = selectedPatternFilter == pattern,
                        onClick = {
                            selectedPatternFilter = if (selectedPatternFilter == pattern) null else pattern
                        },
                        label = { Text("${pattern.label} ($count)", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("filter_pattern_${pattern.name.lowercase()}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Firefighter Availability List
        if (filteredFirefighters.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "No firefighters match the selected availability filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(filteredFirefighters) { ff ->
                    val avail = availabilities[ff.id]
                    val pattern = avail?.pattern ?: AvailabilityPattern.ALWAYS_AVAILABLE
                    val availableDays = avail?.availableDays ?: listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    val isOt = avail?.isAvailableForOvertime ?: true

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firefighter_availability_card_${ff.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = ff.fullName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (ff.isOfficer) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "★ ${ff.rank ?: "Officer"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Badge #${ff.badgeNumber ?: "N/A"} • ${ff.status.label}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onEditAvailability(ff) },
                                    modifier = Modifier.testTag("edit_availability_button_${ff.id}"),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("Change Pattern", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Pattern Badge + Overtime Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AvailabilityPatternBadge(pattern = pattern)

                                if (isOt) {
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFF81C784))
                                    ) {
                                        Text(
                                            text = "⚡ Overtime / 2nd Alarm",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF1B5E20),
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Available Days
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Days:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = availableDays.joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Custom availability note if present
                            avail?.notes?.let { noteText ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Note: $noteText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
