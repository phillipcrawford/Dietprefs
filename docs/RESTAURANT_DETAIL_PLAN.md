# Restaurant Detail Screen - Implementation Plan

**Status**: ✅ Completed
**Created**: 2025-10-02
**Completed**: 2025-10-03

---

## Overview

Build the restaurant detail screen that shows when user taps a vendor from search results. Shows restaurant info, matching menu items in a scrollable stack, photos, and voting.

**Wireframe Reference**: `docs/wireframes/Dietprefs (4 Third Page Restaraunt Card).png`

---

## Component Structure

### 1. TopBar Component (Reusable)
**File**: `ui/components/TopBar.kt` (new)

Extract shared top bar used across PreferenceScreen, SearchResultsScreen, and RestaurantDetailScreen.

**Shows**:
- Back arrow (navigation)
- Search summary text (e.g., "vegetarian & organic & no eggs results")
- Settings gear icon

### 2. RestaurantDetailScreen
**File**: `ui/screens/RestaurantDetailScreen.kt` (new)

**Navigation**:
- Triggered by tapping vendor row in SearchResultsScreen
- Receives: `vendor` object, `searchSummary` string

---

## Core UI Elements

### 3. Scrollable Stack
**Component**: LazyColumn

**Structure**:
- **Item 0**: Restaurant header (always at top)
- **Items 1-N**: Menu items (N = number of matching items for user preferences)

**Behavior**:
- Vertically scrollable
- Viewport shows 4 items at a time (restaurant + 3 menu items when available)
- Track scroll position to determine which item is "selected" (in focus)
- Selection follows scroll position automatically (no tapping items)

### 4. Selection Highlighting

**Restaurant header**:
- Selected: Bright yellow/gold background
- Unselected: Muted/darker yellow background

**Menu items**:
- Selected: Light green background
- Unselected: Dark green background

Selection determined by scroll position (which item is centered/in focus).

### 5. Menu Items Display

**Data source**: `GET /api/v1/vendors/{id}/items?user1_preferences=...&user2_preferences=...`

**Each menu item shows**:
- Item name (left side)
- Price (right side) - e.g., "$6.99"
- Position counter (middle) - e.g., "2 of 4" = 2nd menu item out of 4 total matching items
- Counter only shows on menu items, NOT on restaurant header

**Example**:
```
| Bowl        2 of 4        $6.99 |  <- Light green (selected)
| Burrito                   $6.99 |  <- Dark green (unselected)
| Tacos                     $6.99 |  <- Dark green (unselected)
```

---

## Dynamic Content Area (Below Scrollable Stack)

### 6. Photo Carousel (when menu item selected)

**Shows when**: Any menu item (index 1-N) is selected

**Implementation**:
- Swipeable horizontal carousel (HorizontalPager in Compose)
- Displays photos for the currently selected menu item
- No index display needed
- Photos from `item.pictures` field (comma-separated URLs)

### 7. Voting UI (when menu item selected)

**Shows when**: Any menu item (index 1-N) is selected

**UI**:
- Thumbs up icon
- Thumbs down icon
- Vote applies to the selected **menu item** (not individual photos)

**API call**: `POST /api/v1/items/{id}/vote`
```json
{"vote": "up"}  // or "down"
```

### 8. External Links Grid (always visible)

Two rows of buttons (placeholder for now):

**Row 1 - Delivery platforms**:
- Chipotle, Ubereats, Grubhub, Doordash, Postmates

**Row 2 - Review sites**:
- Yelp, Google, Tripadvisor, Hooked, Groupon

**Implementation notes**:
- Similar to filter chips on SearchResultsScreen
- Show only available services based on vendor data (e.g., `vendor.grubhub`, `vendor.yelp`)
- No actual navigation for now (placeholders)

### 9. Info Panel (when restaurant selected)

**Shows when**: Restaurant header (index 0) is selected

**Contents**:
- **Map view**: Google Maps showing restaurant location (vendor.lat, vendor.lng)
- **Directions button**: Opens Google Maps app with directions
- **Hours table**: Mon-Sun with open/close times (parse vendor.hours JSON)
- **Address**: Display vendor.address
- **Phone**: Display vendor.phone (clickable to call)
- **Cuisine type**: Display or infer from data
- **Rating**: Display as "15/20" format (e.g., upvotes/total_votes)

---

## API Integration

### Endpoints to use:

1. **Get menu items**:
   ```
   GET /api/v1/vendors/{id}/items?user1_preferences=vegetarian,gluten_free&user2_preferences=keto
   ```

2. **Vote on item**:
   ```
   POST /api/v1/items/{id}/vote
   Body: {"vote": "up"} or {"vote": "down"}
   ```

### Data flow:
- Vendor object passed from SearchResultsScreen (already have all vendor data)
- Fetch menu items on screen load
- Update vote counts locally after voting (optimistic update)

---

## State Management

### ViewModel additions needed:

```kotlin
// In SharedViewModel or new RestaurantDetailViewModel

private val _selectedIndex = MutableStateFlow(0)
val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

private val _isLoadingItems = MutableStateFlow(false)
val isLoadingItems: StateFlow<Boolean> = _isLoadingItems.asStateFlow()

fun fetchMenuItems(vendorId: Int, user1Prefs: List<String>, user2Prefs: List<String>)
fun voteOnItem(itemId: Int, voteType: String)
fun updateSelectedIndex(index: Int)
```

---

## Implementation Steps (Suggested Order)

1. ✅ **Extract TopBar component** - Make it reusable
2. ✅ **Create RestaurantDetailScreen** - Basic structure with navigation
3. ✅ **Build scrollable stack** - Restaurant header + menu items list
4. ✅ **Implement selection highlighting** - Track scroll position, update colors
5. ✅ **Fetch and display menu items** - API call + display with counter
6. ✅ **Build photo carousel** - Show photos for selected menu item
7. ✅ **Add voting UI** - Thumbs up/down with API integration
8. ✅ **Build external links grid** - Placeholder buttons
9. ✅ **Build info panel** - Hours, map, address (when restaurant selected)

---

## Edge Cases to Handle

1. **No matching items**: Vendor has 0 items matching preferences
   - Show restaurant header only
   - Display message: "No matching items for your preferences"

2. **Fewer than 3 items**: Only 1-2 menu items match
   - Still show restaurant + available items
   - Adjust viewport sizing

3. **No photos for item**: Item has empty `pictures` field
   - Show placeholder image or "No photos available" message

4. **API errors**: Menu items fetch fails
   - Show error message with retry button
   - Keep restaurant info visible

---

## Notes

- Photos are stored as comma-separated URLs in `item.pictures` field
- Voting requires calling backend API (affects all users)
- Map integration may require Google Maps SDK (or use static map image initially)
- Hours are stored as JSONB in database (e.g., `{"monday": "10:00-22:00"}`)

---

**Last Updated**: 2025-10-03

---

## Implementation Summary

All features successfully implemented:
- ✅ Reusable TopBar component extracted
- ✅ Restaurant detail screen with navigation from SearchResultsScreen
- ✅ Scrollable stack (60dp restaurant header + 48dp menu items, viewport shows ~3 items)
- ✅ Scroll-based selection highlighting (#ffd24d selected gold, #cc9900 deselected gold for restaurant)
- ✅ Menu items API integration with position counters
- ✅ Photo carousel using HorizontalPager with Coil image loading
- ✅ Voting UI with thumbs up/down (POST /api/v1/items/{id}/vote)
- ✅ External links grid showing available delivery/review platforms
- ✅ Info panel with restaurant details (address, phone, rating, hours)
