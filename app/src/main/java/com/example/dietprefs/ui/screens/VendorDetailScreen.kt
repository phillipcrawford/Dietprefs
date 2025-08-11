package com.example.dietprefs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dietprefs.data.AppDatabase
import com.example.dietprefs.data.ItemEntity
import com.example.dietprefs.data.VendorEntity
import com.example.dietprefs.model.Preference
import com.example.dietprefs.ui.theme.dietprefsGrey
import com.example.dietprefs.ui.theme.user1Red
import com.example.dietprefs.ui.theme.user2Magenta
import com.example.dietprefs.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

@Composable
fun VendorDetailScreen(
    vendorName: String,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State
    var vendor by remember { mutableStateOf<VendorEntity?>(null) }
    var menuItems by remember { mutableStateOf<List<ItemEntity>>(emptyList()) }
    var user1MatchingItems by remember { mutableStateOf<List<ItemEntity>>(emptyList()) }
    var user2MatchingItems by remember { mutableStateOf<List<ItemEntity>>(emptyList()) }
    var selectedTab by remember { mutableStateOf("menu") } // "menu" or "info"
    
    val user1Prefs by sharedViewModel.user1Prefs.collectAsState()
    val user2Prefs by sharedViewModel.user2Prefs.collectAsState()
    val isTwoUserMode = user1Prefs.isNotEmpty() && user2Prefs.isNotEmpty()

    // Load vendor data
    LaunchedEffect(vendorName) {
        coroutineScope.launch {
            val db = AppDatabase.getDatabase(context)
            val vendorWithItems = db.vendorDao().getVendorWithItemsByName(vendorName)
            vendorWithItems?.let { vwi ->
                vendor = vwi.vendor
                menuItems = vwi.items
                
                // Filter items for each user
                user1MatchingItems = if (user1Prefs.isNotEmpty()) {
                    vwi.items.filter { item -> matchesPrefs(user1Prefs, item) }
                } else emptyList()
                
                user2MatchingItems = if (user2Prefs.isNotEmpty()) {
                    vwi.items.filter { item -> matchesPrefs(user2Prefs, item) }
                } else emptyList()
            }
        }
    }

    Scaffold(
        topBar = {
            VendorDetailTopBar(
                user1Prefs = user1Prefs,
                user2Prefs = user2Prefs,
                navController = navController,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Vendor Name Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFA500)) // Orange like Chipotle in wireframe
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vendorName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Tab Selection (Menu/Info)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { selectedTab = "menu" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "menu") MaterialTheme.colorScheme.primary else Color.Gray
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Menu Items")
                }
                Button(
                    onClick = { selectedTab = "info" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "info") MaterialTheme.colorScheme.primary else Color.Gray
                    ),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("Info & Hours")
                }
            }

            when (selectedTab) {
                "menu" -> MenuItemsTab(
                    menuItems = menuItems,
                    user1MatchingItems = user1MatchingItems,
                    user2MatchingItems = user2MatchingItems,
                    isTwoUserMode = isTwoUserMode,
                    vendor = vendor
                )
                "info" -> VendorInfoTab(vendor = vendor)
            }

            // External Platform Links (bottom of screen)
            ExternalPlatformsSection(vendor = vendor)
        }
    }
}

@Composable
private fun MenuItemsTab(
    menuItems: List<ItemEntity>,
    user1MatchingItems: List<ItemEntity>,
    user2MatchingItems: List<ItemEntity>,
    isTwoUserMode: Boolean,
    vendor: VendorEntity?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(menuItems) { item ->
            MenuItemCard(
                item = item,
                matchesUser1 = user1MatchingItems.contains(item),
                matchesUser2 = user2MatchingItems.contains(item),
                isTwoUserMode = isTwoUserMode
            )
        }
        
        // Voting Card Section (like wireframe shows)
        item {
            VotingCard()
        }
    }
}

@Composable
private fun MenuItemCard(
    item: ItemEntity,
    matchesUser1: Boolean,
    matchesUser2: Boolean,
    isTwoUserMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isTwoUserMode && matchesUser1 && matchesUser2 -> Color(0xFF4CAF50) // Green - matches both
                matchesUser1 -> user1Red.copy(alpha = 0.3f)
                matchesUser2 -> user2Magenta.copy(alpha = 0.3f)
                else -> Color.LightGray
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            // User indicators (like wireframe shows "1 of 4")
            if (isTwoUserMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (matchesUser1) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "User 1 Match",
                            tint = user1Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (matchesUser2) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "User 2 Match", 
                            tint = user2Magenta,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Text(
                text = String.format("$%.2f", item.price),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VotingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Pictures of food\nin tinderlike card deck\nfor viewing and\nvoting (if logged in)",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    IconButton(
                        onClick = { /* TODO: Implement downvote */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.ThumbDown,
                            contentDescription = "Downvote",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { /* TODO: Implement upvote */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = "Upvote",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorInfoTab(vendor: VendorEntity?) {
    vendor?.let { v ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Map placeholder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Map View", fontSize = 16.sp)
                        }
                    }
                }
            }
            
            item {
                VendorInfoCard(vendor = v)
            }
            
            item {
                HoursCard()
            }
        }
    }
}

@Composable
private fun VendorInfoCard(vendor: VendorEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Contact Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = "Phone")
                Spacer(modifier = Modifier.width(8.dp))
                Text("${vendor.phone}")
            }
            
            Text(vendor.address)
            Text("Zipcode: ${vendor.zipcode}")
            
            if (vendor.website.isNotBlank()) {
                Text(
                    "Website: ${vendor.website}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun HoursCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Hours",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Placeholder hours - would need to parse vendor.hours field
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            days.forEach { day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(day, fontWeight = FontWeight.Medium)
                    Text("9:00 am - 10:00 pm")
                }
            }
        }
    }
}

@Composable
private fun ExternalPlatformsSection(vendor: VendorEntity?) {
    Column {
        Divider()
        
        Text(
            "Leave dietprefs",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp, 8.dp),
            textAlign = TextAlign.Center
        )
        
        // Delivery Platforms Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PlatformButton("Chipotle", vendor?.website?.isNotBlank() == true)
            PlatformButton("Ubereats", vendor?.ubereats == true)
            PlatformButton("Grubhub", vendor?.grubhub == true)
            PlatformButton("Doordash", vendor?.doordash == true)
            PlatformButton("Postmates", vendor?.postmates == true)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Review Platforms Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PlatformButton("Yelp", vendor?.yelp == true)
            PlatformButton("Google", vendor?.googleReviews == true)
            PlatformButton("TripAdvisor", vendor?.tripadvisor == true)
            PlatformButton("Hooked", false) // Not in data model
            PlatformButton("Groupon", false) // Not in data model
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PlatformButton(name: String, isEnabled: Boolean) {
    Button(
        onClick = { /* TODO: Open external app/website */ },
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) Color.DarkGray else Color.LightGray
        ),
        modifier = Modifier
            .width(65.dp)
            .height(32.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            name,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun VendorDetailTopBar(
    user1Prefs: Set<Preference>,
    user2Prefs: Set<Preference>,
    navController: NavController,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(dietprefsGrey)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        
        // Preference summary in center
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user1Prefs.isNotEmpty() && user2Prefs.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = "User 1", tint = user1Red, modifier = Modifier.size(16.dp))
                    Text(" & ", color = Color.White, fontSize = 12.sp)
                    Icon(Icons.Default.Person, contentDescription = "User 2", tint = user2Magenta, modifier = Modifier.size(16.dp))
                    Text(" ${user1Prefs.joinToString(" & ") { it.display }} results", color = Color.White, fontSize = 12.sp)
                }
            } else {
                val activePrefs = if (user1Prefs.isNotEmpty()) user1Prefs else user2Prefs
                Text(
                    "${activePrefs.joinToString(" & ") { it.display }} results",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
        }
    }
}

// Helper function to match preferences (should probably move to ViewModel)
private fun matchesPrefs(prefs: Set<Preference>, item: ItemEntity): Boolean {
    if (prefs.isEmpty()) return true
    return prefs.all { pref ->
        pref.matcher?.invoke(item) ?: true
    }
}