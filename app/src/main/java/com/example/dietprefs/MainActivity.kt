package com.example.dietprefs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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

