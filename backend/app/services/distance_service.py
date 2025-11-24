from typing import Optional
from app.config import settings
import math


class DistanceService:
    """Service for distance calculations and filtering."""

    EARTH_RADIUS_MILES = 3959.0

    @staticmethod
    def calculate_distance(lat1: float, lng1: float, lat2: float, lng2: float) -> float:
        """
        Calculate distance between two coordinates using Haversine formula.
        Returns distance in miles.
        """
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
        distance = DistanceService.EARTH_RADIUS_MILES * c

        return round(distance, 2)

    @staticmethod
    def get_bounding_box_deltas(lat: float, max_distance: Optional[float] = None) -> tuple[float, float]:
        """
        Calculate lat/lng deltas for bounding box filter.
        Used to pre-filter vendors before exact distance calculation.

        Args:
            lat: Latitude of center point
            max_distance: Maximum distance in miles (defaults to config value)

        Returns:
            Tuple of (lat_delta, lng_delta) in degrees
        """
        if max_distance is None:
            max_distance = settings.MAX_DISTANCE_MILES

        lat_delta = max_distance / 69.0  # Approximately 69 miles per degree latitude
        lng_delta = max_distance / (69.0 * math.cos(math.radians(lat)))

        return lat_delta, lng_delta

    @staticmethod
    def is_within_distance(
        lat1: float,
        lng1: float,
        lat2: float,
        lng2: float,
        max_distance: Optional[float] = None
    ) -> bool:
        """
        Check if two coordinates are within max_distance of each other.

        Args:
            lat1, lng1: First coordinate
            lat2, lng2: Second coordinate
            max_distance: Maximum distance in miles (defaults to config value)

        Returns:
            True if distance is within max_distance, False otherwise
        """
        if max_distance is None:
            max_distance = settings.MAX_DISTANCE_MILES

        distance = DistanceService.calculate_distance(lat1, lng1, lat2, lng2)
        return distance <= max_distance
