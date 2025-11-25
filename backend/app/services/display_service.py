"""
Service for building user-facing display strings.

This service owns the formatting logic for filter displays,
ensuring consistency across all clients (Android, iOS, Web).
"""
from typing import List, Optional


def build_display_text(preferences: List[str], max_price: Optional[float]) -> str:
    """
    Build a unified display string combining categorical and numeric filters.

    Args:
        preferences: List of preference strings (e.g., ["vegetarian", "gluten_free"])
        max_price: Optional maximum price threshold

    Returns:
        Formatted display string (e.g., "vegetarian, gluten-free, under $10")

    Examples:
        >>> build_display_text(["vegetarian"], 10.0)
        'vegetarian, under $10'

        >>> build_display_text(["vegan", "gluten_free"], None)
        'vegan, gluten-free'

        >>> build_display_text([], 15.0)
        'under $15'

        >>> build_display_text([], None)
        ''
    """
    parts = []

    # Add preferences (exclude "low_price" as it's handled separately)
    filtered_prefs = [p for p in preferences if p != "low_price"]
    if filtered_prefs:
        # Convert snake_case to display format with proper spacing
        display_prefs = [_format_preference_display(p) for p in filtered_prefs]
        parts.extend(display_prefs)

    # Add price filter if present
    if max_price is not None:
        parts.append(f"under ${max_price:.0f}")

    return ", ".join(parts)


def _format_preference_display(preference: str) -> str:
    """
    Convert API preference name (snake_case) to display format.

    Args:
        preference: API preference name (e.g., "gmo_free", "no_pork_products")

    Returns:
        Display-friendly format (e.g., "gmo-free", "no pork products")

    Examples:
        >>> _format_preference_display("gmo_free")
        'gmo-free'

        >>> _format_preference_display("no_pork_products")
        'no pork products'

        >>> _format_preference_display("vegetarian")
        'vegetarian'
    """
    # Special cases for hyphenated terms
    if preference == "gmo_free":
        return "gmo-free"
    elif preference == "gluten_free":
        return "gluten-free"
    elif preference == "pork":
        return "bacon/pork/ham"

    # For "no_X" preferences and others, replace underscores with spaces
    return preference.replace("_", " ")
