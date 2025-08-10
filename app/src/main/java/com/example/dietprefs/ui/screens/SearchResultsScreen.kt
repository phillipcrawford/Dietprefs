package com.example.dietprefs.ui.screens // Original package

import android.util.Log // For logging
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button // For FilterButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector // For PreferenceDisplayRow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow // For PreferenceDisplayRow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dietprefs.data.AppDatabase
import com.example.dietprefs.model.Preference
import com.example.dietprefs.model.SortColumn
import com.example.dietprefs.model.SortDirection // Corrected: was com.example.dietprefs.model.SortState
import com.example.dietprefs.model.SortState // Added for SortableHeader parameter type
import com.example.dietprefs.ui.theme.dietprefsGrey
import com.example.dietprefs.ui.theme.user1Red
import com.example.dietprefs.ui.theme.user2Magenta
import com.example.dietprefs.viewmodel.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchResultsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onSettingsClick: () -> Unit
) {
    // --- State Observation ---
    val pagedVendors by sharedViewModel.pagedVendors.collectAsState()
    val totalResults by sharedViewModel.totalResultsCount.collectAsState()
    val visibleRange by sharedViewModel.visibleRange.collectAsState() // From original context
    val sortState by sharedViewModel.sortState.collectAsState()
    val user1Prefs by sharedViewModel.user1Prefs.collectAsState()
    val user2Prefs by sharedViewModel.user2Prefs.collectAsState()

    // NEW: Observe isLoading from ViewModel
    val isLoadingFromViewModel by sharedViewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    val isTwoUserMode = user1Prefs.isNotEmpty() && user2Prefs.isNotEmpty()

    // --- Effects ---
    // Update visible range based on LazyListState (from original context)
    LaunchedEffect(listState.firstVisibleItemIndex, pagedVendors.size, totalResults) {
        if (pagedVendors.isNotEmpty()) {
            val start = listState.firstVisibleItemIndex + 1
            // Ensure end does not exceed pagedVendors.size if it's less than totalResults (e.g., during initial load/pagination)
            // but can go up to totalResults if pagedVendors is the full list after pagination completes
            val currentListSize = pagedVendors.size
            val end = (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size)
                .coerceAtMost(currentListSize) // Cannot exceed current displayed items
            if (start <= end) { // Ensure valid range
                sharedViewModel.updateVisibleRange(start, end)
            } else if (currentListSize > 0) { // If start > end but list is not empty (e.g. scrolled past end)
                sharedViewModel.updateVisibleRange(currentListSize, currentListSize)
            } else { // List is empty
                sharedViewModel.updateVisibleRange(0, 0)
            }
        } else {
            sharedViewModel.updateVisibleRange(0, 0)
        }
    }


    val context = LocalContext.current
    LaunchedEffect(user1Prefs, user2Prefs) {
        Log.d("SearchResultsScreen", "LaunchedEffect (prefs changed): Calling loadAndComputeResults. U1: ${user1Prefs.joinToString { it.name }}, U2: ${user2Prefs.joinToString { it.name }}")
        sharedViewModel.loadAndComputeResults(AppDatabase.getDatabase(context))
    }
    // Optional: Initial load if preferences might be set before reaching this screen,
    // or if you want to load even with no preferences.
    // Ensure it doesn't conflict with the preference-triggered load.
    LaunchedEffect(Unit) {
        if (pagedVendors.isEmpty() && !isLoadingFromViewModel && (user1Prefs.isNotEmpty() || user2Prefs.isNotEmpty())) {
            Log.d("SearchResultsScreen", "LaunchedEffect (Initial with Prefs): Calling loadAndComputeResults.")
            sharedViewModel.loadAndComputeResults(AppDatabase.getDatabase(context))
        } else if (pagedVendors.isEmpty() && !isLoadingFromViewModel && user1Prefs.isEmpty() && user2Prefs.isEmpty()) {
            // If you want to load *something* even with no prefs, do it here.
            // Otherwise, it will wait for prefs to be selected.
            // Log.d("SearchResultsScreen", "LaunchedEffect (Initial no Prefs): No prefs selected, load not triggered by default.")
        }
    }

    Scaffold(
        topBar = {
            SearchResultsTopBar(
                user1Prefs = user1Prefs,
                user2Prefs = user2Prefs,
                navController = navController,
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // --- Sortable Table Header (from original context) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(dietprefsGrey)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .defaultMinSize(minHeight = if (isTwoUserMode) 56.dp else Dp.Unspecified),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vendor Header
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .clickable { sharedViewModel.updateSortState(SortColumn.VENDOR_RATING) }
                ) {
                    SortableHeader(
                        text = "Vendor",
                        column = SortColumn.VENDOR_RATING,
                        currentSortState = sortState,
                        onClick = { /* Click handled by Column */ }
                    )
                    if (totalResults > 0) {
                        Text(
                            text = "${visibleRange.first}–${visibleRange.second} of $totalResults",
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .align(Alignment.End)
                        )
                    } else {
                        Text(
                            text = if (isLoadingFromViewModel) "Loading..."
                            else if (searchQuery.isNotBlank() || user1Prefs.isNotEmpty() || user2Prefs.isNotEmpty()) "0 results"
                            else "Select preferences", // Default message before any action
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .align(Alignment.End)
                        )
                    }
                }
                // Distance Header
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { sharedViewModel.updateSortState(SortColumn.DISTANCE) },
                    contentAlignment = Alignment.Center
                ) {
                    SortableHeader(
                        text = "Diset",
                        column = SortColumn.DISTANCE,
                        currentSortState = sortState,
                        onClick = { /* Click handled by Box */ },
                        textAlign = TextAlign.Center
                    )
                }
                // Menu Items Header
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { sharedViewModel.updateSortState(SortColumn.MENU_ITEMS) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SortableHeader(
                        text = "Menu Items",
                        column = SortColumn.MENU_ITEMS,
                        currentSortState = sortState,
                        onClick = { /* Click handled by Column */ },
                        textAlign = TextAlign.Center
                    )
                    if (isTwoUserMode) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "User 1 Items",
                                tint = user1Red,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "User 2 Items",
                                tint = user2Magenta,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            Divider()

            // --- Results List or Empty/Loading State ---
            val filteredVendors = remember(pagedVendors, searchQuery) {
                pagedVendors.filter { it.vendorName.contains(searchQuery, ignoreCase = true) }
            }

            if (isLoadingFromViewModel && filteredVendors.isEmpty() && searchQuery.isBlank()) {
                // Show general loading indicator ONLY if actively loading, list is empty, AND not due to an active search query filtering to empty
                Log.d("SearchResultsScreen", "UI: isLoadingFromViewModel is true and filteredVendors is empty (no search query). Showing Centered CircularProgressIndicator.")
                Box(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredVendors.isEmpty()) {
                // This covers:
                // 1. No results match preferences (and not loading).
                // 2. No results match search query (and not loading).
                // 3. Initial state before preferences are selected (and not loading).
                Log.d("SearchResultsScreen", "UI: filteredVendors is empty. isLoading: $isLoadingFromViewModel. Determining empty state message.")
                Box(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = when {
                            isLoadingFromViewModel -> "Loading matching vendors..." // Should be less common here due to outer if
                            user1Prefs.isEmpty() && user2Prefs.isEmpty() && searchQuery.isBlank() -> "Please select preferences to see results."
                            searchQuery.isNotBlank() -> "No vendors match your search '$searchQuery'."
                            else -> "No vendors match the selected preferences."
                        },
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Log.d("SearchResultsScreen", "UI: Displaying LazyColumn with ${filteredVendors.size} filtered vendors.")
                LazyColumn(modifier = Modifier.weight(1f), state = listState) {
                    itemsIndexed(
                        items = filteredVendors,
                        key = { _, vendor -> vendor.vendorName + vendor.querySpecificRatingString } // Using original key from context
                    ) { index, vendor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = vendor.vendorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.align(Alignment.TopStart)
                                )
                                Text(
                                    text = "Rating: ${vendor.querySpecificRatingString}",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                            Text(
                                String.format("%.1f mi", vendor.distanceMiles),
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isTwoUserMode) {
                                    Text(
                                        text = "${vendor.user1Count} | ${vendor.user2Count}",
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "${vendor.combinedRelevantItemCount}",
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        Divider()

                        // Pagination Trigger
                        if (index >= filteredVendors.size - 3 && filteredVendors.size < totalResults && !isLoadingFromViewModel) {
                            LaunchedEffect(filteredVendors.size, totalResults) { // Re-key on list size and totalResults
                                Log.d("SearchResultsScreen", "Pagination Triggered. Index: $index, FilteredSize: ${filteredVendors.size}, Total: $totalResults")
                                sharedViewModel.loadNextPage()
                            }
                        }
                    }
                }
            }

            // --- Search Bar (from original context) ---
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search vendors...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    disabledContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // --- Filter Buttons (from original context) ---
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 4.dp)) {
                Text("Filter by (Not Implemented):", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
                for (row in 0 until 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until 5) {
                            val filterName = "Type ${(row * 5) + col + 1}"
                            FilterButton(label = filterName, onClick = { /* TODO */ })
                        }
                    }
                    if (row < 1) Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// --- Helper Composables from original context [1] ---

@Composable
fun SearchResultsTopBar(
    user1Prefs: Set<Preference>,
    user2Prefs: Set<Preference>,
    navController: NavController,
    onSettingsClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isBackEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .background(dietprefsGrey)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        IconButton(
            onClick = {
                if (isBackEnabled) {
                    isBackEnabled = false
                    navController.popBackStack()
                    coroutineScope.launch {
                        delay(300)
                        isBackEnabled = true
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start // As per original
        ) {
            if (user1Prefs.isNotEmpty()) {
                PreferenceDisplayRow(prefs = user1Prefs, userColor = user1Red, icon = Icons.Default.Person, maxLines = if (user2Prefs.isEmpty()) 3 else 2)
            }
            if (user2Prefs.isNotEmpty()) {
                PreferenceDisplayRow(prefs = user2Prefs, userColor = user2Magenta, icon = Icons.Default.Person, maxLines = if (user1Prefs.isEmpty()) 3 else 2)
            }
        }

        IconButton(onClick = onSettingsClick, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun PreferenceDisplayRow( // Marked private as in original
    prefs: Set<Preference>,
    userColor: Color,
    icon: ImageVector, // androidx.compose.ui.graphics.vector.ImageVector
    maxLines: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = "User Preferences",
            tint = userColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            prefs.joinToString(", ") { it.display },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = userColor,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis // androidx.compose.ui.text.style.TextOverflow
        )
    }
}

@Composable
fun SortableHeader(
    text: String,
    column: SortColumn,
    currentSortState: SortState, // Changed from com.example.helloworldapp.model.SortState to SortState (assuming import)
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick), // Added clickable here as parent column/box handles it
        verticalAlignment = Alignment.CenterVertically,
        // Original logic for horizontalArrangement
        horizontalArrangement = if (textAlign == TextAlign.Center) Arrangement.Center else Arrangement.Start // Original was SpaceBetween, but Start might be better if icon is always present (even if spacer)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
            textAlign = textAlign ?: TextAlign.Start
        )
        Spacer(Modifier.width(4.dp))
        if (currentSortState.column == column) {
            Icon(
                imageVector = if (currentSortState.direction == SortDirection.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = "Sort Direction: ${currentSortState.direction}",
                tint = Color.White, // Added tint for visibility
                modifier = Modifier.size(16.dp)
            )
        } else {
            Spacer(Modifier.size(16.dp)) // Keep spacer for alignment
        }
    }
}

@Composable
fun FilterButton(label: String, onClick: () -> Unit) { // Restored to original styled version
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 2.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), // Example styling, adjust as needed or use your original
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondary) // Example styling
    }
}