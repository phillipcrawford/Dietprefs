package com.example.dietprefs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dietprefs.ui.components.TopBar
import com.example.dietprefs.ui.theme.backgroundGrey
import com.example.dietprefs.ui.theme.restaurantSelectedGold
import com.example.dietprefs.ui.theme.restaurantDeselectedGold
import com.example.dietprefs.viewmodel.SharedViewModel

/**
 * Dimension constants for RestaurantDetailScreen layout.
 * These values create a fixed-height viewport that shows the restaurant header
 * plus ~3 menu items at a time, with snap scrolling behavior.
 */
private object RestaurantDetailDimensions {
    /** Menu item row height - matches SearchResultsScreen vendor height */
    val MENU_ITEM_HEIGHT = 48.dp

    /** Restaurant header height (1.25x standard item height) */
    val RESTAURANT_HEADER_HEIGHT = 60.dp

    /** Photo carousel height for menu item images */
    val PHOTO_CAROUSEL_HEIGHT = 300.dp

    /**
     * Viewport height: shows restaurant header + 3 menu items at once
     * Calculation: 60dp (header) + 48dp * 3 (items) = 204dp
     */
    val VIEWPORT_HEIGHT = 204.dp

    /**
     * Bottom padding to allow all items to scroll to top position.
     * Calculation: 204dp (viewport) - 48dp (item height) = 156dp
     * This ensures firstVisibleItemIndex can increment through all items.
     */
    val BOTTOM_PADDING = 156.dp
}

@Composable
fun RestaurantDetailScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onSettingsClick: () -> Unit
) {
    val user1Prefs by sharedViewModel.user1Prefs.collectAsState()
    val user2Prefs by sharedViewModel.user2Prefs.collectAsState()
    val user1Display by sharedViewModel.user1Display.collectAsState()
    val user2Display by sharedViewModel.user2Display.collectAsState()
    val selectedVendor by sharedViewModel.selectedVendor.collectAsState()
    val menuItems by sharedViewModel.menuItems.collectAsState()
    val isLoadingItems by sharedViewModel.isLoadingItems.collectAsState()
    val selectedIndex by sharedViewModel.selectedItemIndex.collectAsState()

    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Fetch menu items when screen loads
    LaunchedEffect(selectedVendor) {
        selectedVendor?.let { vendor ->
            sharedViewModel.fetchMenuItems(vendor.id)
        }
    }

    // Select second visible item from top (more centered in viewport)
    LaunchedEffect(listState.firstVisibleItemIndex, listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val secondVisibleIndex = listState.firstVisibleItemIndex + 1
            val maxIndex = menuItems.size // 0 is restaurant, menuItems.size is last item
            val clampedIndex = secondVisibleIndex.coerceIn(0, maxIndex)
            sharedViewModel.updateSelectedItemIndex(clampedIndex)
        }
    }

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
                .padding(innerPadding)
                .background(backgroundGrey)
                .padding(top = 4.dp)
        ) {
            when {
                selectedVendor == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No restaurant selected",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
                isLoadingItems -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    // Scrollable stack (restaurant header + menu items)
                    LazyColumn(
                        state = listState,
                        flingBehavior = snapFlingBehavior,
                        userScrollEnabled = true,
                        contentPadding = PaddingValues(bottom = RestaurantDetailDimensions.BOTTOM_PADDING),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(RestaurantDetailDimensions.VIEWPORT_HEIGHT)
                    ) {
                        // Item 0: Restaurant header
                        item {
                            RestaurantHeaderItem(
                                vendor = selectedVendor!!,
                                isSelected = selectedIndex == 0,
                                onClick = { sharedViewModel.updateSelectedItemIndex(0) }
                            )
                        }

                        // Items 1-N: Menu items
                        itemsIndexed(menuItems) { index, item ->
                            val itemIndex = index + 1 // +1 because restaurant is at 0
                            MenuItemRow(
                                item = item,
                                position = index + 1,
                                totalItems = menuItems.size,
                                isSelected = selectedIndex == itemIndex,
                                onClick = { sharedViewModel.updateSelectedItemIndex(itemIndex) }
                            )
                        }
                    }

                    // Dynamic content area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (selectedIndex == 0) {
                            // Restaurant info panel
                            RestaurantInfoPanel(vendor = selectedVendor!!)
                        } else {
                            // Menu item photos and voting
                            val selectedMenuItem = menuItems.getOrNull(selectedIndex - 1)
                            selectedMenuItem?.let { item ->
                                PhotoCarousel(
                                    photos = item.pictures?.split(",")?.map { it.trim() } ?: emptyList(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(RestaurantDetailDimensions.PHOTO_CAROUSEL_HEIGHT)
                                )

                                VotingUI(
                                    itemId = item.id,
                                    onVote = { voteType ->
                                        sharedViewModel.voteOnItem(item.id, voteType)
                                    }
                                )
                            }
                        }

                        // External links grid (always visible)
                        ExternalLinksGrid(
                            vendor = selectedVendor!!,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantHeaderItem(
    vendor: com.example.dietprefs.network.models.VendorResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) restaurantSelectedGold else restaurantDeselectedGold

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(RestaurantDetailDimensions.RESTAURANT_HEADER_HEIGHT)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = vendor.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun MenuItemRow(
    item: com.example.dietprefs.network.models.ItemResponse,
    position: Int,
    totalItems: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF90EE90) else Color(0xFF228B22) // Light green vs dark green

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RestaurantDetailDimensions.MENU_ITEM_HEIGHT)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Item name
        Text(
            text = item.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        // Position counter
        Text(
            text = "$position of $totalItems",
            fontSize = 14.sp,
            color = Color.White
        )

        // Price
        Text(
            text = item.price?.let { "$${String.format("%.2f", it)}" } ?: "",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun PhotoCarousel(
    photos: List<String>,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) {
        Box(
            modifier = modifier.background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No photos available",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { photos.size })

        HorizontalPager(
            state = pagerState,
            modifier = modifier
        ) { page ->
            AsyncImage(
                model = photos[page],
                contentDescription = "Menu item photo ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun VotingUI(
    itemId: Int,
    onVote: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onVote("up") },
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = "Thumbs Up",
                tint = Color.Green,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        IconButton(
            onClick = { onVote("down") },
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ThumbDown,
                contentDescription = "Thumbs Down",
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun ExternalLinksGrid(
    vendor: com.example.dietprefs.network.models.VendorResponse,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        Text(
            text = "Order & Reviews",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Delivery platforms row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (vendor.deliveryOptions.grubhub) LinkChip("Grubhub")
            if (vendor.deliveryOptions.ubereats) LinkChip("Ubereats")
            if (vendor.deliveryOptions.doordash) LinkChip("Doordash")
            if (vendor.deliveryOptions.postmates) LinkChip("Postmates")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Review sites row (placeholders)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinkChip("Yelp")
            LinkChip("Google")
            LinkChip("Tripadvisor")
        }
    }
}

@Composable
fun LinkChip(label: String) {
    Box(
        modifier = Modifier
            .background(Color.Gray, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
fun RestaurantInfoPanel(
    vendor: com.example.dietprefs.network.models.VendorResponse
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundGrey)
            .padding(16.dp)
    ) {
        Text(
            text = "Restaurant Information",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Address
        vendor.address?.let {
            InfoRow(label = "Address", value = it)
        }

        // Phone
        vendor.phone?.let {
            InfoRow(label = "Phone", value = it)
        }

        // Rating
        InfoRow(
            label = "Rating",
            value = "${vendor.rating.upvotes}/${vendor.rating.totalVotes} (${(vendor.rating.percentage * 100).toInt()}%)"
        )

        // Hours placeholder
        Text(
            text = "Hours",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Text(
            text = vendor.hours ?: "Hours not available",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}
