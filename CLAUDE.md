# Dietprefs Project Summary

## Overview
Dietprefs is a restaurant discovery app that helps users find restaurants matching their dietary preferences and price constraints. The app consists of:
- **Android app**: Kotlin + Jetpack Compose (MVVM architecture)
- **Backend**: Python FastAPI with SQLAlchemy ORM
- **Database**: PostgreSQL (deployed on Railway)

## Recent Major Refactoring (Completed)

### Priority 1: DRY Violations (Completed)
1. **Created helper methods in SharedViewModel**:
   - `getApiPreferences()`: Converts UI preferences to API format
   - `getSortParameters()`: Converts sort state to API format
   - Eliminates duplication in `searchVendors()`, `loadNextPage()`, and `fetchMenuItems()`

2. **Created Constants.kt** (`android/app/src/main/java/com/example/dietprefs/Constants.kt`):
   - Centralized configuration: `PAGE_SIZE`, `MIN_PRICE`, `MAX_PRICE`, `PRICE_STEP`, test location coordinates

3. **Backend service layer separation**:
   - Created `DistanceService`: Haversine distance calculations, bounding box logic
   - Created `FilterService`: Item filtering logic with preference + price support
   - Refactored `VendorService`: Reduced from 327 to 237 lines

### Priority 2: Component Extraction (Completed)
1. **Extracted Android UI components**:
   - `VendorSearchBar.kt`: Search bar for filtering results
   - `FilterButton.kt`: Reusable filter button component
   - `SortableHeader.kt`: Sortable table header with direction indicators
   - `VendorListItem.kt`: Restaurant list item with rating visualization
   - `PreferenceTile.kt`: Individual preference tile for selection grid
   - `PreferenceGrid.kt`: 16x2 grid for dietary preference selection (3dp spacing)
   - `PriceSelectionDialog.kt`: Dialog for selecting max price filter

2. **Reduced file sizes**:
   - `SearchResultsScreen.kt`: 543 → 396 lines
   - `PreferenceScreen.kt`: 482 → 391 lines
   - Backend `vendor_service.py`: 327 → 237 lines

## Recent Bug Fixes

### 1. Price Filter Integration (Fixed)
**Problem**: Price filters weren't being passed to the restaurant detail screen, causing all items to show even when user selected "under $10".

**Solution**:
- Added `user1_max_price` and `user2_max_price` parameters throughout the stack:
  - Backend API endpoint (`/api/v1/vendors/{id}/items`)
  - Backend `VendorService.get_vendor_items()`
  - Android `DietPrefsApiService.getVendorItems()`
  - Android `VendorRepository.getVendorItems()`
  - Android `SharedViewModel.fetchMenuItems()`
- Price is now treated as a filter alongside dietary preferences

### 2. Inactive User Filter Bug (Fixed)
**Problem**: When only user1 had filters selected, user2 (with no filters) was matching ALL items, causing everything to be included.

**Solution**:
- Added "active user" logic in `VendorService.get_vendor_items()`:
  - Only check filters for users who have at least one preference OR a price filter
  - Inactive users don't participate in filtering decisions
  - Items must match at least one ACTIVE user's filters

### 3. Missing Import (Fixed)
**Problem**: Backend failed to start with `NameError: name 'Optional' is not defined`

**Solution**: Added `Optional` to imports in `backend/app/api/v1/vendors.py`

## Architecture

### Filter System
The app supports two types of filters that work together:

1. **Categorical Filters (Preferences)**:
   - Boolean dietary restrictions (vegetarian, gluten-free, etc.)
   - Stored as `Set<Preference>` in Android, `List[str]` in backend
   - 32 total preferences mapped in `FilterService.PREFERENCE_FIELD_MAP`

2. **Numeric Filters (Price)**:
   - Maximum price threshold per user
   - Stored as `Float?` in Android, `Optional[float]` in backend
   - Range: $5-$30 (configurable in Constants.kt)

### Key Services (Backend)

#### DistanceService (`backend/app/services/distance_service.py`)
- `calculate_distance()`: Haversine formula for distance calculation
- `get_bounding_box_deltas()`: Lat/lng deltas for SQL bounding box queries
- `is_within_distance()`: Check if location is within max distance (10 miles)

#### FilterService (`backend/app/services/filter_service.py`)
- `item_matches_preferences()`: Check if item matches ALL preferences (AND logic) + price constraint
- `build_preference_filter()`: Build SQLAlchemy filter expression for vendor search
- Returns `True` if preferences list is empty (no restrictions)

#### VendorService (`backend/app/services/vendor_service.py`)
- `search_vendors()`: Main search with filtering, sorting, pagination
- `get_vendor_items()`: Get items for restaurant detail view with filtering
- Uses "active user" logic: only users with filters participate in filtering

### Data Flow

#### Restaurant Search
1. User selects preferences + price → `SharedViewModel`
2. `searchVendors()` converts to API format via helper methods
3. Backend applies bounding box filter, then filters vendors with matching items
4. Results sorted by rating/distance/item_count and paginated
5. `cachedVendorResponses` stores full data, `pagedVendors` stores display data

#### Restaurant Detail
1. User clicks restaurant → `selectVendorByName()` finds vendor in cache
2. `fetchMenuItems()` called with vendor ID + both users' filters
3. Backend filters items: must match at least one ACTIVE user's filters
4. Items displayed with `matches_user1` and `matches_user2` metadata

## Important Files

### Android
- `SharedViewModel.kt`: Central state management, API calls, filter logic
- `PreferenceScreen.kt`: Main preference selection UI (391 lines)
- `SearchResultsScreen.kt`: Restaurant search results table (396 lines)
- `RestaurantDetailScreen.kt`: Restaurant detail with menu items
- `Constants.kt`: App-wide configuration constants
- `VendorRepository.kt`: API communication layer

### Backend
- `app/services/vendor_service.py`: Vendor search and item filtering (237 lines)
- `app/services/filter_service.py`: Preference and price filtering logic
- `app/services/distance_service.py`: Distance calculations
- `app/api/v1/vendors.py`: API endpoints for vendor operations
- `app/config.py`: Backend configuration (MAX_DISTANCE_MILES = 10.0)

## Configuration

### Android Constants
```kotlin
PAGE_SIZE = 10
MIN_PRICE = 5f
MAX_PRICE = 30f
PRICE_STEP = 1f
TEST_LATITUDE = 45.6770  // Bozeman, MT
TEST_LONGITUDE = -111.0429
```

### Backend Config
```python
MAX_DISTANCE_MILES = 10.0
```

## UI Spacing
- PreferenceGrid: 3dp spacing (both vertical and horizontal)
- User requested this specific value after testing 2dp and 4dp

## Known Working State
- ✅ Restaurant search with dietary preferences + price filters
- ✅ Dual-user mode (two sets of filters working independently)
- ✅ Restaurant detail screen with correct item filtering
- ✅ Distance-based search (10-mile radius)
- ✅ Sorting by rating, distance, or item count
- ✅ Pagination with local caching and API fetching
- ✅ Price filter properly integrated throughout

## Next Steps
User will move onto a new issue (TBD).
