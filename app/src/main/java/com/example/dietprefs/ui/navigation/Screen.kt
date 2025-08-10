package com.example.dietprefs.ui.navigation

sealed class Screen(val route: String) {
    object Preferences : Screen("preferences")
    object SearchResults : Screen("searchResults")
    object Settings : Screen("SettingsScreen")
    object VendorDetail : Screen("VendorDetailScreen")
}