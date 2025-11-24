from typing import List, Optional, Tuple
from sqlalchemy.orm import Session, selectinload
from sqlalchemy import or_
from app.models.vendor import Vendor
from app.models.item import Item
from app.schemas.vendor import VendorSearchRequest, VendorResponse, VendorRating, ItemCounts, DeliveryOptions
from app.config import settings
from app.services.distance_service import DistanceService
from app.services.filter_service import FilterService


class VendorService:
    """Business logic for vendor search and filtering."""

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
        user1_prefs = request.user1_preferences
        user2_prefs = request.user2_preferences
        is_user1_active = len(user1_prefs) > 0
        is_user2_active = len(user2_prefs) > 0

        # Build base query with eager loading
        query = db.query(Vendor).options(selectinload(Vendor.items))

        # Apply distance bounding box filter BEFORE loading vendors (if location provided)
        if request.lat is not None and request.lng is not None:
            lat_delta, lng_delta = DistanceService.get_bounding_box_deltas(request.lat)

            query = query.filter(
                Vendor.lat.between(request.lat - lat_delta, request.lat + lat_delta),
                Vendor.lng.between(request.lng - lng_delta, request.lng + lng_delta)
            )

        # Check if price filters or preferences are active
        user1_has_price = request.user1_max_price is not None
        user2_has_price = request.user2_max_price is not None

        # If preferences or price filters are active, filter to vendors that have at least one matching item
        if is_user1_active or is_user2_active or user1_has_price or user2_has_price:
            # Build filters for each user (including price)
            user1_filter = FilterService.build_preference_filter(user1_prefs, request.user1_max_price)
            user2_filter = FilterService.build_preference_filter(user2_prefs, request.user2_max_price)

            # Combine with OR: vendor must have items matching user1 OR user2
            if user1_filter is not None and user2_filter is not None:
                combined_filter = or_(user1_filter, user2_filter)
            elif user1_filter is not None:
                combined_filter = user1_filter
            else:
                combined_filter = user2_filter

            # Join with items and filter
            if combined_filter is not None:
                query = query.join(Item).filter(combined_filter).distinct()

        # Execute query to get vendors (with items eagerly loaded)
        vendors = query.all()

        processed_vendors = []

        for vendor in vendors:
            # Filter items for each user
            user1_matching_items = []
            user2_matching_items = []

            for item in vendor.items:
                if (is_user1_active or user1_has_price) and FilterService.item_matches_preferences(item, user1_prefs, request.user1_max_price):
                    user1_matching_items.append(item)
                if (is_user2_active or user2_has_price) and FilterService.item_matches_preferences(item, user2_prefs, request.user2_max_price):
                    user2_matching_items.append(item)

            # When both users have filters, vendor must have items for BOTH users
            if (is_user1_active or user1_has_price) and (is_user2_active or user2_has_price):
                if len(user1_matching_items) == 0 or len(user2_matching_items) == 0:
                    continue

            # Determine relevant items for rating calculation
            if (is_user1_active or user1_has_price) and (is_user2_active or user2_has_price):
                # Combine both users' matching items (distinct by ID)
                seen_ids = set()
                relevant_items = []
                for item in user1_matching_items + user2_matching_items:
                    if item.id not in seen_ids:
                        seen_ids.add(item.id)
                        relevant_items.append(item)
            elif is_user1_active or user1_has_price:
                relevant_items = user1_matching_items
            elif is_user2_active or user2_has_price:
                relevant_items = user2_matching_items
            else:
                # No preferences or price filters selected, all items are relevant
                relevant_items = vendor.items

            # Skip vendor if no relevant items found (when preferences or price filters are active)
            if (is_user1_active or is_user2_active or user1_has_price or user2_has_price) and len(relevant_items) == 0:
                continue

            # Calculate context-aware rating
            total_upvotes = sum(item.upvotes for item in relevant_items)
            total_votes = sum(item.total_votes for item in relevant_items)
            rating_percentage = min(total_upvotes / total_votes, 1.0) if total_votes > 0 else 0.0

            # Calculate exact distance if user location provided
            # (bounding box filter already applied in SQL)
            distance_miles = None
            if request.lat is not None and request.lng is not None:
                distance_miles = DistanceService.calculate_distance(
                    request.lat, request.lng, vendor.lat, vendor.lng
                )

                # Apply exact distance filter (after rough bounding box)
                if not DistanceService.is_within_distance(
                    request.lat, request.lng, vendor.lat, vendor.lng
                ):
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
