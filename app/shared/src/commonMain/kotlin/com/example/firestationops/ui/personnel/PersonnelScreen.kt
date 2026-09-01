package com.example.firestationops.ui.personnel

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firestationops.domain.export.PersonnelCsvExporter
import com.example.firestationops.model.Firefighter
import com.example.firestationops.model.PersonnelStatus
import com.example.firestationops.platform.ExportResult
import com.example.firestationops.platform.rememberFileExporter
import kotlinx.coroutines.launch

/**
 * Screen displaying the roster of department personnel, ranks, certifications, and operational readiness.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonnelScreen(
    firefighters: List<Firefighter>,
    onStatusChange: (firefighterId: String, newStatus: PersonnelStatus) -> Unit = { _, _ -> },
    onFirefighterClick: (Firefighter) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val fileExporter = rememberFileExporter()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isExporting by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatuses by remember { mutableStateOf<Set<PersonnelStatus>>(emptySet()) }
    var selectedFirefighterForDialog by remember { mutableStateOf<Firefighter?>(null) }

    val statusCounts = remember(firefighters) {
        PersonnelStatus.entries.associateWith { status ->
            firefighters.count { it.status == status }
        }
    }

    val filteredList = remember(firefighters, searchQuery, selectedStatuses) {
        firefighters.filter { ff ->
            val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.contains(ff.status)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                ff.fullName.lowercase().contains(q) ||
                    (ff.rank?.lowercase()?.contains(q) == true) ||
                    (ff.badgeNumber?.lowercase()?.contains(q) == true) ||
                    ff.certifications.any { it.lowercase().contains(q) }
            }
            matchesStatus && matchesSearch
        }
    }

    val readyCount = remember(firefighters) { firefighters.count { it.isReadyToRespond } }
    val engagedCount = remember(firefighters) { firefighters.count { it.status.isActivelyEngaged } }

    Scaffold(
        modifier = modifier.testTag("personnel_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Department Personnel",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$readyCount Ready for Duty • $engagedCount Active on Call • ${firefighters.size} Total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                val exportData = if (filteredList.isNotEmpty()) filteredList else firefighters
                                val deptId = firefighters.firstOrNull()?.departmentId ?: "department"
                                val csvContent = PersonnelCsvExporter.export(
                                    firefighters = exportData,
                                    departmentName = deptId
                                )
                                val fileName = "personnel_roster_${deptId}.csv"
                                val result = fileExporter.saveTextFile(fileName, csvContent)
                                isExporting = false
                                snackbarHostState.showSnackbar(
                                    when (result) {
                                        ExportResult.Success -> "Personnel roster CSV exported ($fileName)"
                                        ExportResult.Cancelled -> "Export cancelled"
                                        is ExportResult.Error -> "Export failed: ${result.message}"
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("personnel_export_csv_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        enabled = !isExporting && firefighters.isNotEmpty()
                    ) {
                        Text(if (isExporting) "Exporting..." else "Export CSV")
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
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("personnel_search_input"),
                placeholder = { Text("Search name, role, badge, certification...") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        TextButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Text("Clear")
                        }
                    }
                }
            )

            // Status Filter Component
            PersonnelStatusFilterBar(
                selectedStatuses = selectedStatuses,
                onStatusToggled = { status ->
                    selectedStatuses = if (selectedStatuses.contains(status)) {
                        selectedStatuses - status
                    } else {
                        selectedStatuses + status
                    }
                },
                onSelectAll = { selectedStatuses = emptySet() },
                onSelectPreset = { presetSet -> selectedStatuses = presetSet },
                statusCounts = statusCounts,
                totalCount = firefighters.size,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Firefighter roster list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("firefighters_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp)
                                .testTag("empty_personnel_state"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No personnel found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Try adjusting your search query or filter selection.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (searchQuery.isNotEmpty() || selectedStatuses.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            searchQuery = ""
                                            selectedStatuses = emptySet()
                                        },
                                        modifier = Modifier.testTag("reset_personnel_filters_btn")
                                    ) {
                                        Text("Reset Filters")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { firefighter ->
                        FirefighterListItem(
                            firefighter = firefighter,
                            onItemClick = {
                                onFirefighterClick(firefighter)
                                selectedFirefighterForDialog = firefighter
                            },
                            onChangeStatusClick = {
                                selectedFirefighterForDialog = firefighter
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail & Status Selection Dialog
    selectedFirefighterForDialog?.let { ff ->
        FirefighterDetailDialog(
            firefighter = ff,
            onDismiss = { selectedFirefighterForDialog = null },
            onStatusSelected = { newStatus ->
                onStatusChange(ff.id, newStatus)
                selectedFirefighterForDialog = null
            }
        )
    }
}
