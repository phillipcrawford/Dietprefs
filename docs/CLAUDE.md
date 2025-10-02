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

---

## What's Next (No Specific Order)

**UX Improvements**:
- Welcome/onboarding screen
- Restaurant detail view (menu items, photos, external links)
- Photo voting (Tinder-style swipe)
- Make distance radius user-configurable (currently hardcoded 10mi)

**Backend**:
- Voting endpoint (`POST /api/items/{id}/vote`)
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

**Test Data Quirk**:
- 15 vendors within 10 miles, 5 beyond
- Most vendors have vegetarian items (not realistic but fine for testing)
- Need more specialized seed data for real testing

---

**Last Updated**: 2025-10-02
