# Dietprefs - Food Vendor & Restaurant Discovery App

## Overview
Android app for discovering food vendors and restaurants based on dietary preferences and restrictions. Helps users find food that matches their specific dietary needs with filtering, rating, and sorting capabilities.

## Architecture
- **Platform**: Android with Jetpack Compose
- **Database**: Room (local SQLite)
- **Architecture Pattern**: MVVM with SharedViewModel
- **Navigation**: Jetpack Navigation Compose
- **Language**: Kotlin

## Key Data Entities

### VendorEntity (`data/VendorEntity.kt`)
Represents restaurants/vendors with:
- Location data (lat, lng, address, zipcode)
- Delivery options (delivery, takeout, grubhub, doordash, ubereats, etc.)
- Business info (name, phone, website, hours, reviews)
- Custom dietary compliance (`customByNature`)

### ItemEntity (`data/ItemEntity.kt`)
Menu items with extensive dietary/allergen flags:
- **Dietary Preferences**: vegetarian, pescetarian, vegan, keto, organic, gmoFree, locallySourced, raw, kosher, halal
- **Allergens**: noMilk, noEggs, noFish, noShellfish, noPeanuts, noTreenuts, glutenFree, noSoy, noSesame, noMsg, noAlliums
- **Meat Types**: beef, chicken, pork, seafood, noPorkProducts, noRedMeat
- **Nutritional**: lowSugar, highProtein, lowCarb
- **Classification**: entree, sweet
- **Social**: upvotes, totalVotes for rating system
- **Media**: pictures (comma-separated filenames)

## Core Functionality

### SharedViewModel (`viewmodel/SharedViewModel.kt`)
Central state management for:
- **Dual User Preferences**: Supports two users with independent dietary preference sets
- **Dynamic Filtering**: Real-time filtering of vendors/items based on selected preferences
- **Rating System**: Query-specific ratings based on relevant items only
- **Sorting**: By vendor rating, distance, or menu item count
- **Pagination**: Load results in pages of 10
- **State Management**: Uses StateFlow for reactive UI updates

### Key Features
1. **Multi-User Support**: Two users can set independent dietary preferences
2. **Smart Filtering**: Vendors shown only if they have items matching active preferences
3. **Context-Aware Ratings**: Ratings calculated only from items relevant to current query
4. **Distance Calculation**: Location-based vendor sorting (placeholder implementation)
5. **Comprehensive Dietary Support**: 25+ dietary flags covering allergies, preferences, and restrictions

## Current State
- Basic data models and ViewModel implemented
- Room database schema defined
- Pagination system with StateFlow
- Extensive logging for debugging preference matching
- UI screens for preferences and search results (basic implementation)

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
🚧 **In Progress**: Search functionality (placeholder), filter tags
❌ **Missing**: Welcome screen, restaurant detail screen, photo voting, external integrations

## Recent Work Context
- Focus on preference filtering logic and debugging
- Rating system based on relevant items only
- Multi-user preference handling with StateFlow
- Performance optimization for large vendor datasets
- Fixed infinite loading spinner and empty state handling
- Removed dead code and unused parameters