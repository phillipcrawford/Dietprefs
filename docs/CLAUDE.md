# Dietprefs - Project Context

**What it is**: Food discovery app that filters restaurants by dietary preferences (vegetarian, vegan, gluten-free, etc.). Supports dual-user mode.

---

## Current Stack

**Backend** (Production at https://dietprefs-production.up.railway.app):
- Python FastAPI + PostgreSQL on Railway
- 20 test vendors, 140 menu items with 33 dietary flags each
- Optimized with SQL filtering and eager loading (Oct 2025)

**Android Client**:
- Jetpack Compose + Retrofit (MVVM)
- Location services (Google Play Services)
- Client-side caching for instant sorting

**Not Yet Built**: iOS, Web

---

## Key Features Working

✅ Dual-user preference selection (User 1 + User 2)
✅ SQL-based filtering (AND logic for preferences)
✅ Context-aware ratings (only from matching items)
✅ Distance filtering (10-mile radius with Haversine)
✅ Sorting (rating, distance, item count)
✅ Location permissions and GPS integration
✅ Restaurant detail screen with photos, voting, and info panel

---

## What's Next (No Specific Order)

**UX Improvements**:
- Welcome/onboarding screen
- Make distance radius user-configurable (currently hardcoded 10mi)

**Backend**:
- Image uploads (S3/Cloudinary integration)
- More realistic seed data (1000+ vendors)

**Future Platforms**:
- iOS app (SwiftUI)
- Web app (React/Vue)

---

## File Locations

**Backend**: `/backend`
- Main logic: `app/services/vendor_service.py`
- API routes: `app/api/v1/`
- Database models: `app/models/`

**Android**: `/android/app/src/main/java/com/example/dietprefs/`
- ViewModel: `viewmodel/SharedViewModel.kt`
- Screens: `ui/screens/`
- API: `network/`, `repository/`

**Docs**: `/docs`
- This file (project context)
- `architecture.md` (detailed specs - rarely needed)
- `wireframes/` (UI designs)

---

## Recent Work (Reverse Chronological)

### 2025-11-17: Price Filtering (In Progress) 🚧
- **Backend Complete**: Added `user1_max_price` and `user2_max_price` to API
- Server-side price filtering in SQL (`WHERE item.price <= max_price`)
- Updated `vendor_service.py` to filter by price alongside other preferences
- **Android Data Layer Complete**: Updated API models, repository, and ViewModel
- **Still TODO**: Price input dialog UI on PreferenceScreen

### 2025-11-17: Test Data Migration to Bozeman ✅
- Updated seed data from San Francisco to Bozeman, MT coordinates
- Changed test restaurant locations, addresses, zipcodes, phone area codes
- Android LocationService now returns Bozeman coordinates for emulator testing
- Reseeded Railway database with new Bozeman-centered data

### 2025-11-17: UI Tweaks ✅
- Fixed text colors in SearchResultsScreen (rating, menu items now white)
- Added grey background to menu items column to match distance column
- Railway deployment reactivated (paid plan after free trial expired)

### 2025-10-03: Restaurant Detail Screen Complete ✅
- Extracted reusable TopBar component used across screens
- Built scrollable stack with restaurant header (60dp) + menu items (48dp)
- Implemented scroll-based selection highlighting (#ffd24d selected, #cc9900 deselected)
- Added photo carousel with Coil image loading (HorizontalPager)
- Implemented voting UI (thumbs up/down) with API integration
- Built external links grid for delivery/review platforms
- Created info panel showing restaurant details (address, phone, hours, rating)
- Full navigation flow: SearchResults → tap vendor → RestaurantDetail

### 2025-10-02: Planning Restaurant Detail Screen 📋
- Analyzed wireframe `Dietprefs (4 Third Page Restaraunt Card).png`
- Created detailed implementation plan in `RESTAURANT_DETAIL_PLAN.md`
- Defined all UI components, API endpoints, and state management needs

### 2025-10-02: Documentation Cleanup 📝
- Simplified CLAUDE.md (18KB → 3.8KB)
- Simplified architecture.md (17KB → 6.1KB)
- Removed phase-based planning (too rigid)
- Added .gitignore for Python/Android artifacts
- Total: 1061 → 389 lines

### 2025-10-02: Backend Performance Optimization ✅
- Fixed N+1 query problem (21 → 2-3 queries)
- Moved filtering to SQL WHERE clauses
- Added distance bounding box filter
- **Result**: 10-100x faster searches at scale

### 2025-10-02: Phase 3 Complete - Location Services ✅
- Android location permissions + GPS
- 10-mile distance filtering
- Fixed rating calculation bugs
- Backend seeded with realistic GPS coordinates

### 2025-10-01: Android Migration to API ✅
- Removed Room database entirely
- Added Retrofit networking + Repository pattern
- Client-side caching for instant sorting
- End-to-end tested with live backend

### 2025-10-01: Backend Deployed ✅
- Built FastAPI REST API with all endpoints
- PostgreSQL database on Railway
- Complex filtering logic with dual-user support
- Swagger UI at `/docs`

---

## Quick Start for New Session

1. **Backend API**: https://dietprefs-production.up.railway.app/docs
2. **Test search**: POST `/api/v1/vendors/search` with preferences
3. **Android app**: Run from `/android` (connects to live API)
4. **Main files to know**:
   - `backend/app/services/vendor_service.py` - Search logic
   - `android/.../SharedViewModel.kt` - Client state management

---

## Architecture Notes

**Filtering Logic**:
- User selects preferences (e.g., "vegetarian + gluten_free")
- Backend filters vendors that have items matching ALL preferences (AND)
- Dual-user mode: vendor must have items for user1 OR user2
- Context-aware ratings: calculated only from matching items

**Performance**:
- SQL does filtering/distance checks
- Python does per-user counting and ratings (acceptable for 10-50 results)
- Sorting/pagination in memory (trivial for small result sets)

**Test Data**:
- Location: Bozeman, MT (45.6770, -111.0429)
- 20 test vendors: 15 within 10 miles, 5 beyond
- Most vendors have vegetarian items (not realistic but fine for testing)
- Need more specialized seed data for real testing

---

**Last Updated**: 2025-11-17
