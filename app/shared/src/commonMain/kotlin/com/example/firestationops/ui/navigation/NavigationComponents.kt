package com.example.firestationops.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * The primary navigation destinations supported by the shared navigation components.
 */
enum class AppNavDestination(
    val route: String,
    val title: String,
    val testTag: String
) {
    DASHBOARD("dashboard", "Dashboard", "nav_tab_dashboard"),
    PERSONNEL("personnel", "Personnel", "nav_tab_personnel"),
    EQUIPMENT("equipment", "Equipment", "nav_tab_equipment"),
    SHIFTS("shifts", "Shifts", "nav_tab_shifts"),
    SEARCH("search", "Search", "nav_tab_search")
}

/**
 * Standard Material 3 Bottom Navigation Bar for compact (handheld/mobile) displays.
 */
@Composable
fun AppBottomNavigationBar(
    selectedDestination: AppNavDestination,
    onDestinationSelected: (AppNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("app_bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        AppNavDestination.entries.forEach { destination ->
            val isSelected = destination == selectedDestination
            NavigationBarItem(
                modifier = Modifier.testTag(destination.testTag),
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    NavIcon(destination = destination, isSelected = isSelected)
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Standard Material 3 Navigation Rail for expanded/desktop/tablet landscape displays.
 */
@Composable
fun AppNavigationRail(
    selectedDestination: AppNavDestination,
    onDestinationSelected: (AppNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null
) {
    NavigationRail(
        modifier = modifier.testTag("app_navigation_rail"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = header
    ) {
        AppNavDestination.entries.forEach { destination ->
            val isSelected = destination == selectedDestination
            NavigationRailItem(
                modifier = Modifier.testTag(destination.testTag),
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    NavIcon(destination = destination, isSelected = isSelected)
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun NavIcon(destination: AppNavDestination, isSelected: Boolean) {
    // Custom vector indicator icons
    when (destination) {
        AppNavDestination.DASHBOARD -> {
            Canvas(modifier = Modifier.size(24.dp)) {
                val color = if (isSelected) {
                    Color(0xFFB71C1C) // Fire red
                } else {
                    Color.Gray
                }
                val strokeWidth = 2.dp.toPx()
                // House/station roof + base
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.15f)
                    lineTo(size.width * 0.85f, size.height * 0.45f)
                    lineTo(size.width * 0.75f, size.height * 0.45f)
                    lineTo(size.width * 0.75f, size.height * 0.85f)
                    lineTo(size.width * 0.25f, size.height * 0.85f)
                    lineTo(size.width * 0.25f, size.height * 0.45f)
                    lineTo(size.width * 0.15f, size.height * 0.45f)
                    close()
                }
                drawPath(
                    path = path,
                    color = color,
                    style = if (isSelected) Fill else Stroke(strokeWidth)
                )
            }
        }
        AppNavDestination.PERSONNEL -> {
            Canvas(modifier = Modifier.size(24.dp)) {
                val color = if (isSelected) {
                    Color(0xFFB71C1C)
                } else {
                    Color.Gray
                }
                val strokeWidth = 2.dp.toPx()
                // Head circle
                drawCircle(
                    color = color,
                    radius = size.width * 0.2f,
                    center = Offset(size.width * 0.5f, size.height * 0.32f),
                    style = if (isSelected) Fill else Stroke(strokeWidth)
                )
                // Shoulders / torso arc
                val path = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.85f)
                    cubicTo(
                        size.width * 0.2f, size.height * 0.62f,
                        size.width * 0.8f, size.height * 0.62f,
                        size.width * 0.8f, size.height * 0.85f
                    )
                    close()
                }
                drawPath(
                    path = path,
                    color = color,
                    style = if (isSelected) Fill else Stroke(strokeWidth)
                )
            }
        }
        AppNavDestination.EQUIPMENT -> {
            Canvas(modifier = Modifier.size(24.dp)) {
                val color = if (isSelected) {
                    Color(0xFFB71C1C)
                } else {
                    Color.Gray
                }
                val strokeWidth = 2.dp.toPx()
                // Tool/gear box outline
                val cornerRadius = CornerRadius(4.dp.toPx())
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.15f, size.height * 0.35f),
                    size = Size(size.width * 0.7f, size.height * 0.5f),
                    cornerRadius = cornerRadius,
                    style = if (isSelected) Fill else Stroke(strokeWidth)
                )
                // Top handle
                val handlePath = Path().apply {
                    moveTo(size.width * 0.35f, size.height * 0.35f)
                    lineTo(size.width * 0.35f, size.height * 0.2f)
                    lineTo(size.width * 0.65f, size.height * 0.2f)
                    lineTo(size.width * 0.65f, size.height * 0.35f)
                }
                drawPath(
                    path = handlePath,
                    color = color,
                    style = Stroke(strokeWidth)
                )
            }
        }
        AppNavDestination.SHIFTS -> {
            Canvas(modifier = Modifier.size(24.dp)) {
                val color = if (isSelected) {
                    Color(0xFFB71C1C)
                } else {
                    Color.Gray
                }
                val strokeWidth = 2.dp.toPx()
                // Clock / Shift dial circle
                drawCircle(
                    color = color,
                    radius = size.width * 0.38f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    style = Stroke(strokeWidth)
                )
                // Clock hands (indicating duty shift timing)
                val handsPath = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.5f)
                    lineTo(size.width * 0.5f, size.height * 0.25f)
                    moveTo(size.width * 0.5f, size.height * 0.5f)
                    lineTo(size.width * 0.72f, size.height * 0.5f)
                }
                drawPath(
                    path = handsPath,
                    color = color,
                    style = Stroke(strokeWidth)
                )
            }
        }
        AppNavDestination.SEARCH -> {
            Canvas(modifier = Modifier.size(24.dp)) {
                val color = if (isSelected) {
                    Color(0xFFB71C1C)
                } else {
                    Color.Gray
                }
                val strokeWidth = 2.dp.toPx()
                // Magnifying glass lens circle
                drawCircle(
                    color = color,
                    radius = size.width * 0.28f,
                    center = Offset(size.width * 0.42f, size.height * 0.42f),
                    style = Stroke(strokeWidth)
                )
                // Magnifying glass handle
                val handlePath = Path().apply {
                    moveTo(size.width * 0.62f, size.height * 0.62f)
                    lineTo(size.width * 0.85f, size.height * 0.85f)
                }
                drawPath(
                    path = handlePath,
                    color = color,
                    style = Stroke(strokeWidth)
                )
            }
        }
    }
}
