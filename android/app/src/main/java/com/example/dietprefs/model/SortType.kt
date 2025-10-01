package com.example.dietprefs.model

enum class SortColumn {
    VENDOR_RATING, // "Vendor" - by rating (upvotes/total_votes)
    DISTANCE,      // "Dist" - by distance
    MENU_ITEMS     // "Menu Items" - by combined item count
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

data class SortState(
    val column: SortColumn = SortColumn.MENU_ITEMS, // Default sort
    val direction: SortDirection = SortDirection.DESCENDING // Default for menu items (highest first)
)