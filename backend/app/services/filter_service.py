from typing import List, Optional
from sqlalchemy import and_
from app.models.item import Item


class FilterService:
    """Service for filtering items by dietary preferences and price."""

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
    def item_matches_preferences(
        item: Item,
        preferences: List[str],
        max_price: Optional[float] = None
    ) -> bool:
        """
        Check if an item matches ALL given preferences (AND logic) and price constraint.
        Returns True if all preferences match or if preferences list is empty.

        Args:
            item: Item model instance to check
            preferences: List of preference names (e.g., ["vegetarian", "gluten_free"])
            max_price: Optional maximum price filter

        Returns:
            True if item matches all criteria, False otherwise
        """
        # Check price constraint first
        if max_price is not None and item.price is not None:
            if item.price > max_price:
                return False

        if not preferences:
            return True

        for pref in preferences:
            field_name = FilterService.PREFERENCE_FIELD_MAP.get(pref.lower())
            if field_name is None:
                # Unknown preference, skip it
                continue

            if not getattr(item, field_name, False):
                # If ANY preference doesn't match, return False
                return False

        return True

    @staticmethod
    def build_preference_filter(
        preferences: List[str],
        max_price: Optional[float] = None
    ):
        """
        Build SQL filter for items matching ALL preferences (AND logic) and price constraint.

        Args:
            preferences: List of preference names
            max_price: Optional maximum price filter

        Returns:
            SQLAlchemy filter expression, or None if no filters
        """
        filters = []

        # Add price filter if provided
        if max_price is not None:
            filters.append(Item.price <= max_price)

        # Add preference filters
        for pref in preferences:
            field_name = FilterService.PREFERENCE_FIELD_MAP.get(pref.lower())
            if field_name:
                filters.append(getattr(Item, field_name) == True)

        return and_(*filters) if filters else None
