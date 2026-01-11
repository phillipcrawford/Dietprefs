from typing import List, Optional, Tuple
from sqlalchemy.orm import Session, selectinload
from sqlalchemy import or_, and_
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
        # Fetch vendors with SQL filters applied
        vendors = VendorService._fetch_filtered_vendors(db, request)

        # Process vendors into response objects
        vendor_responses = VendorService._process_vendors(vendors, request)

        # Sort results
        sorted_vendors = VendorService._sort_vendors(vendor_responses, request)

        # Paginate
        paginated_vendors = VendorService._paginate_results(sorted_vendors, request)

        return paginated_vendors, len(sorted_vendors)

    @staticmethod
    def _fetch_filtered_vendors(db: Session, request: VendorSearchRequest) -> List[Vendor]:
        """
        Build and execute SQL query with all filters applied.
        Returns list of Vendor models with items eagerly loaded.
        """
        # Build base query with eager loading
        query = db.query(Vendor).options(selectinload(Vendor.items))

        # Apply distance filter (bounding box)
        query = VendorService._apply_distance_filter(query, request)

        # Apply vendor-level filters (delivery, cuisine, etc.)
        query = VendorService._apply_vendor_filters(query, request)

        # Apply search query and preference filters
        query = VendorService._apply_search_and_preference_filters(query, request)

        # Execute query
        vendors = query.all()

        # Apply "open" filter (post-query, requires time-based logic)
        if request.vendor_filters and "open" in [f.lower().strip() for f in request.vendor_filters]:
            open_vendor_ids = VendorService.filter_open_vendors(vendors)
            vendors = [v for v in vendors if v.id in open_vendor_ids]

        return vendors

    @staticmethod
    def _apply_distance_filter(query, request: VendorSearchRequest):
        """Apply distance bounding box filter to query."""
        if request.lat is not None and request.lng is not None:
            lat_delta, lng_delta = DistanceService.get_bounding_box_deltas(request.lat)

            query = query.filter(
                Vendor.lat.between(request.lat - lat_delta, request.lat + lat_delta),
                Vendor.lng.between(request.lng - lng_delta, request.lng + lng_delta)
            )

        return query

    @staticmethod
    def _apply_vendor_filters(query, request: VendorSearchRequest):
        """Apply vendor-level filters (delivery, takeout, cuisine, fusion)."""
        if not request.vendor_filters:
            return query

        filter_conditions = []

        for filter_name in request.vendor_filters:
            filter_lower = filter_name.lower().strip()

            if filter_lower == "delivery":
                filter_conditions.append(Vendor.delivery == True)
            elif filter_lower == "takeout":
                filter_conditions.append(Vendor.takeout == True)
            elif filter_lower == "fusion":
                filter_conditions.append(Vendor.fusion == True)
            elif filter_lower == "usa":
                filter_conditions.append(Vendor.cuisine_usa == True)
            elif filter_lower == "europe":
                filter_conditions.append(Vendor.cuisine_europe == True)
            elif filter_lower == "north_africa_middle_east":
                filter_conditions.append(Vendor.cuisine_north_africa_middle_east == True)
            elif filter_lower == "mexico_south_america":
                filter_conditions.append(Vendor.cuisine_mexico_south_america == True)
            elif filter_lower == "sub_saharan_africa":
                filter_conditions.append(Vendor.cuisine_sub_saharan_africa == True)
            elif filter_lower == "east_asia":
                filter_conditions.append(Vendor.cuisine_east_asia == True)
            # Note: "open" filter handled post-query in _fetch_filtered_vendors

        # Apply all vendor filters with AND logic
        if filter_conditions:
            query = query.filter(*filter_conditions)

        return query

    @staticmethod
    def _apply_search_and_preference_filters(query, request: VendorSearchRequest):
        """
        Apply text search and preference filters.
        Joins Item table if needed and applies filters with correct logic.
        """
        user1_prefs = request.user1_preferences
        user2_prefs = request.user2_preferences
        is_user1_active = len(user1_prefs) > 0
        is_user2_active = len(user2_prefs) > 0
        user1_has_price = request.user1_max_price is not None
        user2_has_price = request.user2_max_price is not None
        has_search_query = bool(request.search_query and request.search_query.strip())

        # Determine if we need to join with Item table
        needs_preference_filter = is_user1_active or is_user2_active or user1_has_price or user2_has_price
        needs_item_join = has_search_query or needs_preference_filter

        if not needs_item_join:
            return query

        # Build text search filter (vendor fields OR item names)
        search_filter = None
        if has_search_query:
            search_pattern = f"%{request.search_query.strip()}%"
            search_filter = or_(
                Vendor.name.ilike(search_pattern),
                Vendor.address.ilike(search_pattern),
                Vendor.seo_tags.ilike(search_pattern),
                Item.name.ilike(search_pattern)
            )

        # Build preference filter
        preference_filter = None
        if needs_preference_filter:
            user1_filter = FilterService.build_preference_filter(user1_prefs, request.user1_max_price)
            user2_filter = FilterService.build_preference_filter(user2_prefs, request.user2_max_price)

            # Combine with OR: vendor must have items matching user1 OR user2
            if user1_filter is not None and user2_filter is not None:
                preference_filter = or_(user1_filter, user2_filter)
            elif user1_filter is not None:
                preference_filter = user1_filter
            else:
                preference_filter = user2_filter

        # Join with items and apply filters
        query = query.join(Item)

        # Apply filters (both must match if both exist)
        if search_filter is not None:
            query = query.filter(search_filter)
        if preference_filter is not None:
            query = query.filter(preference_filter)

        query = query.distinct()

        return query

    @staticmethod
    def _process_vendors(vendors: List[Vendor], request: VendorSearchRequest) -> List[VendorResponse]:
        """
        Process vendors into VendorResponse objects.
        Filters items, calculates ratings, and builds response objects.
        """
        processed_vendors = []

        for vendor in vendors:
            vendor_response = VendorService._process_single_vendor(vendor, request)
            if vendor_response:  # None if vendor doesn't meet criteria
                processed_vendors.append(vendor_response)

        return processed_vendors

    @staticmethod
    def _process_single_vendor(vendor: Vendor, request: VendorSearchRequest) -> Optional[VendorResponse]:
        """
        Process a single vendor into a VendorResponse.
        Returns None if vendor doesn't meet dual-user criteria.
        """
        # Filter items for each user
        user1_matching_items, user2_matching_items = VendorService._filter_vendor_items(vendor, request)

        # Check dual-user criteria
        if not VendorService._meets_dual_user_criteria(user1_matching_items, user2_matching_items, request):
            return None

        # Determine relevant items for rating calculation
        relevant_items = VendorService._get_relevant_items(
            user1_matching_items, user2_matching_items, vendor.items, request
        )

        # Skip vendor if no relevant items (when filters are active)
        user1_prefs = request.user1_preferences
        user2_prefs = request.user2_preferences
        is_user1_active = len(user1_prefs) > 0
        is_user2_active = len(user2_prefs) > 0
        user1_has_price = request.user1_max_price is not None
        user2_has_price = request.user2_max_price is not None

        if (is_user1_active or is_user2_active or user1_has_price or user2_has_price) and len(relevant_items) == 0:
            return None

        # Calculate rating
        rating = VendorService._calculate_rating(relevant_items)

        # Calculate distance
        distance_miles = VendorService._calculate_distance(vendor, request)

        # Skip if outside distance range
        if distance_miles is not None and request.lat is not None and request.lng is not None:
            if not DistanceService.is_within_distance(
                request.lat, request.lng, vendor.lat, vendor.lng
            ):
                return None

        # Build response object
        return VendorService._build_vendor_response(
            vendor, rating, distance_miles, user1_matching_items, user2_matching_items, relevant_items
        )

    @staticmethod
    def _filter_vendor_items(vendor: Vendor, request: VendorSearchRequest) -> Tuple[List[Item], List[Item]]:
        """
        Filter vendor's items for each user based on preferences and price.
        Returns tuple of (user1_matching_items, user2_matching_items).
        """
        user1_prefs = request.user1_preferences
        user2_prefs = request.user2_preferences
        is_user1_active = len(user1_prefs) > 0
        is_user2_active = len(user2_prefs) > 0
        user1_has_price = request.user1_max_price is not None
        user2_has_price = request.user2_max_price is not None

        user1_matching_items = []
        user2_matching_items = []

        for item in vendor.items:
            if (is_user1_active or user1_has_price) and FilterService.item_matches_preferences(
                item, user1_prefs, request.user1_max_price
            ):
                user1_matching_items.append(item)

            if (is_user2_active or user2_has_price) and FilterService.item_matches_preferences(
                item, user2_prefs, request.user2_max_price
            ):
                user2_matching_items.append(item)

        return user1_matching_items, user2_matching_items

    @staticmethod
    def _meets_dual_user_criteria(
        user1_items: List[Item],
        user2_items: List[Item],
        request: VendorSearchRequest
    ) -> bool:
        """
        Check if vendor meets dual-user criteria.
        When both users have filters, vendor must have items for BOTH users.
        """
        user1_prefs = request.user1_preferences
        user2_prefs = request.user2_preferences
        is_user1_active = len(user1_prefs) > 0
        is_user2_active = len(user2_prefs) > 0
        user1_has_price = request.user1_max_price is not None
        user2_has_price = request.user2_max_price is not None

        # When both users have filters, vendor must have items for BOTH users
        if (is_user1_active or user1_has_price) and (is_user2_active or user2_has_price):
            if len(user1_items) == 0 or len(user2_items) == 0:
                return False

        return True

    @staticmethod
    def _get_relevant_items(
        user1_items: List[Item],
        user2_items: List[Item],
        all_items: List[Item],
        request: VendorSearchRequest
    ) -> List[Item]:
        """
        Determine which items are relevant for rating calculation.

        Logic:
        - Both users active: Union of both users' items (distinct)
        - User 1 only: User 1's items
        - User 2 only: User 2's items
        - No users: All items
        """
        user1_prefs = request.user1_preferences
        user2_prefs = request.user2_preferences
        is_user1_active = len(user1_prefs) > 0
        is_user2_active = len(user2_prefs) > 0
        user1_has_price = request.user1_max_price is not None
        user2_has_price = request.user2_max_price is not None

        if (is_user1_active or user1_has_price) and (is_user2_active or user2_has_price):
            # Combine both users' matching items (distinct by ID)
            seen_ids = set()
            relevant_items = []
            for item in user1_items + user2_items:
                if item.id not in seen_ids:
                    seen_ids.add(item.id)
                    relevant_items.append(item)
            return relevant_items
        elif is_user1_active or user1_has_price:
            return user1_items
        elif is_user2_active or user2_has_price:
            return user2_items
        else:
            # No preferences or price filters selected, all items are relevant
            return all_items

    @staticmethod
    def _calculate_rating(items: List[Item]) -> VendorRating:
        """Calculate context-aware rating from relevant items."""
        total_upvotes = sum(item.upvotes for item in items)
        total_votes = sum(item.total_votes for item in items)
        rating_percentage = min(total_upvotes / total_votes, 1.0) if total_votes > 0 else 0.0

        return VendorRating(
            upvotes=total_upvotes,
            total_votes=total_votes,
            percentage=rating_percentage
        )

    @staticmethod
    def _calculate_distance(vendor: Vendor, request: VendorSearchRequest) -> Optional[float]:
        """Calculate exact distance if user location provided."""
        if request.lat is not None and request.lng is not None:
            return DistanceService.calculate_distance(
                request.lat, request.lng, vendor.lat, vendor.lng
            )
        return None

    @staticmethod
    def _build_vendor_response(
        vendor: Vendor,
        rating: VendorRating,
        distance_miles: Optional[float],
        user1_items: List[Item],
        user2_items: List[Item],
        relevant_items: List[Item]
    ) -> VendorResponse:
        """Build VendorResponse object from vendor and calculated data."""
        return VendorResponse(
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
            rating=rating,
            item_counts=ItemCounts(
                user1_matches=len(user1_items),
                user2_matches=len(user2_items),
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

    @staticmethod
    def _sort_vendors(vendors: List[VendorResponse], request: VendorSearchRequest) -> List[VendorResponse]:
        """Sort vendors by specified column and direction."""
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
        return sorted(vendors, key=sort_key, reverse=reverse)

    @staticmethod
    def _paginate_results(
        vendors: List[VendorResponse],
        request: VendorSearchRequest
    ) -> List[VendorResponse]:
        """Paginate sorted vendor results."""
        start_index = (request.page - 1) * request.page_size
        end_index = start_index + request.page_size
        return vendors[start_index:end_index]

    @staticmethod
    def get_vendor_by_id(db: Session, vendor_id: int) -> Optional[Vendor]:
        """Get a single vendor by ID."""
        return db.query(Vendor).filter(Vendor.id == vendor_id).first()

    @staticmethod
    def get_vendor_items(
        db: Session,
        vendor_id: int,
        user1_preferences: Optional[List[str]] = None,
        user2_preferences: Optional[List[str]] = None,
        user1_max_price: Optional[float] = None,
        user2_max_price: Optional[float] = None
    ) -> List[Item]:
        """
        Get all items for a vendor, optionally filtered by preferences and price.
        """
        items = db.query(Item).filter(Item.vendor_id == vendor_id).all()

        # Check if any filters are active for each user
        user1_active = bool(user1_preferences or user1_max_price is not None)
        user2_active = bool(user2_preferences or user2_max_price is not None)

        # If no filters at all, return all items
        if not user1_active and not user2_active:
            return items

        # Filter items based on preferences and price
        filtered_items = []
        for item in items:
            # Only check matching for active users
            matches_user1 = False
            matches_user2 = False

            if user1_active:
                matches_user1 = FilterService.item_matches_preferences(
                    item, user1_preferences or [], user1_max_price
                )

            if user2_active:
                matches_user2 = FilterService.item_matches_preferences(
                    item, user2_preferences or [], user2_max_price
                )

            # Include item if it matches at least one ACTIVE user's filters
            if matches_user1 or matches_user2:
                # Attach metadata for client
                item.matches_user1 = matches_user1
                item.matches_user2 = matches_user2
                filtered_items.append(item)

        return filtered_items

    @staticmethod
    def filter_open_vendors(vendors: List[Vendor]) -> List[int]:
        """
        Filter vendors that are currently open based on hours JSON field.

        Hours format: {"monday": "11:00-22:00", "tuesday": "11:00-22:00", ...}
        Returns list of vendor IDs that are currently open.
        """
        from datetime import datetime
        import json

        # Get current time in server timezone
        now = datetime.now()
        current_day = now.strftime("%A").lower()  # "monday", "tuesday", etc.
        current_time = now.strftime("%H:%M")  # "14:30"

        open_vendor_ids = []

        for vendor in vendors:
            if vendor.hours:
                try:
                    hours_dict = json.loads(vendor.hours)
                    today_hours = hours_dict.get(current_day)

                    if today_hours and today_hours != "closed":
                        # Parse "11:00-22:00" format
                        open_time, close_time = today_hours.split("-")
                        if open_time <= current_time <= close_time:
                            open_vendor_ids.append(vendor.id)
                except (json.JSONDecodeError, ValueError, AttributeError):
                    # Skip vendors with invalid hours format
                    continue

        return open_vendor_ids
