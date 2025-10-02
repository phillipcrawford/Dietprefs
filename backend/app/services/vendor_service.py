from typing import List, Optional, Tuple
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_
from app.models.vendor import Vendor
from app.models.item import Item
from app.schemas.vendor import VendorSearchRequest, VendorResponse, VendorRating, ItemCounts, DeliveryOptions
import math


class VendorService:
    """Business logic for vendor search and filtering."""

    # Map preference names to Item model fields
    PREFERENCE_FIELD_MAP = {
        "vegetarian": "vegetarian",
        "pescetarian": "pescetarian",
        "vegan": "vegan",
        "keto": "keto",
        "organic": "organic",
        "gmo_free": "gmo_free",
        "locally_sourced": "locally_sourced",
        "raw": "raw",
        "kosher": "kosher",
        "halal": "halal",
        "beef": "beef",
        "chicken": "chicken",
        "pork": "pork",
        "seafood": "seafood",
        "no_pork_products": "no_pork_products",
        "no_red_meat": "no_red_meat",
        "no_milk": "no_milk",
        "no_eggs": "no_eggs",
        "no_fish": "no_fish",
        "no_shellfish": "no_shellfish",
        "no_peanuts": "no_peanuts",
        "no_treenuts": "no_treenuts",
        "gluten_free": "gluten_free",
        "no_soy": "no_soy",
        "no_sesame": "no_sesame",
        "no_msg": "no_msg",
        "no_alliums": "no_alliums",
        "low_sugar": "low_sugar",
        "high_protein": "high_protein",
        "low_carb": "low_carb",
        "entree": "entree",
        "sweet": "sweet",
    }

    @staticmethod
    def calculate_distance(lat1: float, lng1: float, lat2: float, lng2: float) -> float:
        """
        Calculate distance between two coordinates using Haversine formula.
        Returns distance in miles.
        """
        # Radius of Earth in miles
        R = 3959.0

        # Convert to radians
        lat1_rad = math.radians(lat1)
        lat2_rad = math.radians(lat2)
        delta_lat = math.radians(lat2 - lat1)
        delta_lng = math.radians(lng2 - lng1)

        # Haversine formula
        a = (
            math.sin(delta_lat / 2) ** 2
            + math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(delta_lng / 2) ** 2
        )
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        distance = R * c

        return round(distance, 2)

    @staticmethod
    def item_matches_preferences(item: Item, preferences: List[str]) -> bool:
        """
        Check if an item matches ALL given preferences (AND logic).
        Returns True if all preferences match or if preferences list is empty.
        """
        if not preferences:
            return True

        for pref in preferences:
            field_name = VendorService.PREFERENCE_FIELD_MAP.get(pref.lower())
            if field_name is None:
                # Unknown preference, skip it
                continue

            if not getattr(item, field_name, False):
                # If ANY preference doesn't match, return False
                return False

        return True

    @staticmethod
    def search_vendors(
        db: Session,
        request: VendorSearchRequest
    ) -> Tuple[List[VendorResponse], int]:
        """
        Search vendors based on dietary preferences with filtering, sorting, and pagination.

        Returns:
            Tuple of (vendor_responses, total_count)
        """
        # Get all vendors with their items
        vendors = db.query(Vendor).all()

        user1_prefs = request.user1_preferences
        user2_prefs = request.user2_preferences
        is_user1_active = len(user1_prefs) > 0
        is_user2_active = len(user2_prefs) > 0

        processed_vendors = []

        for vendor in vendors:
            # Filter items for each user
            user1_matching_items = []
            user2_matching_items = []

            for item in vendor.items:
                if is_user1_active and VendorService.item_matches_preferences(item, user1_prefs):
                    user1_matching_items.append(item)
                if is_user2_active and VendorService.item_matches_preferences(item, user2_prefs):
                    user2_matching_items.append(item)

            # Determine relevant items for rating calculation
            if is_user1_active and is_user2_active:
                # Combine both users' matching items (distinct by ID)
                seen_ids = set()
                relevant_items = []
                for item in user1_matching_items + user2_matching_items:
                    if item.id not in seen_ids:
                        seen_ids.add(item.id)
                        relevant_items.append(item)
            elif is_user1_active:
                relevant_items = user1_matching_items
            elif is_user2_active:
                relevant_items = user2_matching_items
            else:
                # No preferences selected, all items are relevant
                relevant_items = vendor.items

            # Skip vendor if no relevant items found (when preferences are active)
            if (is_user1_active or is_user2_active) and len(relevant_items) == 0:
                continue

            # Calculate context-aware rating
            total_upvotes = sum(item.upvotes for item in relevant_items)
            total_votes = sum(item.total_votes for item in relevant_items)
            rating_percentage = min(total_upvotes / total_votes, 1.0) if total_votes > 0 else 0.0

            # Calculate distance if user location provided
            distance_miles = None
            if request.lat is not None and request.lng is not None:
                distance_miles = VendorService.calculate_distance(
                    request.lat, request.lng, vendor.lat, vendor.lng
                )

                # Filter out vendors beyond 10 miles
                MAX_DISTANCE_MILES = 10.0
                if distance_miles > MAX_DISTANCE_MILES:
                    continue

            # Build response object
            vendor_response = VendorResponse(
                id=vendor.id,
                name=vendor.name,
                lat=vendor.lat,
                lng=vendor.lng,
                address=vendor.address,
                zipcode=vendor.zipcode,
                phone=vendor.phone,
                website=vendor.website,
                hours=vendor.hours,
                seo_tags=vendor.seo_tags,
                region=vendor.region,
                custom_by_nature=vendor.custom_by_nature,
                distance_miles=distance_miles,
                rating=VendorRating(
                    upvotes=total_upvotes,
                    total_votes=total_votes,
                    percentage=rating_percentage
                ),
                item_counts=ItemCounts(
                    user1_matches=len(user1_matching_items),
                    user2_matches=len(user2_matching_items),
                    total_relevant=len(relevant_items)
                ),
                delivery_options=DeliveryOptions(
                    delivery=vendor.delivery,
                    takeout=vendor.takeout,
                    grubhub=vendor.grubhub,
                    doordash=vendor.doordash,
                    ubereats=vendor.ubereats,
                    postmates=vendor.postmates
                )
            )

            processed_vendors.append(vendor_response)

        # Sort vendors
        sort_key = None
        if request.sort_by == "rating":
            sort_key = lambda v: v.rating.percentage
        elif request.sort_by == "distance" and request.lat is not None:
            sort_key = lambda v: v.distance_miles if v.distance_miles is not None else float('inf')
        elif request.sort_by == "item_count":
            sort_key = lambda v: v.item_counts.total_relevant
        else:
            # Default to item_count
            sort_key = lambda v: v.item_counts.total_relevant

        reverse = request.sort_direction.lower() == "desc"
        sorted_vendors = sorted(processed_vendors, key=sort_key, reverse=reverse)

        # Pagination
        total_count = len(sorted_vendors)
        start_index = (request.page - 1) * request.page_size
        end_index = start_index + request.page_size
        paginated_vendors = sorted_vendors[start_index:end_index]

        return paginated_vendors, total_count

    @staticmethod
    def get_vendor_by_id(db: Session, vendor_id: int) -> Optional[Vendor]:
        """Get a single vendor by ID."""
        return db.query(Vendor).filter(Vendor.id == vendor_id).first()

    @staticmethod
    def get_vendor_items(
        db: Session,
        vendor_id: int,
        user1_preferences: Optional[List[str]] = None,
        user2_preferences: Optional[List[str]] = None
    ) -> List[Item]:
        """
        Get all items for a vendor, optionally filtered by preferences.
        """
        items = db.query(Item).filter(Item.vendor_id == vendor_id).all()

        # If no preferences, return all items
        if not user1_preferences and not user2_preferences:
            return items

        # Filter items based on preferences
        filtered_items = []
        for item in items:
            matches_user1 = VendorService.item_matches_preferences(
                item, user1_preferences or []
            )
            matches_user2 = VendorService.item_matches_preferences(
                item, user2_preferences or []
            )

            # Include item if it matches either user's preferences
            if matches_user1 or matches_user2:
                # Attach metadata for client
                item.matches_user1 = matches_user1
                item.matches_user2 = matches_user2
                filtered_items.append(item)

        return filtered_items
