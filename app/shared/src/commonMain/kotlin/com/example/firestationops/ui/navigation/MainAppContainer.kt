package com.example.firestationops.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.firestationops.domain.model.Member

/**
 * Main application container that integrates navigation between Dashboard, Personnel, and Equipment views
 * alongside account summary and logout actions in an adaptive multiplatform layout.
 */
@Composable
fun MainAppContainer(
    member: Member,
    onLogout: () -> Unit,
    navigationState: AppNavigationState,
    dashboardContent: @Composable () -> Unit,
    personnelContent: @Composable () -> Unit,
    equipmentContent: @Composable () -> Unit,
    shiftsContent: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_app_container")
    ) {
        val isExpanded = maxWidth >= 840.dp

        if (isExpanded) {
            // Wide / Desktop / Tablet Landscape
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(108.dp)
                ) {
                    AppNavigationRail(
                        selectedDestination = navigationState.currentDestination,
                        onDestinationSelected = { dest -> navigationState.navigateTo(dest) },
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = member.firstName,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Button(
                                onClick = onLogout,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("logout_button_wide"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Text("Logout", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (navigationState.currentDestination) {
                        AppNavDestination.DASHBOARD -> dashboardContent()
                        AppNavDestination.PERSONNEL -> personnelContent()
                        AppNavDestination.EQUIPMENT -> equipmentContent()
                        AppNavDestination.SHIFTS -> shiftsContent()
                    }
                }
            }
        } else {
            // Compact / Mobile Layout
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (navigationState.currentDestination) {
                        AppNavDestination.DASHBOARD -> dashboardContent()
                        AppNavDestination.PERSONNEL -> personnelContent()
                        AppNavDestination.EQUIPMENT -> equipmentContent()
                        AppNavDestination.SHIFTS -> shiftsContent()
                    }
                }

                // Top user info summary strip above nav bar
                Surface(
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = buildString {
                                    append(member.fullName)
                                    member.memberNumber?.let { append(" (#$it)") }
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "Dept ${member.departmentId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onLogout,
                            modifier = Modifier.testTag("logout_button_compact"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Logout", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // Navigation Bar for switching views
                AppBottomNavigationBar(
                    selectedDestination = navigationState.currentDestination,
                    onDestinationSelected = { dest -> navigationState.navigateTo(dest) },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}
