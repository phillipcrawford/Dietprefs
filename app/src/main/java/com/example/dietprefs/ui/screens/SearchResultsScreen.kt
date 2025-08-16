package com.example.dietprefs.ui.screens // Assuming a package structure

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.dietprefs.ui.theme.backgroundGrey
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
    onSettingsClick: () -> Unit // From your existing SearchResultsTopBar
) {
    // --- State Observation ---
    val pagedVendors by sharedViewModel.pagedVendors.collectAsState()
    val totalResults by sharedViewModel.totalResultsCount.collectAsState()
    val visibleRange by sharedViewModel.visibleRange.collectAsState()
    val sortState by sharedViewModel.sortState.collectAsState()

    val user1Prefs by sharedViewModel.user1Prefs.collectAsState()
    val user2Prefs by sharedViewModel.user2Prefs.collectAsState()
    val isLoading by sharedViewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") } // Local search query

    // Determine user mode for results display
    val isTwoUserMode = user1Prefs.isNotEmpty() && user2Prefs.isNotEmpty()

    // --- Effects ---
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
            SearchResultsTopBar( // Using your existing TopBar
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
            // Visual separator between top bar and table header
            HorizontalDivider(
                thickness = 4.dp,
                color = backgroundGrey
            )
            
            // --- Sortable Table Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(dietprefsGrey) // Light grey background for header
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .defaultMinSize(minHeight = if (isTwoUserMode) 56.dp else Dp.Unspecified),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vendor Header (includes rating string and result count)
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .clickable { sharedViewModel.updateSortState(SortColumn.VENDOR_RATING) }
                ) {
                    SortableHeader(
                        text = "Vendhrhgor",
                        column = SortColumn.VENDOR_RATING,
                        currentSortState = sortState
                    )
                    if (totalResults > 0) {
                        Text(
                            text = "${visibleRange.first}–${visibleRange.second} of $totalResults",
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp, // Slightly smaller
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .align(Alignment.End)
                        )
                    } else {
                        Text( // Placeholder if no results yet, or show "0 results"
                            text = if (searchQuery.isNotBlank() || user1Prefs.isNotEmpty() || user2Prefs.isNotEmpty()) "0 results" else "Loading...",
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .align(Alignment.End)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { sharedViewModel.updateSortState(SortColumn.DISTANCE) },
                    //.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    SortableHeader(
                        text = "Dist",
                        column = SortColumn.DISTANCE,
                        currentSortState = sortState,
                        textAlign = TextAlign.Center
                    )
                }

                // Menu Items Header (adapts to user mode)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { sharedViewModel.updateSortState(SortColumn.MENU_ITEMS) },
                    //.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SortableHeader(
                        text = "Menu Items",
                        column = SortColumn.MENU_ITEMS,
                        currentSortState = sortState,
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
            HorizontalDivider()

            // --- Results List or Empty/Loading State ---
            if (isLoading && searchQuery.isBlank()) { // Show loading only if not actively searching an empty list
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (pagedVendors.filter { it.vendorName.contains(searchQuery, ignoreCase = true) }.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No vendors match the selected preferences.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), state = listState) {
                    itemsIndexed(
                        items = pagedVendors.filter {
                            it.vendorName.contains(searchQuery, ignoreCase = true)
                        },
                        key = { _, vendor -> vendor.vendorName + vendor.querySpecificRatingString } // More unique key
                    ) { index, vendor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp), // Slightly reduced vertical padding
                            verticalAlignment = Alignment.Top // Align to top for multi-line text
                        ) {
                            // Vendor Name and Rating
                            Box(
                                modifier = Modifier
                                    .weight(2f) // This Box takes up 2/4 of the available width
                                    .padding(end = 8.dp)
                                // .height(IntrinsicSize.Min) // Optional: If you need to ensure Box wraps content height
                            ) {
                                // Vendor Name - Aligned to TopStart (default for Box content, but explicit is fine)
                                Text(
                                    text = vendor.vendorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.align(Alignment.TopStart)
                                )

                                // Rating String - Aligned to TopEnd (Top Right)
                                Text(
                                    text = "Rating: ${vendor.querySpecificRatingString}",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.TopEnd) // <<< ALIGN TO TOP END
                                )
                            }

                            // Distance
                            Text(
                                String.format("%.1f mi", vendor.distanceMiles),
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )

                            // Menu Item Counts (adapts to user mode)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally // Center the text content
                            ) {
                                if (isTwoUserMode) {
                                    Text(
                                        text = "${vendor.user1Count} | ${vendor.user2Count}",
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                } else { // Single user mode or no users with preferences
                                    Text(
                                        text = "${vendor.combinedRelevantItemCount}",
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        HorizontalDivider()

                        // Pagination Trigger
                        if (index >= pagedVendors.size - 3 && pagedVendors.size < totalResults) {
                            LaunchedEffect(pagedVendors.size) { // Re-key on list size
                                sharedViewModel.loadNextPage()
                            }
                        }
                    }
                }
            }

            // --- Search Bar (from your existing code) ---
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search vendors...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors( // Assuming M3 TextFieldColors
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    disabledContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // --- Filter Buttons (from your existing code, simplified) ---
            // This part is kept as you had it, functionality for these buttons is separate.
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
                    if (row < 1) Spacer(modifier = Modifier.height(8.dp)) // Spacer only between rows
                }
            }
        }
    }
}

@Composable
fun SearchResultsTopBar( // Your existing TopBar, slightly adapted for imports
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
            .height(128.dp) // Consider adjusting height or making it dynamic
            .background(dietprefsGrey)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        IconButton(
            onClick = {
                if (isBackEnabled) {
                    isBackEnabled = false // Prevent double taps
                    navController.popBackStack()
                    coroutineScope.launch { // Add delay for smoother transition if needed
                        delay(300) // Optional delay
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
                .fillMaxWidth(0.7f), // Adjusted width
            verticalArrangement = Arrangement.spacedBy(2.dp), // Reduced spacing
            horizontalAlignment = Alignment.Start
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
private fun PreferenceDisplayRow(
    prefs: Set<Preference>,
    userColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    maxLines: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = "User Preferences",
            tint = userColor,
            modifier = Modifier.size(18.dp) // Adjusted size
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            prefs.joinToString(", ") { it.display },
            fontSize = 15.sp, // Adjusted size
            fontWeight = FontWeight.Medium, // Adjusted weight
            color = userColor,
            maxLines = maxLines,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis // Handle overflow
        )
    }
}


@Composable
fun SortableHeader(
    text: String,
    column: SortColumn,
    currentSortState: com.example.dietprefs.model.SortState,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp), // Added some horizontal padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (textAlign == TextAlign.Center) Arrangement.Center else Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold, // Header text is bold
            fontSize = 14.sp,             // Adjusted font size
            color = Color.White,
            textAlign = textAlign ?: TextAlign.Start // Allow specifying text alignment
        )
        Spacer(Modifier.width(4.dp)) // Space between text and icon
        if (currentSortState.column == column) {
            Icon(
                imageVector = if (currentSortState.direction == SortDirection.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = "Sort Direction: ${currentSortState.direction}",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else {
            // Optional: Add a transparent spacer to keep alignment consistent when icon is not visible
            Spacer(Modifier.size(16.dp))
        }
    }
}

@Composable
fun FilterButton(label: String, onClick: () -> Unit) { // Added onClick
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 2.dp), // Reduced padding
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp) // Smaller button
    ) {
        Text(label, fontSize = 12.sp) // Smaller text
    }
}