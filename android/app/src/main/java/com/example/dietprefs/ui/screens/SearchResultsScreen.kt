package com.example.dietprefs.ui.screens // Assuming a package structure

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dietprefs.model.Preference
import com.example.dietprefs.model.SortColumn
import com.example.dietprefs.model.SortDirection
import com.example.dietprefs.ui.navigation.Screen
import com.example.dietprefs.ui.theme.backgroundGrey
import com.example.dietprefs.ui.theme.dietprefsGrey
import com.example.dietprefs.ui.theme.selectedGrey
import com.example.dietprefs.ui.theme.selectedTeal
import com.example.dietprefs.ui.theme.user1Red
import com.example.dietprefs.ui.theme.user2Magenta
import com.example.dietprefs.ui.components.FilterButton
import com.example.dietprefs.ui.components.TopBar
import com.example.dietprefs.ui.components.VendorListItem
import com.example.dietprefs.ui.components.VendorSearchBar
import com.example.dietprefs.ui.components.table.SortableHeader
import com.example.dietprefs.viewmodel.SharedViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchResultsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onSettingsClick: () -> Unit // From your existing SearchResultsTopBar
) {
    // --- State Observation ---
    val pagedVendors by sharedViewModel.pagedVendors.collectAsState()
    val totalResults by sharedViewModel.totalResultsCount.collectAsState()
    val visibleRange by sharedViewModel.visibleRange.collectAsState()
    val sortState by sharedViewModel.sortState.collectAsState()

    val user1Prefs by sharedViewModel.user1Prefs.collectAsState()
    val user2Prefs by sharedViewModel.user2Prefs.collectAsState()
    val user1Display by sharedViewModel.user1Display.collectAsState()
    val user2Display by sharedViewModel.user2Display.collectAsState()
    val isLoading by sharedViewModel.isLoading.collectAsState()
    val searchQuery by sharedViewModel.searchQuery.collectAsState()
    val errorMessage by sharedViewModel.errorMessage.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Track selected filters (from ViewModel)
    val selectedFilters by sharedViewModel.vendorFilters.collectAsState()

    // Helper function to toggle filter selection
    fun toggleFilter(filterName: String) {
        sharedViewModel.toggleVendorFilter(filterName)
    }

    // Determine user mode for results display
    val isTwoUserMode = user1Prefs.isNotEmpty() && user2Prefs.isNotEmpty()

    // --- Effects ---
    // Debounced search - trigger backend search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank() && user1Prefs.isEmpty() && user2Prefs.isEmpty()) {
            // Skip search if no query and no preferences
            return@LaunchedEffect
        }
        // Debounce: wait 300ms before triggering search
        delay(300)
        sharedViewModel.searchVendors()
    }

    // Trigger search when vendor filters change
    LaunchedEffect(selectedFilters) {
        sharedViewModel.searchVendors()
    }

    // Scroll to top when sort state changes
    LaunchedEffect(sortState) {
        listState.scrollToItem(0)
    }

    // Update visible range based on LazyListState
    LaunchedEffect(listState.firstVisibleItemIndex, pagedVendors.size) {
        if (pagedVendors.isNotEmpty()) {
            val start = listState.firstVisibleItemIndex + 1
            val end = (start + listState.layoutInfo.visibleItemsInfo.size - 1)
                .coerceAtMost(pagedVendors.size)
            sharedViewModel.updateVisibleRange(start, end)
        } else {
            sharedViewModel.updateVisibleRange(0, 0)
        }
    }

    // Initial load of results (assuming context is available here or passed)
    // This might be better triggered from where you navigate to this screen.
    // val context = LocalContext.current // If you need context here
    // LaunchedEffect(Unit) {
    //     sharedViewModel.loadAndComputeResults(AppDatabase.getDatabase(context))
    // }

    Scaffold(
        topBar = {
            TopBar(
                user1Display = user1Display,
                user2Display = user2Display,
                onBackClick = { navController.popBackStack() },
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGrey)
                .padding(innerPadding)
        ) {
            // Visual separator between top bar and table header
            HorizontalDivider(
                thickness = 4.dp,
                color = backgroundGrey
            )
            
            // --- Sortable Table Header ---
            if (isTwoUserMode) {
                // Two-user mode: Two rows with explicit alignment
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(dietprefsGrey)
                        .padding(vertical = 8.dp)
                ) {
                    // First row: Main headers aligned
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Vendor Header
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .clickable { sharedViewModel.updateSortState(SortColumn.VENDOR_RATING) }
                        ) {
                            SortableHeader(
                                text = "Vendors",
                                column = SortColumn.VENDOR_RATING,
                                currentSortState = sortState,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        // Distance Header
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { sharedViewModel.updateSortState(SortColumn.DISTANCE) },
                            contentAlignment = Alignment.Center
                        ) {
                            SortableHeader(
                                text = "Distance",
                                column = SortColumn.DISTANCE,
                                currentSortState = sortState,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Menu Items Header
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { sharedViewModel.updateSortState(SortColumn.MENU_ITEMS) },
                            contentAlignment = Alignment.Center
                        ) {
                            SortableHeader(
                                text = "Menu Items",
                                column = SortColumn.MENU_ITEMS,
                                currentSortState = sortState,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Second row: Result count aligned with person icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Result count text
                        Box(
                            modifier = Modifier.weight(2f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (totalResults > 0) {
                                Text(
                                    text = "${visibleRange.first}–${visibleRange.second} of $totalResults",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            } else {
                                Text(
                                    text = if (searchQuery.isNotBlank() || user1Prefs.isNotEmpty() || user2Prefs.isNotEmpty()) "0 results" else "Loading...",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        }

                        // Empty space for distance column
                        Spacer(modifier = Modifier.weight(1f))

                        // Person icons
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Row {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "User 1 Items",
                                    tint = user1Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "User 2 Items",
                                    tint = user2Magenta,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Single-user mode: All on same baseline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(dietprefsGrey)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vendor Header with result count on same line
                    Row(
                        modifier = Modifier
                            .weight(2f)
                            .clickable { sharedViewModel.updateSortState(SortColumn.VENDOR_RATING) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SortableHeader(
                            text = "Vendor",
                            column = SortColumn.VENDOR_RATING,
                            currentSortState = sortState,
                            modifier = Modifier.padding(start = 16.dp)
                        )

                        if (totalResults > 0) {
                            Text(
                                text = "${visibleRange.first}–${visibleRange.second} of $totalResults",
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        } else {
                            Text(
                                text = if (searchQuery.isNotBlank() || user1Prefs.isNotEmpty() || user2Prefs.isNotEmpty()) "0 results" else "Loading...",
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(end = 16.dp)
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
                            text = "Dist",
                            column = SortColumn.DISTANCE,
                            currentSortState = sortState,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Menu Items Header
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { sharedViewModel.updateSortState(SortColumn.MENU_ITEMS) },
                        contentAlignment = Alignment.Center
                    ) {
                        SortableHeader(
                            text = "Menu Items",
                            column = SortColumn.MENU_ITEMS,
                            currentSortState = sortState,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            HorizontalDivider(
                thickness = 2.dp,
                color = backgroundGrey
            )

            // --- Results List or Empty State ---
            Box(modifier = Modifier.weight(1f)) {
                if (pagedVendors.isEmpty()) {
                    // Empty state - but keep structure visible
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else if (errorMessage != null) {
                            Text(
                                "Error: $errorMessage",
                                textAlign = TextAlign.Center,
                                color = Color.Red
                            )
                        } else {
                            Text(
                                "No vendors match the selected preferences.",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                    itemsIndexed(
                        items = pagedVendors,
                        key = { _, vendor -> vendor.vendorName + vendor.querySpecificRatingString } // More unique key
                    ) { index, vendor ->
                        VendorListItem(
                            vendor = vendor,
                            isTwoUserMode = isTwoUserMode,
                            onClick = {
                                sharedViewModel.selectVendorByName(vendor.vendorName)
                                navController.navigate(Screen.RestaurantDetail.route)
                            }
                        )
                        HorizontalDivider(
                            thickness = 2.dp,
                            color = backgroundGrey
                        )

                        // Pagination Trigger
                        if (index >= pagedVendors.size - 3 && pagedVendors.size < totalResults) {
                            LaunchedEffect(pagedVendors.size) { // Re-key on list size
                                sharedViewModel.loadNextPage()
                            }
                        }
                    }
                }
                }
            }

            // --- Search Bar ---
            VendorSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { sharedViewModel.setSearchQuery(it) }
            )

            // --- Filter Buttons ---
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(dietprefsGrey)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 6.dp)
            ) {
                // Row 1: Delivery, Open, USA, Europe, North Africa/Middle East
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CompactFilterButton("Delivery", "delivery" in selectedFilters) { toggleFilter("delivery") }
                    CompactFilterButton("Open", "open" in selectedFilters) { toggleFilter("open") }
                    CompactFilterButton("USA", "usa" in selectedFilters) { toggleFilter("usa") } // TODO: Replace with icon
                    CompactFilterButton("Europe", "europe" in selectedFilters) { toggleFilter("europe") } // TODO: Replace with icon
                    CompactFilterButton("N Afr/ME", "north_africa_middle_east" in selectedFilters) { toggleFilter("north_africa_middle_east") } // TODO: Replace with icon
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Takeout, Fusion, Mexico & South, Sub Saharan Africa, East Asia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CompactFilterButton("Takeout", "takeout" in selectedFilters) { toggleFilter("takeout") }
                    CompactFilterButton("Fusion", "fusion" in selectedFilters) { toggleFilter("fusion") }
                    CompactFilterButton("Mex & SA", "mexico_south_america" in selectedFilters) { toggleFilter("mexico_south_america") } // TODO: Replace with icon
                    CompactFilterButton("Sub Sah", "sub_saharan_africa" in selectedFilters) { toggleFilter("sub_saharan_africa") } // TODO: Replace with icon
                    CompactFilterButton("E Asia", "east_asia" in selectedFilters) { toggleFilter("east_asia") } // TODO: Replace with icon
                }
            }
        }
    }
}

/**
 * Compact, fixed-size filter button for icon-like appearance.
 * Text is truncated to fit within fixed width.
 */
@Composable
private fun CompactFilterButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .size(width = 64.dp, height = 40.dp)
            .shadow(4.dp, RoundedCornerShape(4.dp)),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        border = androidx.compose.foundation.BorderStroke(
            3.dp,
            if (isSelected) selectedTeal else Color.White.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = if (isSelected) selectedTeal.copy(alpha = 0.3f) else backgroundGrey
        )
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}