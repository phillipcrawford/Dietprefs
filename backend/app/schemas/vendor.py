from pydantic import BaseModel, Field
from typing import List, Optional, Dict
from datetime import datetime


class DeliveryOptions(BaseModel):
    """Delivery and platform availability."""
    delivery: bool = False
    takeout: bool = False
    grubhub: bool = False
    doordash: bool = False
    ubereats: bool = False
    postmates: bool = False


class ReviewLinks(BaseModel):
    """External review platform availability."""
    yelp: bool = False
    google_reviews: bool = False
    tripadvisor: bool = False


class VendorRating(BaseModel):
    """Vendor rating based on query-relevant items."""
    upvotes: int
    total_votes: int
    percentage: float = Field(ge=0.0, le=1.0)


class ItemCounts(BaseModel):
    """Item counts for dual-user mode."""
    user1_matches: int
    user2_matches: int
    total_relevant: int


class VendorBase(BaseModel):
    """Base vendor schema with common fields."""
    name: str
    lat: float
    lng: float
    address: Optional[str] = None
    zipcode: Optional[int] = None
    phone: Optional[str] = None
    website: Optional[str] = None
    hours: Optional[str] = None  # JSON string
    seo_tags: Optional[str] = None
    region: Optional[int] = None
    custom_by_nature: bool = False


class VendorCreate(VendorBase):
    """Schema for creating a new vendor."""
    delivery: bool = False
    takeout: bool = False
    grubhub: bool = False
    doordash: bool = False
    ubereats: bool = False
    postmates: bool = False
    yelp: bool = False
    google_reviews: bool = False
    tripadvisor: bool = False


class VendorResponse(VendorBase):
    """Basic vendor response (used in search results)."""
    id: int
    distance_miles: Optional[float] = None
    rating: VendorRating
    item_counts: ItemCounts
    delivery_options: DeliveryOptions

    class Config:
        from_attributes = True


class VendorDetailResponse(VendorBase):
    """Detailed vendor response with all fields."""
    id: int
    delivery_options: DeliveryOptions
    review_links: ReviewLinks
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


class VendorSearchRequest(BaseModel):
    """Request schema for vendor search endpoint."""
    user1_preferences: List[str] = Field(default_factory=list, description="Dietary preferences for user 1")
    user2_preferences: List[str] = Field(default_factory=list, description="Dietary preferences for user 2")
    user1_max_price: Optional[float] = Field(None, description="Maximum price filter for user 1")
    user2_max_price: Optional[float] = Field(None, description="Maximum price filter for user 2")
    lat: Optional[float] = Field(None, description="User latitude for distance calculation")
    lng: Optional[float] = Field(None, description="User longitude for distance calculation")
    sort_by: str = Field("item_count", description="Sort by: rating, distance, or item_count")
    sort_direction: str = Field("desc", description="Sort direction: asc or desc")
    page: int = Field(1, ge=1, description="Page number (starts at 1)")
    page_size: int = Field(10, ge=1, le=100, description="Results per page")


class PaginationMeta(BaseModel):
    """Pagination metadata."""
    page: int
    page_size: int
    total_results: int
    total_pages: int


class VendorSearchResponse(BaseModel):
    """Response schema for vendor search endpoint."""
    vendors: List[VendorResponse]
    pagination: PaginationMeta
    user1_display: str = Field(default="", description="Formatted display text for user 1 filters")
    user2_display: str = Field(default="", description="Formatted display text for user 2 filters")
