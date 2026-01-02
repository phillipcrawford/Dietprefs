# Dietprefs Project Summary

## Overview
Dietprefs is a restaurant discovery app that helps users find restaurants matching their dietary preferences and price constraints. The app consists of:
- **Android app**: Kotlin + Jetpack Compose (MVVM architecture)
- **Backend**: Python FastAPI with SQLAlchemy ORM
- **Database**: PostgreSQL (deployed on Railway)

## Recent Major Refactoring (Completed)

### Multi-Client Architecture: Config Endpoint (NEW)
**Goal**: Move business rules from clients to backend for DRY compliance across future iOS and Web apps.

**Backend Changes**:
- Created `/api/v1/config` endpoint that returns centralized configuration
- Created `backend/app/schemas/config.py`: Config schemas for pricing, pagination, location, sorting
- Created `backend/app/api/v1/config.py`: Config endpoint implementation
- Created `backend/app/services/display_service.py`: Centralized display text formatting

**Config Response Structure**:
```json
{
  "version": "1.0.0",
  "pricing": {
    "min_price": 5.0,
    "max_price": 30.0,
    "price_step": 1.0,
    "default_options": [5.0, 10.0, 15.0, 20.0, 25.0, 30.0]
  },
  "pagination": { "default_page_size": 10, "max_page_size": 100 },
  "location": { "max_distance_miles": 10.0, "default_latitude": 45.6770, ... },
  "sorting": { "options": [...], "default_sort_by": "item_count", ... }
}
```

**Frontend Changes**:
- Created config API models in `ApiModels.kt`
- Added `DietPrefsApiService.getConfig()` and `VendorRepository.getConfig()`
- Added `SharedViewModel.appConfig` StateFlow, fetched on ViewModel init
- Updated `PreferenceScreen` to use backend config for price options (fallback to Constants.kt)

**Benefits**: Change business rules once (backend), all clients (Android, iOS, Web) automatically updated.

---

### LOW_PRICE Preference Refactoring (Completed)
**Goal**: Treat "low price" uniformly with other preferences on the frontend, with backend owning display logic.

**Problem**: LOW_PRICE was handled as a special case throughout the UI, with display formatting ("under $X") duplicated client-side. This violated DRY for multi-client architecture.

**Backend Changes**:
1. Created `DisplayService.build_display_text()`: Formats preferences + price as display strings
2. Updated `VendorSearchResponse` schema: Added `user1_display` and `user2_display` fields
3. Updated `/vendors/search` endpoint: Returns formatted display text for both users
4. `FilterService` already handles unknown preferences gracefully (skips "low_price" in preference matching)

**Frontend Changes**:
1. Added `LOW_PRICE` to `Preference` enum (position 33)
2. Updated `SharedViewModel`:
   - `setUser1MaxPrice()` / `setUser2MaxPrice()` auto-add/remove `LOW_PRICE` from preference sets
   - Added `user1Display` / `user2Display` StateFlows to store backend display text
   - `searchVendors()` captures display text from API response
3. Refactored `PreferenceScreen`:
   - `PreferenceGrid` shows 32 preferences (16 rows)
   - Row 17: "low price" button (left) + user toggle button (right)
   - Clicking "low price" opens `PriceSelectionDialog`
   - Removed `buildFilterDisplayText()` function
4. Updated `PreferencesTopBar`: Builds display text locally (formats LOW_PRICE as "under $X")
5. Updated generic `TopBar` component: Accepts `user1Display` and `user2Display` strings from backend
6. Updated `SearchResultsScreen` and `RestaurantDetailScreen`: Display backend-provided text

**Result**:
- Frontend treats LOW_PRICE like any other preference (just opens dialog instead of toggle)
- Backend owns display formatting logic (single source of truth)
- iOS and Web clients will display identical text without reimplementing formatting

---

### Priority 1: DRY Violations (Completed)
1. **Created helper methods in SharedViewModel**:
   - `getApiPreferences()`: Converts UI preferences to API format (filters out LOW_PRICE)
   - `getSortParameters()`: Converts sort state to API format
   - Eliminates duplication in `searchVendors()`, `loadNextPage()`, and `fetchMenuItems()`

2. **Created Constants.kt** (`android/app/src/main/java/com/example/dietprefs/Constants.kt`):
   - Centralized configuration: `PAGE_SIZE`, `MIN_PRICE`, `MAX_PRICE`, `PRICE_STEP`, test location coordinates
   - **Note**: These are now fallbacks; primary config comes from `/api/v1/config`

3. **Backend service layer separation**:
   - Created `DistanceService`: Haversine distance calculations, bounding box logic
   - Created `FilterService`: Item filtering logic with preference + price support
   - Created `DisplayService`: Display text formatting (DRY across clients)
   - Refactored `VendorService`: Reduced from 327 to 237 lines

### Priority 2: Component Extraction (Completed)
1. **Extracted Android UI components**:
   - `VendorSearchBar.kt`: Search bar for filtering results
   - `FilterButton.kt`: Reusable filter button component
   - `SortableHeader.kt`: Sortable table header with direction indicators
   - `VendorListItem.kt`: Restaurant list item with rating visualization
   - `PreferenceTile.kt`: Individual preference tile for selection grid
   - `PreferenceGrid.kt`: Dynamic grid for dietary preference selection (3dp spacing)
   - `PriceSelectionDialog.kt`: Dialog for selecting max price filter
   - `TopBar.kt`: Generic top bar accepting backend display text

2. **Reduced file sizes**:
   - `SearchResultsScreen.kt`: 543 → 396 lines
   - `PreferenceScreen.kt`: 482 → ~350 lines
   - Backend `vendor_service.py`: 327 → 237 lines

### VendorListItem Text Spacing Fixes (Completed)
**Goal**: Fix text overlap between vendor name and rating display in search results.

**Problem**: In `VendorListItem.kt`, vendor names (e.g., "Smoothie Bar & Juice") overlapped with rating text (e.g., "144/187") in the 48dp row height.

**Solution Implemented**:
1. **Rating text** (VendorListItem.kt:89):
   - Font size: 13sp → **11sp** (reduced visual weight)
   - Top padding: 10dp → **22dp** (shifted down significantly)
2. **Vendor name** (VendorListItem.kt:79):
   - Start padding: 16dp → **12dp** (shifted left 4dp)
   - Top padding: 10dp → **6dp** (shifted up 4dp)

**Result**:
- 16dp vertical separation between vendor name and rating (22dp - 6dp)
- Row height remains 48dp (unchanged)
- Clean visual separation with no text overlap

---

### Backend Search Enhancement: Item Name Search (Completed)
**Goal**: Allow search to match menu item names, not just vendor names/addresses.

**Problem**: Searching for "Fettuccine" or "Fett" wouldn't find restaurants that have those menu items. Search only matched vendor name, address, and SEO tags.

**Solution Implemented** (`backend/app/services/vendor_service.py:43-91`):
1. **Refactored search logic**: Consolidated text search and preference filtering to avoid duplicate joins
2. **Added item name search**: Search pattern now includes `Item.name.ilike(search_pattern)`
3. **Smart join handling**: Join Item table once if needed for search OR preferences
4. **Correct filter logic**: Search filter AND preference filter (both must match if both exist)

**Technical Details**:
```python
# Build text search filter (vendor fields OR item names)
search_filter = or_(
    Vendor.name.ilike(search_pattern),
    Vendor.address.ilike(search_pattern),
    Vendor.seo_tags.ilike(search_pattern),
    Item.name.ilike(search_pattern)  # NEW
)
```

**Result**:
- Searching "Fettuccine" finds all restaurants with that menu item
- Still respects preference filters (only shows if preferences match)
- All preference-filtered items shown (not just the searched item)
- **Status**: Implemented, not yet tested

---

### RestaurantDetailScreen Scrolling & Selection (Completed)
**Goal**: Fix scrolling and selection UX for restaurant detail screen with fixed 204dp viewport.

**Problem**: With a fixed-height scrollable area (204dp) containing restaurant header (60dp) + menu items (48dp each), items near the end of the list couldn't scroll to the top position, causing `firstVisibleItemIndex` to stop incrementing and breaking selection logic.

**Solution Implemented**:
1. **Snap scrolling behavior**: Added `rememberSnapFlingBehavior` to LazyColumn for tactile snap-to-position feedback
2. **Click-to-select**: Made restaurant header and menu items clickable for direct selection
3. **Second-item selection**: Auto-selection based on second visible item from top (not first) for better viewport centering
4. **Bottom padding**: Added 156dp bottom padding to LazyColumn via `contentPadding` parameter
   - Allows all items (including last item) to scroll to top position
   - Calculation: 204dp viewport - 48dp item height = 156dp padding needed
   - Ensures `firstVisibleItemIndex` can increment through all items

**UX Improvements**:
- ✅ All menu items accessible via scrolling (fixed "stuck at item 3 of 7" issue)
- ✅ Snap scrolling provides clear item positioning
- ✅ Dual interaction: scroll to browse OR tap to select directly
- ✅ Selection follows scroll position naturally while allowing manual clicks

**Technical Details** (`RestaurantDetailScreen.kt`):
```kotlin
LazyColumn(
    state = listState,
    flingBehavior = snapFlingBehavior,
    contentPadding = PaddingValues(bottom = 156.dp), // Key fix
    modifier = Modifier.fillMaxWidth().height(204.dp)
)
```
- Selection logic: `secondVisibleIndex = firstVisibleItemIndex + 1`
- Click handlers directly call `sharedViewModel.updateSelectedItemIndex()`

---

## Architecture

### Multi-Client Design
The app is architected for **3 frontends (Android, iOS, Web) sharing 1 backend + database**:

**Backend-Heavy** (DRY across clients):
- ✅ Configuration: `/api/v1/config` endpoint
- ✅ Display formatting: `DisplayService.build_display_text()`
- ✅ Filter logic: `FilterService` with preference mapping
- ✅ Distance calculations: `DistanceService` (Haversine formula)
- ✅ Rating calculations: Backend returns percentages
- ✅ Business rules: Max distance, price ranges, sort options

**Client-Light** (minimal duplication):
- UI-specific: Navigation, state management, platform-specific UI code
- API integration: HTTP calls, response parsing
- Local caching: For offline/performance (optional)

### Filter System
The app supports two types of filters:

1. **Categorical Filters (Preferences)**:
   - Boolean dietary restrictions (vegetarian, gluten-free, etc.)
   - Stored as `Set<Preference>` in Android, `List[str]` in backend
   - 33 total preferences (32 dietary + LOW_PRICE) in `Preference.kt`
   - Backend: 32 dietary preferences mapped in `FilterService.PREFERENCE_FIELD_MAP`

2. **Numeric Filters (Price)**:
   - Maximum price threshold per user
   - Stored as `Float?` in Android, `Optional[float]` in backend
   - Range: $5-$30 (configurable via `/api/v1/config`)
   - Frontend: LOW_PRICE in preference set synced with price value
   - Backend: Uses separate `user1_max_price` / `user2_max_price` parameters

### Key Services (Backend)

#### DisplayService (`backend/app/services/display_service.py`)
- `build_display_text(preferences, max_price)`: Formats display strings for clients
- Converts snake_case to display format (e.g., "gmo_free" → "gmo-free")
- Appends "under $X" for price filters
- **Single source of truth for display formatting across all clients**

#### DistanceService (`backend/app/services/distance_service.py`)
- `calculate_distance()`: Haversine formula for distance calculation
- `get_bounding_box_deltas()`: Lat/lng deltas for SQL bounding box queries
- `is_within_distance()`: Check if location is within max distance (10 miles)

#### FilterService (`backend/app/services/filter_service.py`)
- `item_matches_preferences()`: Check if item matches ALL preferences (AND logic) + price constraint
- `build_preference_filter()`: Build SQLAlchemy filter expression for vendor search
- Gracefully skips unknown preferences (e.g., "low_price")

#### VendorService (`backend/app/services/vendor_service.py`)
- `search_vendors()`: Main search with filtering, sorting, pagination
- `get_vendor_items()`: Get items for restaurant detail view with filtering
- Uses "active user" logic: only users with filters participate in filtering

### Data Flow

#### Restaurant Search
1. User selects preferences + price → `SharedViewModel`
2. `searchVendors()` converts to API format via helper methods
3. Backend applies bounding box filter, then filters vendors with matching items
4. Backend generates `user1_display` and `user2_display` strings
5. Results sorted by rating/distance/item_count and paginated
6. Frontend stores display text and caches vendor data

#### Restaurant Detail
1. User clicks restaurant → `selectVendorByName()` finds vendor in cache
2. `fetchMenuItems()` called with vendor ID + both users' filters
3. Backend filters items: must match at least one ACTIVE user's filters
4. Items displayed with `matches_user1` and `matches_user2` metadata

## Important Files

### Android
- `SharedViewModel.kt`: Central state management, API calls, filter logic, config fetching
- `PreferenceScreen.kt`: Main preference selection UI (~350 lines)
- `SearchResultsScreen.kt`: Restaurant search results table (396 lines)
- `RestaurantDetailScreen.kt`: Restaurant detail with menu items
- `Constants.kt`: Fallback configuration constants
- `VendorRepository.kt`: API communication layer
- `Preference.kt`: Preference enum (33 values including LOW_PRICE)
- `ApiModels.kt`: API request/response models including `AppConfig`

### Backend
- `app/api/v1/config.py`: Configuration endpoint (NEW)
- `app/schemas/config.py`: Config response schemas (NEW)
- `app/services/display_service.py`: Display text formatting (NEW)
- `app/services/vendor_service.py`: Vendor search and item filtering (237 lines)
- `app/services/filter_service.py`: Preference and price filtering logic
- `app/services/distance_service.py`: Distance calculations
- `app/api/v1/vendors.py`: Vendor API endpoints (includes display text generation)
- `app/config.py`: Backend configuration (MAX_DISTANCE_MILES = 10.0)

## Configuration

### Primary: Backend Config Endpoint
Endpoint: `GET /api/v1/config`

Returns dynamic configuration for:
- Pricing: min/max prices, step, default options
- Pagination: page sizes
- Location: max distance, default location
- Sorting: available options, defaults

**Clients fetch on app launch** → Single source of truth for business rules

### Fallback: Android Constants.kt
Used if config fetch fails (offline mode):
```kotlin
PAGE_SIZE = 10
MIN_PRICE = 5f
MAX_PRICE = 30f
PRICE_STEP = 1f
TEST_LATITUDE = 45.6770  // Bozeman, MT
TEST_LONGITUDE = -111.0429
```

### Backend Settings
```python
# backend/app/config.py
MAX_DISTANCE_MILES = 10.0
```

## Test Data
- **Seed data**: 20 vendors, 140 items (7 items per vendor)
- **Item prices**: Random between $5.00 and $25.00
- **Note**: When testing "low price $25", most items pass filter (expected behavior)

## UI Spacing
- PreferenceGrid: 3dp spacing (both vertical and horizontal)
- User requested this specific value after testing 2dp and 4dp

## Known Working State
- ✅ Restaurant search with dietary preferences + price filters
- ✅ Dual-user mode (two sets of filters working independently)
- ✅ Restaurant detail screen with correct item filtering
- ✅ Restaurant detail scrolling with snap behavior and click-to-select
- ✅ All menu items accessible via scrolling (bottom padding solution)
- ✅ Distance-based search (10-mile radius)
- ✅ Sorting by rating, distance, or item count
- ✅ Pagination with local caching and API fetching
- ✅ Price filter properly integrated throughout
- ✅ LOW_PRICE treated uniformly with other preferences
- ✅ Backend display text formatting (DRY across clients)
- ✅ Dynamic configuration from backend (`/api/v1/config`)

## Multi-Client Readiness
**Completed**:
- ✅ Configuration endpoint (prices, pagination, sorting, location)
- ✅ Display text formatting on backend
- ✅ Filter logic centralized on backend

**Future Enhancements** (for iOS/Web):
- `/api/v1/preferences` endpoint: Return full preference list with metadata
- Localization support in config responses
- Feature flags for A/B testing
- Region-specific configuration

## Known Issues & Limitations

### Android Emulator: Keyboard Toolbar Behavior
**Issue**: On Android emulators with hardware keyboard enabled (default configuration), text fields may show a floating toolbar with hamburger/microphone/backspace/search/emoji buttons instead of the soft keyboard.

**Root Cause**:
- Emulator detects host computer's keyboard as a hardware keyboard
- Android's default behavior is to hide soft keyboard when hardware keyboard is present
- Shows floating toolbar as alternative input method

**Workaround for Development**:
```bash
# Enable soft keyboard even with hardware keyboard present
adb shell settings put secure show_ime_with_hard_keyboard 1
```

**Expected Behavior on Real Devices**:
- Real devices without hardware keyboards show soft keyboard normally
- This is an emulator-specific development issue, not a production concern
- Users with physical keyboards (rare on phones) may see similar behavior, which is expected Android OS behavior

**Not Fixed in App Code**: This is Android system-level behavior that cannot be overridden from application code. The workaround requires system settings or ADB configuration.
