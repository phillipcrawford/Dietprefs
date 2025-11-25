package com.example.dietprefs.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dietprefs.Constants
import com.example.dietprefs.model.Preference
import com.example.dietprefs.ui.components.PreferenceGrid
import com.example.dietprefs.ui.components.PriceSelectionDialog
import com.example.dietprefs.ui.theme.backgroundGrey
import com.example.dietprefs.ui.theme.dietprefsGrey
import com.example.dietprefs.ui.theme.dietprefsTeal
import com.example.dietprefs.ui.theme.selectedGrey
import com.example.dietprefs.ui.theme.selectedTeal
import com.example.dietprefs.ui.theme.user1Red
import com.example.dietprefs.ui.theme.user2Magenta
import com.example.dietprefs.viewmodel.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun PreferenceScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUserModeClick: () -> Unit,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val user1Prefs by sharedViewModel.user1Prefs.collectAsState()
    val user2Prefs by sharedViewModel.user2Prefs.collectAsState()
    val user1MaxPrice by sharedViewModel.user1MaxPrice.collectAsState()
    val user2MaxPrice by sharedViewModel.user2MaxPrice.collectAsState()
    val appConfig by sharedViewModel.appConfig.collectAsState()
    val isUser2Active = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Price dialog state
    var showPriceDialog by remember { mutableStateOf(false) }
    // Use config from backend, fallback to Constants if not loaded yet
    val priceOptions = remember(appConfig) {
        appConfig?.pricing?.defaultOptions ?:
        (Constants.MIN_PRICE.toInt()..Constants.MAX_PRICE.toInt()).map { it.toFloat() }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, request location and search
            scope.launch {
                // Wait for location to be retrieved
                sharedViewModel.requestUserLocation(context)
                // Now search with location
                sharedViewModel.searchVendors()
                onSearchClick()
            }
        } else {
            // Permission denied, search without location
            sharedViewModel.searchVendors()
            onSearchClick()
        }
    }

    Scaffold(
        topBar = {
            PreferencesTopBar(
                user1Prefs = user1Prefs,
                user2Prefs = user2Prefs,
                user1MaxPrice = user1MaxPrice,
                user2MaxPrice = user2MaxPrice,
                onSettingsClick = onSettingsClick,
                onUserModeClick = onUserModeClick
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(dietprefsGrey)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        // Request location permissions
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Search", color = user1Red, fontSize = 32.sp)
                }
                Button(
                    onClick = {
                        sharedViewModel.clearAllFilters()
                        isUser2Active.value = false
                    },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB35100))
                ) {
                    Text("C", color = Color.White, fontSize = 20.sp)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundGrey)
                .padding(0.dp, 4.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                PreferenceGrid(
                    preferences = Preference.orderedForUI.take(32),  // First 32 preferences (exclude LOW_PRICE)
                    user1Prefs = user1Prefs,
                    user2Prefs = user2Prefs,
                    isUser2Active = isUser2Active.value,
                    onTogglePref = { pref ->
                        if (isUser2Active.value) {
                            sharedViewModel.toggleUser2Pref(pref)
                        } else {
                            sharedViewModel.toggleUser1Pref(pref)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(16f)  // 16 rows for 32 items
                )

                // Row 17: Low price button and user toggle button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Low price button
                    val isLowPriceSelected = if (isUser2Active.value)
                        user2Prefs.contains(Preference.LOW_PRICE)
                    else
                        user1Prefs.contains(Preference.LOW_PRICE)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isLowPriceSelected) selectedGrey else dietprefsGrey,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                showPriceDialog = true
                            }
                            .padding(start = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "low price",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    // User toggle button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isUser2Active.value) selectedGrey else dietprefsGrey,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable(enabled = user1Prefs.isNotEmpty() || user2Prefs.isNotEmpty() || isUser2Active.value) {
                                if (user1Prefs.isEmpty() && user2Prefs.isEmpty()) {
                                    isUser2Active.value = false
                                } else {
                                    isUser2Active.value = !isUser2Active.value
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Person",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Price Selection Dialog
    PriceSelectionDialog(
        showDialog = showPriceDialog,
        onDismiss = { showPriceDialog = false },
        isUser2Active = isUser2Active.value,
        user1MaxPrice = user1MaxPrice,
        user2MaxPrice = user2MaxPrice,
        onSetPrice = { price ->
            if (isUser2Active.value) {
                sharedViewModel.setUser2MaxPrice(price)
            } else {
                sharedViewModel.setUser1MaxPrice(price)
            }
        },
        onClearPrice = {
            if (isUser2Active.value) {
                sharedViewModel.setUser2MaxPrice(null)
            } else {
                sharedViewModel.setUser1MaxPrice(null)
            }
        },
        priceOptions = priceOptions
    )
}

@Composable
fun PreferencesTopBar(
    user1Prefs: Set<Preference>,
    user2Prefs: Set<Preference>,
    user1MaxPrice: Float?,
    user2MaxPrice: Float?,
    onSettingsClick: () -> Unit,
    onUserModeClick: () -> Unit
) {
    // Build display text from preferences
    // LOW_PRICE preference is formatted as "under $X" using the price value
    val user1DisplayText = user1Prefs
        .map { if (it == Preference.LOW_PRICE) "under $${"%.0f".format(user1MaxPrice ?: 0)}" else it.display }
        .joinToString(", ")
    val user2DisplayText = user2Prefs
        .map { if (it == Preference.LOW_PRICE) "under $${"%.0f".format(user2MaxPrice ?: 0)}" else it.display }
        .joinToString(", ")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .background(dietprefsGrey)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (user1DisplayText.isEmpty() && user2DisplayText.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .alpha(0f),
                    tint = Color.Transparent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Preferences",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = user1Red,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.85f)
                    .padding(start = 16.dp), // Match SearchResultsScreen positioning
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (user1DisplayText.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User 1",
                                tint = user1Red
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user1DisplayText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = user1Red,
                            maxLines = if (user2DisplayText.isEmpty()) 4 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (user2DisplayText.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User 2",
                                tint = user2Magenta
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user2DisplayText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = user2Magenta,
                            maxLines = if (user1DisplayText.isEmpty()) 4 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
