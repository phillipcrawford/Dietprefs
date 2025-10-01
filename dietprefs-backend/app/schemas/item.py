from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime


class DietaryFlags(BaseModel):
    """All dietary and allergen flags for an item."""
    # Dietary preferences
    vegetarian: bool = False
    pescetarian: bool = False
    vegan: bool = False
    keto: bool = False
    organic: bool = False
    gmo_free: bool = False
    locally_sourced: bool = False
    raw: bool = False
    kosher: bool = False
    halal: bool = False

    # Meat types
    beef: bool = False
    chicken: bool = False
    pork: bool = False
    seafood: bool = False
    no_pork_products: bool = False
    no_red_meat: bool = False

    # Allergens
    no_milk: bool = False
    no_eggs: bool = False
    no_fish: bool = False
    no_shellfish: bool = False
    no_peanuts: bool = False
    no_treenuts: bool = False
    gluten_free: bool = False
    no_soy: bool = False
    no_sesame: bool = False
    no_msg: bool = False
    no_alliums: bool = False

    # Nutritional
    low_sugar: bool = False
    high_protein: bool = False
    low_carb: bool = False

    # Classification
    entree: bool = False
    sweet: bool = False


class ItemRating(BaseModel):
    """Item rating information."""
    upvotes: int
    total_votes: int
    percentage: float = Field(ge=0.0, le=1.0)


class ItemBase(BaseModel):
    """Base item schema with common fields."""
    name: str
    price: Optional[float] = None
    pictures: Optional[str] = None  # Comma-separated URLs


class ItemCreate(ItemBase):
    """Schema for creating a new item."""
    vendor_id: int
    dietary_flags: DietaryFlags


class ItemResponse(ItemBase):
    """Item response schema."""
    id: int
    vendor_id: int
    dietary_flags: DietaryFlags
    rating: ItemRating
    matches_user1: Optional[bool] = None
    matches_user2: Optional[bool] = None
    created_at: datetime

    class Config:
        from_attributes = True


class ItemVoteRequest(BaseModel):
    """Request schema for voting on an item."""
    vote: str = Field(..., pattern="^(up|down)$", description="Vote type: 'up' or 'down'")


class ItemVoteResponse(BaseModel):
    """Response schema after voting."""
    item_id: int
    upvotes: int
    total_votes: int
    rating_percentage: float
