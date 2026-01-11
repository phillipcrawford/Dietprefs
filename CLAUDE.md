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

### SearchResultsScreen Filter Buttons UI (Completed)
**Goal**: Update filter buttons with proper labels and fixed sizing for service type and geographic region filters.

**Problem**: Filter buttons displayed generic "Type 1" through "Type 10" labels with no functionality. Button sizing was flexible, creating inconsistent appearance.

**Solution Implemented**:
1. **Button Layout** (SearchResultsScreen.kt:378-403):
   - **Row 1**: "Delivery", "Open", "USA", "Europe", "N Afr/ME"
   - **Row 2**: "Takeout", "Fusion", "Mex & SA", "Sub Sah", "E Asia"
   - 4 service type buttons (text)
   - 6 geographic region buttons (text placeholders for future icons)

2. **CompactFilterButton Component** (SearchResultsScreen.kt:414-432):
   - Fixed size: 64dp × 40dp (icon-sized, uniform)
   - Font size: 14.sp for legibility
   - Single-line text with truncation
   - All 10 buttons identical in appearance
   - Ready for icon replacement (6 region buttons will use geographic silhouettes)

3. **Search Bar Styling** (VendorSearchBar.kt):
   - Corner radius: 24.dp → 8.dp (less rounded)
   - Horizontal padding: 4.dp (wider, aligns with filter buttons)
   - Vertical padding: 4.dp (balanced spacing)

4. **Spacing Fix**:
   - **Problem Diagnosed**: "Filter by:" text label was creating excessive space (~20dp) between search bar and buttons
   - **Solution**: Removed label, adjusted padding to 6.dp top for clean visual separation
   - **Result**: 10dp total spacing (search bar 4.dp bottom + filter buttons 6.dp top)

**Geographic Regions** (awaiting icon implementation):
- USA
- Europe
- North Africa/Middle East
- Mexico & Central/South America
- Sub Saharan Africa
- East Asia

**Next Steps**:
- Create/source geographic region silhouette icons (SVG format)
- Place icons in `res/drawable/`
- Update `CompactFilterButton` to accept icon parameter
- Replace text with icons for 6 region buttons

---

### Vendor-Level Filters Implementation (Completed)
**Goal**: Make filter buttons functional by implementing server-side vendor filtering with client-server architecture.

**Filter Types**:
1. **Service Options** (2 filters):
   - `delivery`: Vendor offers delivery service
   - `takeout`: Vendor offers takeout service

2. **Geographic Regions** (6 filters):
   - `usa`: American cuisine
   - `europe`: European cuisine
   - `north_africa_middle_east`: Mediterranean/Middle Eastern/Indian cuisine
   - `mexico_south_america`: Mexican and South American cuisine
   - `sub_saharan_africa`: Sub-Saharan African cuisine
   - `east_asia`: Japanese, Chinese, Thai, Vietnamese, Korean cuisine

3. **Special Categories** (2 filters):
   - `fusion`: Hard-to-categorize fusion restaurants (manual flag)
   - `open`: Currently open based on operating hours (time-based calculation)

**Filter Logic**: AND operation - restaurant must match ALL selected filters

**Backend Changes**:

1. **Database Schema** (`backend/app/models/vendor.py`):
   - Added 7 boolean columns to Vendor model:
     - `cuisine_usa`, `cuisine_europe`, `cuisine_north_africa_middle_east`
     - `cuisine_mexico_south_america`, `cuisine_sub_saharan_africa`, `cuisine_east_asia`
     - `fusion` (manual catch-all flag)
   - Note: `delivery` and `takeout` columns already existed

2. **API Schema** (`backend/app/schemas/vendor.py:101`):
   - Added `vendor_filters: List[str]` to VendorSearchRequest
   - Accepts filter names as lowercase snake_case strings

3. **Filter Service** (`backend/app/services/vendor_service.py:44-84`):
   - Applies vendor-level filters with AND logic using SQLAlchemy:
     ```python
     if request.vendor_filters:
         filter_conditions = []
         for filter_name in request.vendor_filters:
             if filter_lower == "delivery":
                 filter_conditions.append(Vendor.delivery == True)
             # ... (9 more filters)

         if filter_conditions:
             query = query.filter(*filter_conditions)  # AND logic
     ```
   - Special "Open" filter implementation:
     - Parses `hours` JSON field for each vendor
     - Compares current day/time against operating hours
     - Filters post-query (not in SQL) due to complexity

4. **Seed Data** (`backend/app/seed.py:342-370`):
   - Added cuisine classification logic based on cuisine type:
     ```python
     if cuisine_type in ["American Breakfast", "American BBQ", ...]:
         cuisine_usa = True
     elif cuisine_type in ["Italian", "French Cafe"]:
         cuisine_europe = True
     # ... (more mappings)
     ```
   - Restaurants can have multiple cuisine flags (e.g., fusion restaurants)

**Frontend Changes**:

1. **API Models** (`ApiModels.kt:81-83`):
   - Added `vendorFilters: List<String>` to VendorSearchRequest

2. **ViewModel State** (`SharedViewModel.kt:71-83`):
   - Added `_vendorFilters` MutableStateFlow for selected filters
   - Added `toggleVendorFilter()` to add/remove filters from selection
   - Updated `searchVendors()` to pass filters to API
   - Updated `clearAllFilters()` to reset vendor filters

3. **Repository** (`VendorRepository.kt:38`):
   - Added `vendorFilters` parameter to `searchVendors()`
   - Passes filters to API request

4. **UI Integration** (`SearchResultsScreen.kt:71-98`):
   - Observes `vendorFilters` StateFlow from ViewModel
   - `LaunchedEffect(selectedFilters)` triggers search when filters change
   - Filter buttons call `toggleVendorFilter()` on click
   - Buttons show selected state with `selectedGrey` background

**Architecture: Client-Server Filter Flow**:

1. **Client-Side (UI State)**:
   - User clicks filter button → `toggleVendorFilter()` updates StateFlow
   - Button shows selected/unselected state immediately

2. **Trigger Server Request**:
   - `LaunchedEffect` detects state change → calls `searchVendors()`

3. **Server-Side (Database Filtering)**:
   - Android sends `vendorFilters` list to backend
   - Backend applies filters directly in SQL query
   - Database returns only matching vendors

4. **Client Updates**:
   - Response updates `pagedVendors` StateFlow
   - UI displays filtered results

**Why Server-Side Filtering?**:
- **Efficient**: Database filters at source without downloading all vendors
- **Combined logic**: Works with preferences, distance, search query, sorting
- **Pagination**: Only fetches needed vendors (10 at a time)
- **DRY**: Single source of truth for all clients (Android, iOS, Web)

**Filter Naming Convention**:
- Backend API: lowercase snake_case (`north_africa_middle_east`)
- UI Display: Abbreviated text (`N Afr/ME`)
- Future: Region filters will use icon images instead of text

**Files Modified**:
- Backend: `models/vendor.py`, `schemas/vendor.py`, `services/vendor_service.py`, `seed.py`
- Frontend: `ApiModels.kt`, `VendorRepository.kt`, `SharedViewModel.kt`, `SearchResultsScreen.kt`

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
The app supports three types of filters:

1. **Item-Level Filters (Dietary Preferences)**:
   - Boolean dietary restrictions (vegetarian, gluten-free, etc.)
   - Filters menu items, not vendors
   - Stored as `Set<Preference>` in Android, `List[str]` in backend
   - 33 total preferences (32 dietary + LOW_PRICE) in `Preference.kt`
   - Backend: 32 dietary preferences mapped in `FilterService.PREFERENCE_FIELD_MAP`
   - User-specific: Each user can have different dietary preferences

2. **Numeric Filters (Price)**:
   - Maximum price threshold per user
   - Filters menu items by price
   - Stored as `Float?` in Android, `Optional[float]` in backend
   - Range: $5-$30 (configurable via `/api/v1/config`)
   - Frontend: LOW_PRICE in preference set synced with price value
   - Backend: Uses separate `user1_max_price` / `user2_max_price` parameters
   - User-specific: Each user can have different price thresholds

3. **Vendor-Level Filters** (NEW):
   - Boolean vendor attributes (delivery, cuisine type, open status)
   - Filters entire restaurants, not individual items
   - Stored as `Set<String>` in Android, `List[str]` in backend
   - 10 total filters: 2 service options + 6 geographic regions + 2 special categories
   - Backend: Applied directly in vendor search SQL query
   - Shared: All users see same vendor-level filter results
   - AND logic: Restaurant must match ALL selected filters

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
- ✅ Vendor-level filters (10 filters: delivery, takeout, open, fusion, 6 cuisine regions)
- ✅ Client-server filter architecture with server-side database filtering
- ✅ Filter state management with reactive UI updates

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

### No Error UI in Android App
**Issue**: API errors are captured in `SharedViewModel._errorMessage` StateFlow but are not displayed to users anywhere in the UI.

**Current Behavior**:
- Network errors, API failures, and validation errors are logged and stored in state
- No Toast, Snackbar, or error text components show these errors to users
- Users have no feedback when operations fail silently

**Files Affected**:
- `SharedViewModel.kt`: Sets `_errorMessage.value` on failures (lines 151, 396, 454, 520, 553)
- All screens: No UI components observe or display `errorMessage` StateFlow

**Impact**:
- Low for typical use (Android app sends valid requests)
- Users unaware of network failures or API issues
- Difficult to debug issues in production

**Future Fix**: Add Snackbar/Toast notifications or error text displays that observe `sharedViewModel.errorMessage` StateFlow.
