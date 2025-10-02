# Dietprefs

Multi-platform food vendor & restaurant discovery application. Find restaurants that match your dietary preferences and restrictions.

## Project Structure

```
Dietprefs/
├── android/          # Android app (Jetpack Compose + Kotlin)
├── backend/          # REST API (FastAPI + PostgreSQL)
├── docs/             # Documentation and wireframes
│   ├── CLAUDE.md           # Project context and guidelines
│   ├── architecture.md     # Multi-platform architecture specs
│   ├── BACKEND_SUMMARY.md  # Backend implementation details
│   └── wireframes/         # UI/UX wireframes
└── README.md         # This file
```

## Quick Start

### Backend (FastAPI + PostgreSQL)
```bash
cd backend
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
# See backend/README.md for full setup
```

### Android (Jetpack Compose)
```bash
cd android
./gradlew assembleDebug
# Open in Android Studio
```

## Documentation

- **[Architecture Overview](docs/architecture.md)** - Multi-platform system design
- **[Project Guidelines](docs/CLAUDE.md)** - Development context and roadmap
- **[Backend Details](docs/BACKEND_SUMMARY.md)** - API implementation
- **[Backend Deployment](backend/DEPLOYMENT.md)** - Deployment instructions

## Features

- **Dual-User Support**: Two users with independent dietary preferences
- **Smart Filtering**: AND logic for precise dietary matching
- **Context-Aware Ratings**: Ratings based only on relevant menu items
- **Distance Calculations**: Location-based vendor sorting
- **33 Dietary Flags**: Comprehensive allergen and preference support

## Technology Stack

**Backend**:
- FastAPI (Python)
- PostgreSQL
- SQLAlchemy ORM
- Deployed on Railway

**Android**:
- Jetpack Compose
- Kotlin
- MVVM Architecture
- Retrofit + OkHttp
- Repository Pattern
- StateFlow
- Google Play Services Location

**Planned**:
- iOS (SwiftUI)
- Web (React/Vue)

## Current Status

✅ **Phase 1 Complete**: Backend API deployed and live at https://dietprefs-production.up.railway.app
✅ **Phase 2 Complete**: Android app fully migrated to REST API with smart caching
✅ **Phase 3 Complete**: Location services and 10-mile distance filtering
🚧 **Phase 4 Next**: Feature completion (welcome screen, restaurant details, photo voting)
❌ **iOS and Web clients** not started

**Recent Achievements**:
- Android location services integrated (Google Play Services)
- 10-mile radius filtering in backend
- Real distance calculations with Haversine formula
- Permission request UI with proper callbacks
- Fixed backend bugs (rating calculation, preference filtering)
- All dietary preferences working (vegetarian, vegan, halal, etc.)
- Fast loading with cached location (instant vs 2+ seconds)

See [docs/CLAUDE.md](docs/CLAUDE.md) for detailed status and roadmap.

## Live API

**Production URL**: https://dietprefs-production.up.railway.app
**API Docs**: https://dietprefs-production.up.railway.app/docs
**Health Check**: https://dietprefs-production.up.railway.app/health

## License

Private project - All rights reserved
