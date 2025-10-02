# Dietprefs - Food Vendor & Restaurant Discovery App

## Overview
Multi-platform food vendor & restaurant discovery application for **Android, iOS, and Web**. Helps users find food that matches their specific dietary needs with filtering, rating, and sorting capabilities. All platforms share a common backend API and database.

## Architecture

**⚠️ MIGRATION IN PROGRESS**: Transitioning from Android-only (Room) to multi-platform (Backend API).

See **[architecture.md](architecture.md)** for complete technical specifications.

### Current Target Architecture
- **Backend**: Python FastAPI + PostgreSQL (shared across all platforms)
- **Android Client**: Jetpack Compose + Retrofit (MVVM pattern)
- **iOS Client**: SwiftUI + URLSession (planned)
- **Web Client**: React/Vue + REST API (planned)
- **Database**: PostgreSQL (hosted on Railway/Render)
- **API**: RESTful JSON endpoints

### Legacy Architecture (Being Phased Out)
- **Platform**: Android with Jetpack Compose
- **Database**: Room (local SQLite) - **TO BE REMOVED**
- **Architecture Pattern**: MVVM with SharedViewModel
- **Navigation**: Jetpack Navigation Compose
- **Language**: Kotlin

## Key Data Entities

**Note**: These entities will become API response models. Database schema defined in `architecture.md`.

### Vendor
Represents restaurants/vendors with:
- Location data (lat, lng, address, zipcode)
- Delivery options (delivery, takeout, grubhub, doordash, ubereats, etc.)
- Business info (name, phone, website, hours, reviews)
- Custom dietary compliance (`customByNature`)
- **Currently**: `data/VendorEntity.kt` (Room) → **Future**: API response model

### Item
Menu items with extensive dietary/allergen flags:
- **Dietary Preferences**: vegetarian, pescetarian, vegan, keto, organic, gmoFree, locallySourced, raw, kosher, halal
- **Allergens**: noMilk, noEggs, noFish, noShellfish, noPeanuts, noTreenuts, glutenFree, noSoy, noSesame, noMsg, noAlliums
- **Meat Types**: beef, chicken, pork, seafood, noPorkProducts, noRedMeat
- **Nutritional**: lowSugar, highProtein, lowCarb
- **Classification**: entree, sweet
- **Social**: upvotes, totalVotes for rating system
- **Media**: pictures (comma-separated URLs)
- **Currently**: `data/ItemEntity.kt` (Room) → **Future**: API response model

## Core Functionality

### Key Features (Platform-Agnostic)
1. **Dual-User Support**: Two users can set independent dietary preferences
2. **Smart Filtering**: Vendors shown only if they have items matching ALL active preferences (AND logic)
3. **Context-Aware Ratings**: Ratings calculated only from items relevant to current query
4. **Dynamic Sorting**: By vendor rating, distance, or menu item count
5. **Pagination**: Results loaded in pages of 10
6. **Distance Calculation**: Location-based vendor sorting using Haversine formula
7. **Comprehensive Dietary Support**: 33 dietary preferences covering allergies, restrictions, and preferences

### Backend Responsibilities (Future)
- Filter vendors by dietary preferences (complex AND logic)
- Calculate context-aware ratings
- Sort results (rating, distance, item count)
- Paginate responses
- Distance calculations from user location

### Android Client Responsibilities (Current & Future)
- **SharedViewModel** (`viewmodel/SharedViewModel.kt`): State management using StateFlow
  - Manage user preference selections (User 1 and User 2)
  - Call backend API for vendor search
  - Handle loading/error states
  - Cache results for offline viewing (optional)
- **UI Layer**: Jetpack Compose screens (PreferenceScreen, SearchResultsScreen)
- **Navigation**: Jetpack Navigation Compose

## Current State

### ✅ Completed

**Backend (FastAPI + PostgreSQL)** - **✅ DEPLOYED AND LIVE**:
- ✅ Complete REST API with all endpoints (see `BACKEND_SUMMARY.md`)
- ✅ Vendor search with dual-user preference filtering
- ✅ Context-aware rating calculations
- ✅ Distance calculations (Haversine formula)
- ✅ Sorting and pagination
- ✅ Database models with 33 dietary flags
- ✅ **Production URL**: https://dietprefs-production.up.railway.app
- ✅ **PostgreSQL database** connected and operational
- ✅ **Database seeded** with 20 vendors and 140 menu items
- ✅ **All endpoints tested** via Swagger UI and working

**Android (Jetpack Compose + Room)**:
- Preference selection UI with dual-user mode
- Search results screen with sortable columns
- Vendor list with visual rating bars
- Pagination UI with lazy loading
- Color-coded user preferences (red/magenta)
- **Note**: Currently uses Room database (to be replaced with API calls)

### ✅ Phase 2 Complete: Android Migration to Backend API

**Completed Tasks**:
1. ✅ Removed Room database from Android app
2. ✅ Added Retrofit networking layer with Gson converters
3. ✅ Created Repository pattern for API calls
4. ✅ Updated SharedViewModel to call backend API
5. ✅ Implemented smart caching for instant local sorting
6. ✅ Fixed UX issues (scroll to top, no white flash)
7. ✅ Tested end-to-end with deployed backend

**Current Architecture**: Android app fully migrated to REST API with client-side caching.

### ✅ Phase 3 Complete: Location Services & Distance Filtering

**Completed Tasks**:
1. ✅ Added Android location permissions (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
2. ✅ Created LocationService.kt using Google Play Services Location API
3. ✅ Integrated location into SharedViewModel with async/await handling
4. ✅ Implemented 10-mile distance filter in backend (filters out vendors beyond radius)
5. ✅ Updated seed data with realistic GPS coordinates spread across distances
6. ✅ Fixed backend rating calculation bugs (upvotes > total_votes causing HTTP 500)
7. ✅ Fixed SQLAlchemy set() deduplication issue for two-user searches
8. ✅ Added permission request UI with proper callback handling
9. ✅ Tested end-to-end: Returns 17 vendors within 10 miles, all preferences working

**Current Architecture**: Android app with full location services, backend with distance filtering and radius-based vendor filtering.

### 🚧 Next Phase: Feature Completion (Phase 4)

**Priority Tasks**:
1. Welcome/onboarding screen (Wireframe 1)
2. Restaurant detail screen (Wireframe 4) - individual vendor view with menu items
3. Photo voting system (Tinder-style swipe)
4. External integrations (Grubhub, Yelp, DoorDash links)

### ❌ Not Yet Implemented
- Welcome/onboarding screen (Wireframe 1)
- Restaurant detail screen (Wireframe 4)
- Photo voting system (Tinder-style)
- External integrations (Grubhub, Yelp, etc.)
- iOS client (SwiftUI)
- Web client (React/Vue)

## Wireframes & Target Design

### Wireframe Files
Located in `docs/wireframes/` directory:
- `Dietprefs (1 First Load Demo Video).png` - Complete user flow from welcome to results
- `Dietprefs (2 Single Person Mode).png` - Single user flow and settings screen
- `Dietprefs (3 Multiple Person Mode).png` - Dual user mode with split counts
- `Dietprefs (4 Third Page Restaraunt Card).png` - Restaurant detail screens
- `Dietprefs (5 Add data Interface).png` - Data management (separate app scope)

### User Flow (from wireframes)
1. **Welcome Screen**: "DietPrefs - A flair for food" with onboarding
2. **Preference Selection**: Progressive selection of dietary preferences
3. **Multi-User Mode**: Support for two users with color-coded preferences (red/magenta)
4. **Results List**: Vendor list with ratings, distances, and dual-user item counts
5. **Restaurant Detail**: Individual vendor view with menu items, voting, and integrations

### Key UI Components
- **Top Bar**: Shows current preference summary for both users
- **Results Table**: Sortable columns (Vendor/Rating, Distance, Menu Items)
- **Dual User Indicators**: Person icons and split counts (e.g., "4 | 3" items)
- **Search & Filters**: Bottom search bar with filter tags
- **Restaurant Cards**: 
  - Menu items with user-specific matching indicators
  - Tinder-style photo voting system
  - Delivery platform integration (Ubereats, Grubhub, DoorDash, etc.)
  - Review site links (Yelp, Google, TripAdvisor)
  - Map, hours, contact info, directions

### Implementation Status vs Wireframes
✅ **Completed**: Multi-user preferences, results filtering, sorting, dual counts, backend API, Android-to-API migration
🚧 **In Progress**: Location services, distance-based filtering
❌ **Missing**: Welcome screen, restaurant detail screen, photo voting, external integrations

---

## Migration Roadmap

### Phase 1: Backend Development ✅ COMPLETE
See `architecture.md` for detailed implementation plan.

**Goals**:
1. Set up Python FastAPI + PostgreSQL backend
2. Implement `POST /api/vendors/search` endpoint
3. Implement `GET /api/vendors/{id}` and `GET /api/vendors/{id}/items` endpoints
4. Deploy to Railway/Render
5. Seed database with test data (20 vendors × 7 items)

### Phase 2: Android Migration ✅ COMPLETE
**Completed Goals**:
1. ✅ Removed Room database (`AppDatabase.kt`, `VendorDao.kt`, all Room entities)
2. ✅ Added Retrofit networking layer (Retrofit 2.9.0, OkHttp, Gson)
3. ✅ Created Repository pattern (`VendorRepository`)
4. ✅ Updated SharedViewModel with smart caching for instant local sorting
5. ✅ Tested end-to-end with real backend at `https://dietprefs-production.up.railway.app`
6. ✅ Fixed UX issues (scroll to top on sort, eliminated white flash)
7. ✅ Added varied test data to verify API integration

**Key Files Created**:
- `android/app/src/main/java/com/example/dietprefs/network/DietPrefsApiService.kt`
- `android/app/src/main/java/com/example/dietprefs/network/RetrofitClient.kt`
- `android/app/src/main/java/com/example/dietprefs/network/models/ApiModels.kt`
- `android/app/src/main/java/com/example/dietprefs/repository/VendorRepository.kt`

**Key Files Modified**:
- `viewmodel/SharedViewModel.kt` - Complete rewrite with caching
- `model/Preference.kt` - Added `apiName` and `hasApiSupport` fields
- `ui/screens/PreferenceScreen.kt` - Updated to call `searchVendors()`
- `ui/screens/SearchResultsScreen.kt` - Added scroll-to-top on sort

### Phase 3: Location Services & Distance Filtering ✅ COMPLETE
**Completed Goals**:
1. ✅ Added Android location permissions and GPS services (Google Play Services Location API)
2. ✅ Sent user lat/lng to backend API with proper async handling
3. ✅ Implemented max distance filter (10 miles) in backend
4. ✅ Display real distances calculated with Haversine formula
5. ✅ Fixed backend bugs (rating calculation, SQLAlchemy deduplication)
6. ✅ Added permission request UI with callbacks

**Key Files Created**:
- `android/app/src/main/java/com/example/dietprefs/location/LocationService.kt`
- `android/app/src/main/java/com/example/dietprefs/location/UserLocation.kt` (data class)

**Key Files Modified**:
- `viewmodel/SharedViewModel.kt` - Added `requestUserLocation()` and location state
- `ui/screens/PreferenceScreen.kt` - Added permission launcher and location request flow
- `backend/app/services/vendor_service.py` - Added 10-mile distance filter and rating fixes
- `backend/app/seed.py` - Updated with realistic GPS coordinates and fixed upvotes bug
- `android/app/src/main/AndroidManifest.xml` - Added location permissions
- `android/gradle/libs.versions.toml` - Added play-services-location and kotlinx-coroutines-play-services

### Phase 4: Feature Completion (Future)
**Goals**:
1. Welcome/onboarding screen (Wireframe 1)
2. Restaurant detail screen (Wireframe 4)
3. Photo voting system (Tinder-style swipe)
4. External integrations (Grubhub, Yelp, etc.)

### Phase 5: iOS & Web Clients (Future)
**Goals**:
1. iOS app (SwiftUI + URLSession)
2. Web app (React/Vue)
3. All clients use shared backend API

---

## Development Guidelines

### Code Organization
- **Keep wireframes as UI reference**: All UI decisions should match wireframe designs
- **Platform-agnostic logic goes in backend**: Filtering, sorting, rating calculations
- **Client handles UI state only**: Preference selection, navigation, display logic
- **Use repository pattern**: Separate API calls from ViewModel logic

### Testing Strategy
- **Backend**: Unit tests for filtering logic, integration tests for endpoints
- **Android**: UI tests for preference selection → search flow
- **Mock API responses** for offline Android development

### Naming Conventions
- Preference names: lowercase with underscores (e.g., `gluten_free`, `no_peanuts`)
- API endpoints: RESTful naming (`/api/vendors/search`, not `/api/searchVendors`)
- Database tables: lowercase plural (`vendors`, `items`)

---

## Recent Work Context

### 2025-10-02: Phase 3 Complete - Location Services & Distance Filtering ✅

- **Location Services Implementation** (Complete):
  - Created LocationService.kt using Google Play Services FusedLocationProviderClient
  - Added kotlinx-coroutines-play-services dependency for .await() extension
  - Implemented getCurrentLocation() and getLastKnownLocation() with permission checks
  - Added proper SecurityException handling and permission validation
  - Updated SharedViewModel.requestUserLocation() to be suspend function
  - Modified PreferenceScreen with rememberLauncherForActivityResult for permission requests
  - Search now waits for location before calling API (no race conditions)
  - Added hardcoded San Francisco location (37.7749, -122.4194) for emulator testing

- **Backend Distance Filtering** (Complete):
  - Implemented 10-mile radius filter in vendor_service.py
  - Filters out vendors beyond MAX_DISTANCE_MILES when lat/lng provided
  - Updated seed.py with realistic GPS coordinates spread across distances:
    - Vendors 1-10: Within 5 miles of San Francisco center
    - Vendors 11-15: 5-10 miles from center
    - Vendors 16-20: 10-15 miles from center (filtered out)
  - Haversine distance calculations working correctly

- **Critical Bug Fixes**:
  - Fixed seed.py: upvotes can no longer exceed total_votes (was `randint(0, total_votes + 1)`)
  - Fixed vendor_service.py: clamped rating percentage to max 1.0 with `min()` function
  - Fixed vendor_service.py: replaced `set()` with ID-based deduplication for SQLAlchemy items
  - All three fixes deployed and tested - vegetarian, vegan, halal, raw now working

- **Testing Results**:
  - ✅ Location permission dialog appears and works
  - ✅ Returns 17 vendors within 10 miles (vendors 16, 18, 19 filtered out)
  - ✅ Distance values display correctly (0.05 mi to 8.51 mi)
  - ✅ All preferences working: vegetarian, vegan, halal, raw, pescetarian, keto, etc.
  - ✅ Fast loading with getLastKnownLocation() first (instant vs 2+ seconds)
  - ✅ Two-user mode working correctly

### 2025-10-01: Phase 2 Complete - Android Fully Migrated to API ✅

- **Android Migration** (Complete):
  - Removed all Room database dependencies (Room, KSP, entity files)
  - Implemented Retrofit 2.9.0 networking layer with Gson converters
  - Created `VendorRepository` with Result-based error handling
  - Complete SharedViewModel rewrite with smart caching strategy
  - Cache stores all fetched vendors for instant local sorting
  - Sorting now instant (no API call, no screen reload)
  - Fixed UX: scroll to top on sort, eliminated white flash during loads
  - Added INTERNET and ACCESS_NETWORK_STATE permissions
  - Updated Preference model with `apiName` mapping (snake_case)
  - Tested end-to-end with varied backend data - confirmed API integration

- **Backend Updates**:
  - Modified seed data to create varied vendor specialties
  - Vendors 5, 10, 15, 20: Vegetarian-focused (5 items each)
  - Vendors 7, 14: Vegan-focused (3 items each)
  - Vendors 4, 8, 12, 16, 20: Seafood-focused (4 items each)
  - Force-reseed on deploy to clear old test data
  - Created admin endpoint (for future reseeding needs)

- **Architecture Achievements**:
  - Clean separation: API → Repository → ViewModel → UI
  - Client-side caching eliminates unnecessary API calls
  - Instant sorting/filtering on cached results
  - Progressive loading ready (fetch page 1, cache, show more on scroll)
  - Ready for location-based features (lat/lng parameters already supported)

### 2025-10-01: Backend Deployed ✅

- **Architecture Planning**:
  - Created `architecture.md` with complete multi-platform specifications
  - Updated `CLAUDE.md` to reflect backend-first strategy
  - Decided on Python FastAPI + PostgreSQL + Railway stack

- **Backend Implementation** (Complete):
  - Built complete FastAPI REST API (`backend/` directory)
  - Implemented all 4 API endpoints with full functionality
  - Created SQLAlchemy models (Vendor + Item with 33 dietary flags)
  - Implemented complex filtering logic with AND logic in `VendorService`
  - Context-aware rating calculations
  - Haversine distance calculations
  - Sorting (rating/distance/item_count) and pagination
  - Database seeding script (20 vendors × 7 items = 140 items)

- **Deployment** (Complete):
  - ✅ **Deployed to Railway**: https://dietprefs-production.up.railway.app
  - ✅ PostgreSQL database connected
  - ✅ Database seeded with test data
  - ✅ All endpoints tested and verified working
  - ✅ API documentation live at `/docs`

- **Repository Restructure**:
  - Reorganized to clean monorepo: `android/`, `backend/`, `docs/`
  - Removed old `dietprefs-backend/` directory
  - Updated all cross-references in documentation

- **Rollback Point**: Commit `12032ae` ("distance text to white") - last commit before backend work

### Previous Work (Android)
- Implemented preference filtering logic with AND logic
- Built rating system based on query-relevant items only
- Created dual-user preference handling with StateFlow
- Fixed infinite loading spinner and empty state handling