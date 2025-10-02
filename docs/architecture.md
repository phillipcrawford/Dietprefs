# Dietprefs - Technical Reference

Quick reference for database schema, API contracts, and data models. Most day-to-day context is in `CLAUDE.md`.

---

## Database Schema (PostgreSQL)

### Vendors Table
```sql
CREATE TABLE vendors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    lat DECIMAL(10, 8) NOT NULL,
    lng DECIMAL(11, 8) NOT NULL,
    address TEXT,
    zipcode INTEGER,
    phone VARCHAR(20),
    website TEXT,
    hours JSONB,

    -- Delivery options
    delivery BOOLEAN DEFAULT false,
    takeout BOOLEAN DEFAULT false,
    grubhub BOOLEAN DEFAULT false,
    doordash BOOLEAN DEFAULT false,
    ubereats BOOLEAN DEFAULT false,
    postmates BOOLEAN DEFAULT false,

    -- Review platforms
    yelp BOOLEAN DEFAULT false,
    google_reviews BOOLEAN DEFAULT false,
    tripadvisor BOOLEAN DEFAULT false,

    custom_by_nature BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vendors_location ON vendors(lat, lng);
```

### Items Table
```sql
CREATE TABLE items (
    id SERIAL PRIMARY KEY,
    vendor_id INTEGER REFERENCES vendors(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2),
    pictures TEXT,  -- comma-separated URLs

    -- Dietary preferences (10 flags)
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

    -- Meat types (6 flags)
    beef BOOLEAN DEFAULT false,
    chicken BOOLEAN DEFAULT false,
    pork BOOLEAN DEFAULT false,
    seafood BOOLEAN DEFAULT false,
    no_pork_products BOOLEAN DEFAULT false,
    no_red_meat BOOLEAN DEFAULT false,

    -- Allergens (11 flags)
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

    -- Nutritional (3 flags)
    low_sugar BOOLEAN DEFAULT false,
    high_protein BOOLEAN DEFAULT false,
    low_carb BOOLEAN DEFAULT false,

    -- Classification (2 flags)
    entree BOOLEAN DEFAULT false,
    sweet BOOLEAN DEFAULT false,

    -- Rating system
    upvotes INTEGER DEFAULT 0,
    total_votes INTEGER DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_items_vendor_id ON items(vendor_id);
```

**Total**: 33 dietary flags per item

---

## API Endpoints

**Base URL**: `https://dietprefs-production.up.railway.app`

### Search Vendors
```
POST /api/v1/vendors/search
```

**Request**:
```json
{
  "user1_preferences": ["vegetarian", "gluten_free"],
  "user2_preferences": ["keto"],
  "lat": 37.7749,
  "lng": -122.4194,
  "sort_by": "rating",           // "rating" | "distance" | "item_count"
  "sort_direction": "desc",      // "asc" | "desc"
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
      "delivery_options": { /* ... */ }
    }
  ],
  "pagination": {
    "page": 1,
    "total_results": 47,
    "total_pages": 5
  }
}
```

### Get Vendor Details
```
GET /api/v1/vendors/{id}
```

### Get Vendor Items
```
GET /api/v1/vendors/{id}/items?user1_preferences=vegetarian&user2_preferences=keto
```

### Vote on Item
```
POST /api/v1/items/{id}/vote
Body: { "vote": "up" }  // "up" or "down"
```

---

## Preference Field Mapping

Python API uses snake_case, Android uses camelCase:

| API Name (snake_case) | Android (camelCase) | Category |
|----------------------|---------------------|----------|
| `vegetarian` | `VEGETARIAN` | Dietary |
| `gluten_free` | `GLUTEN_FREE` | Allergen |
| `no_peanuts` | `NO_PEANUTS` | Allergen |
| `keto` | `KETO` | Dietary |
| ... (33 total) | ... | ... |

See `backend/app/services/vendor_service.py:PREFERENCE_FIELD_MAP` for complete list.

---

## Key Algorithms

### Context-Aware Rating
```python
# Only items matching the query are used for rating
relevant_items = items_matching_user1_or_user2_prefs
rating = sum(item.upvotes) / sum(item.total_votes)
```

### Distance Calculation (Haversine)
```python
def calculate_distance(lat1, lng1, lat2, lng2):
    R = 3959.0  # Earth radius in miles
    # ... Haversine formula
    return distance_miles
```

### Filtering Logic
```python
# SQL: Vendor must have items matching preferences
query = query.join(Item).filter(
    and_(Item.vegetarian == True, Item.gluten_free == True)
).distinct()

# Python: Count matches per user separately
for item in vendor.items:
    if matches_all_prefs(item, user1_prefs):
        user1_matches += 1
    if matches_all_prefs(item, user2_prefs):
        user2_matches += 1
```

---

## Performance Notes

**Current Optimizations** (Oct 2025):
- Eager loading with `selectinload(Vendor.items)` prevents N+1 queries
- SQL WHERE clauses filter vendors before loading (not in Python)
- Distance bounding box in SQL reduces candidates by ~75%

**Query Count**:
- Before: 21 queries (1 + 20 for items)
- After: 2-3 queries total

**Acceptable In-Memory Operations**:
- Sorting 10-50 vendors by rating (microseconds)
- Pagination with array slicing (nanoseconds)
- Per-user item counting (milliseconds)

---

## Test Data

**Current Seed** (`backend/app/seed.py`):
- 20 vendors total
- 15 within 10 miles of SF (37.7749, -122.4194)
- 5 beyond 10 miles (filtered out by distance)
- 7 items per vendor = 140 total items
- Most items have vegetarian flag (test data bias)

**Need**: More specialized vendors and 1000+ vendor dataset for realistic testing

---

**Last Updated**: 2025-10-02
