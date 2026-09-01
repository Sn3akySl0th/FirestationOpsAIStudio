package com.example.firestationops.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigationStateTest {

    @Test
    fun testInitialDestinationIsDashboard() {
        val navState = AppNavigationState()
        assertEquals(AppNavDestination.DASHBOARD, navState.currentDestination)
    }

    @Test
    fun testNavigateToPersonnel() {
        val navState = AppNavigationState()
        navState.navigateTo(AppNavDestination.PERSONNEL)
        assertEquals(AppNavDestination.PERSONNEL, navState.currentDestination)
    }

    @Test
    fun testNavigateToEquipment() {
        val navState = AppNavigationState()
        navState.navigateTo(AppNavDestination.EQUIPMENT)
        assertEquals(AppNavDestination.EQUIPMENT, navState.currentDestination)
    }

    @Test
    fun testNavigateBackToDashboard() {
        val navState = AppNavigationState(AppNavDestination.EQUIPMENT)
        assertEquals(AppNavDestination.EQUIPMENT, navState.currentDestination)
        navState.navigateTo(AppNavDestination.DASHBOARD)
        assertEquals(AppNavDestination.DASHBOARD, navState.currentDestination)
    }

    @Test
    fun testDestinationProperties() {
        assertEquals("Dashboard", AppNavDestination.DASHBOARD.title)
        assertEquals("dashboard", AppNavDestination.DASHBOARD.route)
        assertEquals("nav_tab_dashboard", AppNavDestination.DASHBOARD.testTag)

        assertEquals("Personnel", AppNavDestination.PERSONNEL.title)
        assertEquals("personnel", AppNavDestination.PERSONNEL.route)
        assertEquals("nav_tab_personnel", AppNavDestination.PERSONNEL.testTag)

        assertEquals("Equipment", AppNavDestination.EQUIPMENT.title)
        assertEquals("equipment", AppNavDestination.EQUIPMENT.route)
        assertEquals("nav_tab_equipment", AppNavDestination.EQUIPMENT.testTag)

        assertEquals("Search", AppNavDestination.SEARCH.title)
        assertEquals("search", AppNavDestination.SEARCH.route)
        assertEquals("nav_tab_search", AppNavDestination.SEARCH.testTag)
    }
}
