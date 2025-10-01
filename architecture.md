# Dietprefs - Multi-Platform Architecture

## Overview
Dietprefs is a food vendor & restaurant discovery application with **Android, iOS, and Web clients** sharing a common backend API and database. This architecture enables consistent functionality across all platforms while allowing platform-specific UI implementations.

---

## Architecture Pattern

### **Client-Server with Shared Backend**

```
┌─────────────────────────────────────────────────┐
│                   Clients                       │
├─────────────┬─────────────┬────────────────────┤
│   Android   │     iOS     │        Web         │
│  (Compose)  │  (SwiftUI)  │  (React/Vue/...)   │
└──────┬──────┴──────┬──────┴─────────┬──────────┘
       │             │                │
       └─────────────┼────────────────┘
                     │ HTTPS/REST
                     ▼
         ┌───────────────────────┐
         │     Backend API       │
         │  (Node.js/Python/Go)  │
         └───────────┬───────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │   PostgreSQL / MySQL  │
         │   Shared Database     │
         └───────────────────────┘
```

---

## Technology Stack Options

### **Option A: Node.js + PostgreSQL** (Recommended for JavaScript developers)
- **Backend**: Node.js + Express.js
- **Database**: PostgreSQL
- **ORM**: Prisma or Sequelize
- **Hosting**: Railway, Render, or Heroku
- **Pros**: Large ecosystem, great for real-time features, easy deployment
- **Cons**: Less type-safe than other options

### **Option B: Python + PostgreSQL** (Recommended for Python developers)
- **Backend**: Python + FastAPI
- **Database**: PostgreSQL
- **ORM**: SQLAlchemy or Django ORM
- **Hosting**: Railway, Render, or Google Cloud Run
- **Pros**: Excellent type safety, automatic API docs, fast development
- **Cons**: Slightly slower than Node.js for I/O-heavy operations

### **Option C: Supabase** (Fastest to MVP)
- **Backend**: Supabase (PostgreSQL + auto-generated REST API)
- **Database**: PostgreSQL (hosted by Supabase)
- **Auth**: Built-in Supabase Auth
- **Hosting**: Supabase cloud (free tier available)
- **Pros**: Instant API, built-in auth, real-time subscriptions, very fast setup
- **Cons**: Less control over API logic, vendor lock-in

### **Option D: Firebase** (Alternative managed solution)
- **Backend**: Firebase Cloud Functions
- **Database**: Cloud Firestore
- **Hosting**: Firebase Hosting
- **Pros**: Real-time sync, great mobile SDK, generous free tier
- **Cons**: NoSQL requires data denormalization, vendor lock-in

---

## Recommended Choice: **Option B (Python + FastAPI + PostgreSQL)**

**Rationale**:
- Type-safe API matches Kotlin's type system
- Automatic OpenAPI/Swagger documentation
- Fast development with async support
- Easy to host and scale
- No vendor lock-in
- Excellent for complex filtering logic

---

## Data Models

### **Vendor Model**
```python
class Vendor:
    id: int (Primary Key)
    name: str
    lat: float
    lng: float
    region: int
    address: str
    zipcode: int
    phone: str
    website: str
    hours: str  # JSON string
    seo_tags: str  # comma-separated

    # Delivery options
    delivery: bool
    takeout: bool
    grubhub: bool
    doordash: bool
    ubereats: bool
    postmates: bool

    # Review integrations
    yelp: bool
    google_reviews: bool
    tripadvisor: bool

    # Special attributes
    custom_by_nature: bool

    created_at: datetime
    updated_at: datetime
```

### **Item Model**
```python
class Item:
    id: int (Primary Key)
    vendor_id: int (Foreign Key -> Vendor.id)
    name: str
    price: float
    pictures: str  # comma-separated URLs

    # Dietary preferences (25+ Boolean fields)
    vegetarian: bool
    pescetarian: bool
    vegan: bool
    keto: bool
    organic: bool
    gmo_free: bool
    locally_sourced: bool
    raw: bool
    kosher: bool
    halal: bool

    # Meat types
    beef: bool
    chicken: bool
    pork: bool
    seafood: bool
    no_pork_products: bool
    no_red_meat: bool

    # Allergens
    no_milk: bool
    no_eggs: bool
    no_fish: bool
    no_shellfish: bool
    no_peanuts: bool
    no_treenuts: bool
    gluten_free: bool
    no_soy: bool
    no_sesame: bool
    no_msg: bool
    no_alliums: bool

    # Nutritional
    low_sugar: bool
    high_protein: bool
    low_carb: bool

    # Classification
    entree: bool
    sweet: bool

    # Rating system
    upvotes: int
    total_votes: int

    created_at: datetime
    updated_at: datetime
```

### **Additional Models** (Future)
```python
class User:
    id: int
    email: str
    saved_preferences: JSON  # Store user's dietary preferences
    created_at: datetime

class SavedVendor:
    user_id: int (FK)
    vendor_id: int (FK)
    created_at: datetime
```

---

## API Endpoints

### **Base URL**: `https://api.dietprefs.com` (or your domain)

### **Vendor Endpoints**

#### `POST /api/vendors/search`
Search and filter vendors by dietary preferences.

**Request Body**:
```json
{
  "user1_preferences": ["vegetarian", "gluten_free"],
  "user2_preferences": ["keto", "no_dairy"],
  "lat": 37.7749,
  "lng": -122.4194,
  "sort_by": "rating",  // "rating" | "distance" | "item_count"
  "sort_direction": "desc",  // "asc" | "desc"
  "page": 1,
  "page_size": 10
}
```

**Response**:
```json
{
  "vendors": [
    {
      "id": 1,
      "name": "Healthy Eats",
      "distance_miles": 1.2,
      "address": "123 Main St",
      "rating": {
        "upvotes": 45,
        "total_votes": 50,
        "percentage": 0.9
      },
      "item_counts": {
        "user1_matches": 12,
        "user2_matches": 8,
        "total_relevant": 15
      },
      "delivery_options": {
        "delivery": true,
        "takeout": true,
        "grubhub": true,
        "doordash": false
      }
    }
  ],
  "pagination": {
    "page": 1,
    "page_size": 10,
    "total_results": 47,
    "total_pages": 5
  }
}
```

#### `GET /api/vendors/{id}`
Get detailed vendor information.

**Response**:
```json
{
  "id": 1,
  "name": "Healthy Eats",
  "lat": 37.7749,
  "lng": -122.4194,
  "address": "123 Main St",
  "zipcode": 94102,
  "phone": "4155551234",
  "website": "https://healthyeats.com",
  "hours": {
    "monday": "10:00-22:00",
    "tuesday": "10:00-22:00"
  },
  "delivery_options": { /* ... */ },
  "review_links": {
    "yelp": true,
    "google_reviews": true,
    "tripadvisor": false
  }
}
```

#### `GET /api/vendors/{id}/items`
Get all menu items for a vendor with optional filtering.

**Query Parameters**:
- `preferences`: Comma-separated list of dietary preferences
- `user1_preferences`: Preferences for user 1
- `user2_preferences`: Preferences for user 2

**Response**:
```json
{
  "items": [
    {
      "id": 1,
      "name": "Veggie Burger",
      "price": 12.99,
      "pictures": ["url1.jpg", "url2.jpg"],
      "dietary_flags": {
        "vegetarian": true,
        "vegan": false,
        "gluten_free": true,
        /* ... all other flags ... */
      },
      "rating": {
        "upvotes": 25,
        "total_votes": 30
      },
      "matches_user1": true,
      "matches_user2": false
    }
  ]
}
```

### **Item Endpoints**

#### `POST /api/items/{id}/vote`
Vote on a menu item (upvote/downvote).

**Request Body**:
```json
{
  "vote": "up"  // "up" | "down"
}
```

**Response**:
```json
{
  "item_id": 1,
  "upvotes": 26,
  "total_votes": 31,
  "rating_percentage": 0.84
}
```

### **Image Endpoints** (Future)

#### `POST /api/items/{id}/images`
Upload item images.

#### `GET /api/images/{filename}`
Retrieve item images.

---

## Backend Implementation Plan

### **Phase 1: Core Infrastructure**
1. Set up FastAPI project structure
2. Configure PostgreSQL database connection
3. Create database migrations (Alembic)
4. Set up SQLAlchemy models
5. Create seed data script (20 vendors × 7 items)
6. Deploy to Railway/Render

### **Phase 2: Search & Filter API**
1. Implement `POST /api/vendors/search` endpoint
2. Build filtering logic for dietary preferences
3. Implement context-aware rating calculation
4. Add sorting (rating, distance, item count)
5. Add pagination
6. Distance calculation using lat/lng (Haversine formula)

### **Phase 3: Vendor Details API**
1. Implement `GET /api/vendors/{id}`
2. Implement `GET /api/vendors/{id}/items`
3. Add item filtering for dual-user mode

### **Phase 4: Voting System**
1. Implement `POST /api/items/{id}/vote`
2. Update rating calculations in real-time

### **Phase 5: Advanced Features** (Future)
1. User authentication (JWT)
2. Saved vendors/preferences
3. Image uploads (S3 or Cloudinary)
4. Real-time updates (WebSockets)

---

## Client Architecture Changes

### **Android App Migration**

#### **Remove**:
- `data/AppDatabase.kt` (Room database)
- `data/VendorDao.kt` (DAO layer)
- Room dependencies from `build.gradle`

#### **Add**:
```
data/
  ├── api/
  │   ├── ApiService.kt          # Retrofit interface
  │   ├── RetrofitClient.kt      # Retrofit setup
  │   └── models/
  │       ├── VendorResponse.kt
  │       ├── SearchRequest.kt
  │       └── ...
  ├── repository/
  │   └── VendorRepository.kt    # Mediates between API and ViewModel
  └── models/                    # Keep existing entities
      ├── VendorEntity.kt        # Now represents API response
      └── ItemEntity.kt
```

#### **Update**:
- `SharedViewModel.kt`: Call API through repository instead of Room
- Add loading/error states for network calls
- Add offline caching strategy (optional)

### **Dependencies to Add**:
```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Coroutines (already present)
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

---

## Database Schema (PostgreSQL)

```sql
CREATE TABLE vendors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    lat DECIMAL(10, 8) NOT NULL,
    lng DECIMAL(11, 8) NOT NULL,
    region INTEGER,
    address TEXT,
    zipcode INTEGER,
    phone VARCHAR(20),
    website TEXT,
    hours JSONB,
    seo_tags TEXT,

    -- Delivery options
    delivery BOOLEAN DEFAULT false,
    takeout BOOLEAN DEFAULT false,
    grubhub BOOLEAN DEFAULT false,
    doordash BOOLEAN DEFAULT false,
    ubereats BOOLEAN DEFAULT false,
    postmates BOOLEAN DEFAULT false,

    -- Reviews
    yelp BOOLEAN DEFAULT false,
    google_reviews BOOLEAN DEFAULT false,
    tripadvisor BOOLEAN DEFAULT false,

    custom_by_nature BOOLEAN DEFAULT false,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE items (
    id SERIAL PRIMARY KEY,
    vendor_id INTEGER REFERENCES vendors(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2),
    pictures TEXT,  -- comma-separated URLs

    -- Dietary preferences
    vegetarian BOOLEAN DEFAULT false,
    pescetarian BOOLEAN DEFAULT false,
    vegan BOOLEAN DEFAULT false,
    keto BOOLEAN DEFAULT false,
    organic BOOLEAN DEFAULT false,
    gmo_free BOOLEAN DEFAULT false,
    locally_sourced BOOLEAN DEFAULT false,
    raw BOOLEAN DEFAULT false,
    kosher BOOLEAN DEFAULT false,
    halal BOOLEAN DEFAULT false,

    -- Meat types
    beef BOOLEAN DEFAULT false,
    chicken BOOLEAN DEFAULT false,
    pork BOOLEAN DEFAULT false,
    seafood BOOLEAN DEFAULT false,
    no_pork_products BOOLEAN DEFAULT false,
    no_red_meat BOOLEAN DEFAULT false,

    -- Allergens
    no_milk BOOLEAN DEFAULT false,
    no_eggs BOOLEAN DEFAULT false,
    no_fish BOOLEAN DEFAULT false,
    no_shellfish BOOLEAN DEFAULT false,
    no_peanuts BOOLEAN DEFAULT false,
    no_treenuts BOOLEAN DEFAULT false,
    gluten_free BOOLEAN DEFAULT false,
    no_soy BOOLEAN DEFAULT false,
    no_sesame BOOLEAN DEFAULT false,
    no_msg BOOLEAN DEFAULT false,
    no_alliums BOOLEAN DEFAULT false,

    -- Nutritional
    low_sugar BOOLEAN DEFAULT false,
    high_protein BOOLEAN DEFAULT false,
    low_carb BOOLEAN DEFAULT false,

    -- Classification
    entree BOOLEAN DEFAULT false,
    sweet BOOLEAN DEFAULT false,

    -- Rating
    upvotes INTEGER DEFAULT 0,
    total_votes INTEGER DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_vendors_location ON vendors(lat, lng);
CREATE INDEX idx_items_vendor_id ON items(vendor_id);
CREATE INDEX idx_items_dietary ON items(vegetarian, vegan, keto, gluten_free);
```

---

## Deployment Strategy

### **Backend Hosting Options**:

1. **Railway** (Recommended)
   - Free tier: $5 credit/month
   - Built-in PostgreSQL
   - GitHub auto-deploy
   - Easy SSL certificates

2. **Render**
   - Free tier available
   - PostgreSQL included
   - Auto-deploy from Git

3. **Heroku**
   - Free tier discontinued, but simple pricing
   - PostgreSQL add-on
   - Well-documented

### **CI/CD Pipeline**:
```
GitHub Push → Automated Tests → Deploy to Railway → Health Check
```

### **Environment Variables**:
```
DATABASE_URL=postgresql://...
SECRET_KEY=...
ENVIRONMENT=production
ALLOWED_ORIGINS=https://app.dietprefs.com,dietprefs://
```

---

## Migration Timeline

### **Week 1-2: Backend Development**
- Day 1-2: Set up FastAPI + PostgreSQL
- Day 3-4: Implement data models & migrations
- Day 5-7: Build search/filter endpoint
- Day 8-10: Build vendor details endpoints
- Day 11-14: Testing & deployment

### **Week 3: Android Migration**
- Day 1-2: Remove Room, add Retrofit
- Day 3-4: Create repository layer
- Day 5-7: Update ViewModel & test

### **Week 4+: iOS/Web Development**
- iOS: SwiftUI + URLSession/Alamofire
- Web: React + Axios or Vue + Fetch API

---

## Security Considerations

1. **API Rate Limiting**: Prevent abuse (100 req/min per IP)
2. **Input Validation**: Validate all request parameters
3. **SQL Injection Prevention**: Use parameterized queries (SQLAlchemy handles this)
4. **CORS**: Whitelist only your domains
5. **HTTPS Only**: Enforce SSL/TLS
6. **Authentication**: JWT tokens for user-specific features (Phase 2)

---

## Performance Optimizations

1. **Database Indexing**: On location, dietary flags, vendor_id
2. **Caching**: Redis for frequently accessed vendor data
3. **Pagination**: Always paginate results (10-20 per page)
4. **Database Connection Pooling**: Reuse connections
5. **CDN**: CloudFlare for static assets and images

---

## Testing Strategy

### **Backend Tests**:
- Unit tests for filtering logic
- Integration tests for API endpoints
- Load testing with 1000+ concurrent users

### **Client Tests**:
- Unit tests for ViewModels
- UI tests for critical flows (preference selection → search)
- API mocking for offline testing

---

## Future Enhancements

1. **Machine Learning**: Personalized vendor recommendations
2. **Real-time Sync**: WebSocket for live rating updates
3. **Geofencing**: Push notifications when near matching vendors
4. **Social Features**: Share preferences with friends
5. **Admin Dashboard**: Web interface for managing vendors/items (Wireframe 5)
6. **Analytics**: Track popular preferences and vendors

---

## Questions to Resolve

1. **Backend Framework**: Python FastAPI, Node.js Express, or Supabase?
2. **Hosting Provider**: Railway, Render, or Heroku?
3. **Image Storage**: Local storage, S3, or Cloudinary?
4. **Authentication**: Implement now or later?
5. **Android Testing**: Keep Room temporarily for testing, or wait for backend?

---

## Next Steps

1. **Choose backend technology stack** (see options above)
2. **Set up backend project repository**
3. **Implement core API endpoints**
4. **Deploy to staging environment**
5. **Migrate Android app to use API**
6. **Test end-to-end functionality**
7. **Begin iOS/Web development**

---

**Last Updated**: 2025-10-01
**Status**: Architecture Planning Phase
