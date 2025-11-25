"""Configuration endpoint for app-wide settings."""
from fastapi import APIRouter
from app.schemas.config import (
    AppConfig,
    PricingConfig,
    PaginationConfig,
    LocationConfig,
    SortingConfig,
    SortOption
)
from app.config import MAX_DISTANCE_MILES

router = APIRouter()


@router.get("/config", response_model=AppConfig)
async def get_app_config():
    """
    Get application configuration.

    Returns centralized configuration for:
    - Pricing filters (min, max, step, default options)
    - Pagination settings (page sizes)
    - Location settings (default location, max distance)
    - Sorting options (available sorts, defaults)

    This ensures all clients (Android, iOS, Web) use consistent business rules
    without hardcoding values in each client.
    """
    return AppConfig(
        version="1.0.0",
        pricing=PricingConfig(
            min_price=5.0,
            max_price=30.0,
            price_step=1.0,
            default_options=[5.0, 10.0, 15.0, 20.0, 25.0, 30.0]
        ),
        pagination=PaginationConfig(
            default_page_size=10,
            max_page_size=100
        ),
        location=LocationConfig(
            max_distance_miles=MAX_DISTANCE_MILES,
            default_latitude=45.6770,
            default_longitude=-111.0429,
            default_location_name="Bozeman, MT"
        ),
        sorting=SortingConfig(
            options=[
                SortOption(id="rating", display="Rating", default_direction="desc"),
                SortOption(id="distance", display="Distance", default_direction="asc"),
                SortOption(id="item_count", display="Menu Items", default_direction="desc")
            ],
            default_sort_by="item_count",
            default_sort_direction="desc"
        )
    )
