"""Configuration schemas for app-wide settings."""
from pydantic import BaseModel, Field
from typing import List


class PricingConfig(BaseModel):
    """Pricing filter configuration."""
    min_price: float = Field(5.0, description="Minimum price filter value")
    max_price: float = Field(30.0, description="Maximum price filter value")
    price_step: float = Field(1.0, description="Step increment for price selection")
    default_options: List[float] = Field(
        default_factory=lambda: [5.0, 10.0, 15.0, 20.0, 25.0, 30.0],
        description="Common price filter options to display"
    )


class PaginationConfig(BaseModel):
    """Pagination configuration."""
    default_page_size: int = Field(10, description="Default number of results per page")
    max_page_size: int = Field(100, description="Maximum allowed page size")


class LocationConfig(BaseModel):
    """Location and distance configuration."""
    max_distance_miles: float = Field(10.0, description="Maximum search radius in miles")
    default_latitude: float = Field(45.6770, description="Default latitude (Bozeman, MT)")
    default_longitude: float = Field(-111.0429, description="Default longitude (Bozeman, MT)")
    default_location_name: str = Field("Bozeman, MT", description="Default location display name")


class SortOption(BaseModel):
    """Available sort option."""
    id: str = Field(..., description="Sort option ID used in API requests")
    display: str = Field(..., description="User-facing display name")
    default_direction: str = Field(..., description="Default sort direction (asc/desc)")


class SortingConfig(BaseModel):
    """Sorting configuration."""
    options: List[SortOption] = Field(
        default_factory=lambda: [
            SortOption(id="rating", display="Rating", default_direction="desc"),
            SortOption(id="distance", display="Distance", default_direction="asc"),
            SortOption(id="item_count", display="Menu Items", default_direction="desc")
        ],
        description="Available sort options"
    )
    default_sort_by: str = Field("item_count", description="Default sort field")
    default_sort_direction: str = Field("desc", description="Default sort direction")


class AppConfig(BaseModel):
    """Complete application configuration."""
    version: str = Field("1.0.0", description="API version")
    pricing: PricingConfig = Field(default_factory=PricingConfig)
    pagination: PaginationConfig = Field(default_factory=PaginationConfig)
    location: LocationConfig = Field(default_factory=LocationConfig)
    sorting: SortingConfig = Field(default_factory=SortingConfig)
