package com.example.firestationops.ui.equipment

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.firestationops.model.Equipment
import com.example.firestationops.model.EquipmentCategory
import com.example.firestationops.model.EquipmentStatus

/**
 * Screen displaying department equipment, tracking status, readiness, inspections, categories, and maintenance needs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(
    equipmentList: List<Equipment>,
    onStatusChange: (equipmentId: String, newStatus: EquipmentStatus) -> Unit = { _, _ -> },
    onEquipmentClick: (Equipment) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<EquipmentCategory?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<EquipmentStatus?>(null) }
    var selectedEquipmentForDialog by remember { mutableStateOf<Equipment?>(null) }

    val filteredList = remember(equipmentList, searchQuery, selectedCategory, selectedStatusFilter) {
        equipmentList.filter { eq ->
            val matchesCategory = selectedCategory == null || eq.category == selectedCategory
            val matchesStatus = selectedStatusFilter == null || eq.status == selectedStatusFilter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                eq.name.lowercase().contains(q) ||
                    eq.category.name.lowercase().contains(q) ||
                    (eq.serialNumber?.lowercase()?.contains(q) == true) ||
                    (eq.barcode?.lowercase()?.contains(q) == true) ||
                    (eq.apparatusId?.lowercase()?.contains(q) == true) ||
                    (eq.notes?.lowercase()?.contains(q) == true)
            }
            matchesCategory && matchesStatus && matchesSearch
        }
    }

    val inServiceCount = remember(equipmentList) { equipmentList.count { it.status == EquipmentStatus.IN_SERVICE } }
    val maintenanceNeededCount = remember(equipmentList) { equipmentList.count { it.status == EquipmentStatus.MAINTENANCE_REQUIRED } }
    val outOfServiceCount = remember(equipmentList) { equipmentList.count { it.status == EquipmentStatus.OUT_OF_SERVICE } }
    val reserveCount = remember(equipmentList) { equipmentList.count { it.status == EquipmentStatus.RESERVE } }
    val totalAttentionNeeded = maintenanceNeededCount + outOfServiceCount

    Scaffold(
        modifier = modifier.testTag("equipment_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Station Equipment & Gear",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("equipment_title")
                        )
                        Text(
                            text = "$inServiceCount In Service • $totalAttentionNeeded Need Attention • $reserveCount Reserve",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, S/N, barcode, apparatus...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("equipment_search_input")
            )

            // Status Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("All Statuses (${equipmentList.size})") },
                        modifier = Modifier.testTag("filter_status_all")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EquipmentStatus.IN_SERVICE,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == EquipmentStatus.IN_SERVICE) null else EquipmentStatus.IN_SERVICE
                        },
                        label = { Text("In Service ($inServiceCount)") },
                        modifier = Modifier.testTag("filter_status_in_service")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EquipmentStatus.MAINTENANCE_REQUIRED,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == EquipmentStatus.MAINTENANCE_REQUIRED) null else EquipmentStatus.MAINTENANCE_REQUIRED
                        },
                        label = { Text("Maint Required ($maintenanceNeededCount)") },
                        modifier = Modifier.testTag("filter_status_maintenance")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EquipmentStatus.OUT_OF_SERVICE,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == EquipmentStatus.OUT_OF_SERVICE) null else EquipmentStatus.OUT_OF_SERVICE
                        },
                        label = { Text("Out of Service ($outOfServiceCount)") },
                        modifier = Modifier.testTag("filter_status_out_of_service")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == EquipmentStatus.RESERVE,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == EquipmentStatus.RESERVE) null else EquipmentStatus.RESERVE
                        },
                        label = { Text("Reserve ($reserveCount)") },
                        modifier = Modifier.testTag("filter_status_reserve")
                    )
                }
            }

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Categories") },
                        modifier = Modifier.testTag("filter_category_all")
                    )
                }
                EquipmentCategory.entries.forEach { cat ->
                    val count = equipmentList.count { it.category == cat }
                    if (count > 0) {
                        item {
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                },
                                label = { Text("${cat.name.replace('_', ' ')} ($count)") },
                                modifier = Modifier.testTag("filter_category_${cat.name}")
                            )
                        }
                    }
                }
            }

            // Maintenance Attention Banner (if items require maintenance or are out of service)
            if (totalAttentionNeeded > 0 && selectedStatusFilter == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("maintenance_alert_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "⚠️ Maintenance Attention Needed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "$totalAttentionNeeded item(s) currently require repair, servicing, or are out of service.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }
            }

            // Main Equipment List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No Equipment Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "No equipment matches the search query or status filter.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedCategory = null
                                        selectedStatusFilter = null
                                    },
                                    modifier = Modifier.testTag("reset_equipment_filters_btn")
                                ) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { equipment ->
                        EquipmentListItem(
                            equipment = equipment,
                            onItemClick = { onEquipmentClick(equipment) },
                            onChangeStatusClick = { selectedEquipmentForDialog = equipment }
                        )
                    }
                }
            }
        }
    }

    // Detail & Status Dialog
    selectedEquipmentForDialog?.let { eq ->
        EquipmentDetailDialog(
            equipment = eq,
            onDismiss = { selectedEquipmentForDialog = null },
            onStatusSelected = { newStatus ->
                onStatusChange(eq.id, newStatus)
                selectedEquipmentForDialog = null
            }
        )
    }
}
