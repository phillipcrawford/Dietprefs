package com.example.dietprefs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dietprefs.ui.navigation.AppNavGraph
import com.example.dietprefs.ui.screens.PreferenceScreen
import com.example.dietprefs.ui.screens.SearchResultsScreen
import com.example.dietprefs.viewmodel.SharedViewModel
import com.example.dietprefs.ui.theme.DietprefsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only one instance of SharedViewModel tied to this Activity
        val sharedViewModel: SharedViewModel by viewModels()

        setContent {
            DietprefsTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController, sharedViewModel = sharedViewModel)
            }
        }
    }
}

//@Composable
//fun AppNavHost(
//    navController: NavHostController,
//    sharedViewModel: SharedViewModel
//) {
//    NavHost(
//        navController = navController,
//        startDestination = "PreferenceScreen"
//    ) {
//        composable("PreferenceScreen") {
//            PreferenceScreen(
//                onSearchClick = { navController.navigate("SearchResultsScreen") },
//                onSettingsClick = { /* TODO: Settings */ },
//                onUserModeClick = { /* TODO: toggle user mode */ },
//                sharedViewModel = sharedViewModel
//            )
//        }
//        composable("SearchResultsScreen") {
//            SearchResultsScreen(
//                navController = navController,
//                onSettingsClick = { /* TODO: Settings */ },
//                sharedViewModel = sharedViewModel
//            )
//        }
//    }
//}