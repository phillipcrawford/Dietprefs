package com.example.dietprefs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dietprefs.ui.screens.PreferenceScreen
import com.example.dietprefs.ui.screens.SearchResultsScreen
import com.example.dietprefs.viewmodel.SharedViewModel

@Composable
fun AppNavGraph(navController: NavHostController, sharedViewModel: SharedViewModel) {
    //val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Preferences.route
    ) {
        composable(Screen.Preferences.route) {
            PreferenceScreen(
                onSearchClick = { navController.navigate(Screen.SearchResults.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onUserModeClick = { /* Handle user switch */ },
                sharedViewModel = sharedViewModel
            )
        }
        composable(Screen.SearchResults.route) {
            SearchResultsScreen(
                navController = navController,
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                sharedViewModel = sharedViewModel
            )
        }

        composable(Screen.Settings.route) {
            /* TODO: Implement SettingsScreen() */
        }
    }
}