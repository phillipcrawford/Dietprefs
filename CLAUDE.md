# Dietprefs - Food Vendor & Restaurant Discovery App

## Overview
Multi-platform food vendor & restaurant discovery application for **Android, iOS, and Web**. Helps users find food that matches their specific dietary needs with filtering, rating, and sorting capabilities. All platforms share a common backend API and database.

## Architecture

**⚠️ MIGRATION IN PROGRESS**: Transitioning from Android-only (Room) to multi-platform (Backend API).

See **[architecture.md](./architecture.md)** for complete technical specifications.

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

**Backend (FastAPI + PostgreSQL)**:
- Complete REST API with all endpoints (see `BACKEND_SUMMARY.md`)
- Vendor search with dual-user preference filtering
- Context-aware rating calculations
- Distance calculations (Haversine formula)
- Sorting and pagination
- Database models with 33 dietary flags
- Seed data script (20 vendors × 7 items)
- Deployment documentation for Railway/Render/Heroku
- Ready for production deployment

**Android (Jetpack Compose + Room)**:
- Preference selection UI with dual-user mode
- Search results screen with sortable columns
- Vendor list with visual rating bars
- Pagination UI with lazy loading
- Color-coded user preferences (red/magenta)
- **Note**: Currently uses Room database (to be replaced with API calls)

### 🚧 Next Phase: Android Migration to Backend API

**Priority Tasks**:
1. Remove Room database from Android app
2. Add Retrofit networking layer
3. Create repository pattern for API calls
4. Update SharedViewModel to call backend API
5. Test end-to-end with deployed backend

See Phase 2 in "Migration Roadmap" section below.

### ❌ Not Yet Implemented
- Android networking layer (Retrofit)
- Welcome/onboarding screen (Wireframe 1)
- Restaurant detail screen (Wireframe 4)
- Photo voting system (Tinder-style)
- External integrations (Grubhub, Yelp, etc.)
- iOS client (SwiftUI)
- Web client (React/Vue)

## Wireframes & Target Design

### Wireframe Files
Located in `/wireframes/` directory:
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
✅ **Completed**: Multi-user preferences, results filtering, sorting, dual counts
🚧 **In Progress**: Backend API development, Android migration to Retrofit
❌ **Missing**: Welcome screen, restaurant detail screen, photo voting, external integrations

---

## Migration Roadmap

### Phase 1: Backend Development (Current Priority)
See `architecture.md` for detailed implementation plan.

**Goals**:
1. Set up Python FastAPI + PostgreSQL backend
2. Implement `POST /api/vendors/search` endpoint
3. Implement `GET /api/vendors/{id}` and `GET /api/vendors/{id}/items` endpoints
4. Deploy to Railway/Render
5. Seed database with test data (20 vendors × 7 items)

### Phase 2: Android Migration
**Goals**:
1. Remove Room database (`AppDatabase.kt`, `VendorDao.kt`)
2. Add Retrofit networking layer
3. Create Repository pattern for API calls
4. Update SharedViewModel to call API instead of Room
5. Test end-to-end with real backend

### Phase 3: Feature Completion
**Goals**:
1. Welcome/onboarding screen (Wireframe 1)
2. Restaurant detail screen (Wireframe 4)
3. Photo voting system (Tinder-style swipe)
4. External integrations (Grubhub, Yelp, etc.)

### Phase 4: iOS & Web Clients
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

### 2025-10-01: Backend Complete ✅
- **Architecture Planning**:
  - Created `architecture.md` with complete multi-platform specifications
  - Updated `CLAUDE.md` to reflect backend-first strategy
  - Decided on Python FastAPI + PostgreSQL + Railway stack

- **Backend Implementation** (Complete):
  - Built complete FastAPI REST API (`dietprefs-backend/`)
  - Implemented all 4 API endpoints with full functionality
  - Created SQLAlchemy models (Vendor + Item with 33 dietary flags)
  - Implemented complex filtering logic with AND logic in `VendorService`
  - Context-aware rating calculations
  - Haversine distance calculations
  - Sorting (rating/distance/item_count) and pagination
  - Database seeding script (20 vendors × 7 items = 140 items)
  - Created comprehensive deployment docs (Railway/Render/Heroku)
  - Ready for production deployment

- **Rollback Point**: Commit `12032ae` ("distance text to white") - last commit before backend work

### Previous Work (Android)
- Implemented preference filtering logic with AND logic
- Built rating system based on query-relevant items only
- Created dual-user preference handling with StateFlow
- Fixed infinite loading spinner and empty state handling