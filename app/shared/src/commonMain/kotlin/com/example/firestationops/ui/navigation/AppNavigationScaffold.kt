package com.example.firestationops.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * State holder for simple Compose Multiplatform navigation between top-level views.
 */
class AppNavigationState(
    initialDestination: AppNavDestination = AppNavDestination.DASHBOARD
) {
    var currentDestination by mutableStateOf(initialDestination)
        private set

    fun navigateTo(destination: AppNavDestination) {
        currentDestination = destination
    }
}

@Composable
fun rememberAppNavigationState(
    initialDestination: AppNavDestination = AppNavDestination.DASHBOARD
): AppNavigationState {
    return remember { AppNavigationState(initialDestination) }
}

/**
 * Adaptive navigation host that automatically renders a NavigationRail on wide/desktop displays
 * and a NavigationBar on compact/mobile displays to switch seamlessly between Dashboard,
 * Personnel, and Equipment views.
 */
@Composable
fun AppNavigationScaffold(
    navigationState: AppNavigationState,
    dashboardContent: @Composable () -> Unit,
    personnelContent: @Composable () -> Unit,
    equipmentContent: @Composable () -> Unit,
    shiftsContent: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("app_navigation_scaffold")) {
        val isExpanded = maxWidth >= 840.dp

        if (isExpanded) {
            // Wide / Desktop / Tablet Landscape Layout: Side Navigation Rail + Main View
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavigationRail(
                    selectedDestination = navigationState.currentDestination,
                    onDestinationSelected = { dest -> navigationState.navigateTo(dest) }
                )
                Surface(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHostContent(
                        currentDestination = navigationState.currentDestination,
                        dashboardContent = dashboardContent,
                        personnelContent = personnelContent,
                        equipmentContent = equipmentContent,
                        shiftsContent = shiftsContent
                    )
                }
            }
        } else {
            // Compact / Mobile Layout: Main View + Bottom Navigation Bar
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AppNavHostContent(
                        currentDestination = navigationState.currentDestination,
                        dashboardContent = dashboardContent,
                        personnelContent = personnelContent,
                        equipmentContent = equipmentContent,
                        shiftsContent = shiftsContent
                    )
                }
                AppBottomNavigationBar(
                    selectedDestination = navigationState.currentDestination,
                    onDestinationSelected = { dest -> navigationState.navigateTo(dest) },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun AppNavHostContent(
    currentDestination: AppNavDestination,
    dashboardContent: @Composable () -> Unit,
    personnelContent: @Composable () -> Unit,
    equipmentContent: @Composable () -> Unit,
    shiftsContent: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "nav_content_transition"
    ) { destination ->
        when (destination) {
            AppNavDestination.DASHBOARD -> dashboardContent()
            AppNavDestination.PERSONNEL -> personnelContent()
            AppNavDestination.EQUIPMENT -> equipmentContent()
            AppNavDestination.SHIFTS -> shiftsContent()
        }
    }
}
