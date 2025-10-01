from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.vendor import (
    VendorSearchRequest,
    VendorSearchResponse,
    VendorDetailResponse,
    PaginationMeta,
    DeliveryOptions,
    ReviewLinks
)
from app.schemas.item import ItemResponse, DietaryFlags, ItemRating
from app.services.vendor_service import VendorService
import math

router = APIRouter()


@router.post("/vendors/search", response_model=VendorSearchResponse)
async def search_vendors(
    request: VendorSearchRequest,
    db: Session = Depends(get_db)
):
    """
    Search vendors based on dietary preferences.

    - **user1_preferences**: List of dietary preferences for user 1
    - **user2_preferences**: List of dietary preferences for user 2
    - **lat/lng**: Optional user location for distance calculation
    - **sort_by**: Sort by 'rating', 'distance', or 'item_count'
    - **sort_direction**: 'asc' or 'desc'
    - **page**: Page number (starts at 1)
    - **page_size**: Results per page (1-100)
    """
    vendors, total_count = VendorService.search_vendors(db, request)

    total_pages = math.ceil(total_count / request.page_size) if total_count > 0 else 0

    return VendorSearchResponse(
        vendors=vendors,
        pagination=PaginationMeta(
            page=request.page,
            page_size=request.page_size,
            total_results=total_count,
            total_pages=total_pages
        )
    )


@router.get("/vendors/{vendor_id}", response_model=VendorDetailResponse)
async def get_vendor_details(
    vendor_id: int,
    db: Session = Depends(get_db)
):
    """
    Get detailed information about a specific vendor.
    """
    vendor = VendorService.get_vendor_by_id(db, vendor_id)

    if not vendor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Vendor with id {vendor_id} not found"
        )

    return VendorDetailResponse(
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
        delivery_options=DeliveryOptions(
            delivery=vendor.delivery,
            takeout=vendor.takeout,
            grubhub=vendor.grubhub,
            doordash=vendor.doordash,
            ubereats=vendor.ubereats,
            postmates=vendor.postmates
        ),
        review_links=ReviewLinks(
            yelp=vendor.yelp,
            google_reviews=vendor.google_reviews,
            tripadvisor=vendor.tripadvisor
        ),
        created_at=vendor.created_at,
        updated_at=vendor.updated_at
    )


@router.get("/vendors/{vendor_id}/items", response_model=List[ItemResponse])
async def get_vendor_items(
    vendor_id: int,
    user1_preferences: str = "",
    user2_preferences: str = "",
    db: Session = Depends(get_db)
):
    """
    Get all menu items for a specific vendor.

    - **user1_preferences**: Comma-separated dietary preferences for user 1
    - **user2_preferences**: Comma-separated dietary preferences for user 2

    Returns items with flags indicating which user's preferences they match.
    """
    # Check if vendor exists
    vendor = VendorService.get_vendor_by_id(db, vendor_id)
    if not vendor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Vendor with id {vendor_id} not found"
        )

    # Parse preferences
    user1_prefs = [p.strip() for p in user1_preferences.split(",") if p.strip()]
    user2_prefs = [p.strip() for p in user2_preferences.split(",") if p.strip()]

    items = VendorService.get_vendor_items(db, vendor_id, user1_prefs, user2_prefs)

    # Convert to response schema
    response_items = []
    for item in items:
        dietary_flags = DietaryFlags(
            vegetarian=item.vegetarian,
            pescetarian=item.pescetarian,
            vegan=item.vegan,
            keto=item.keto,
            organic=item.organic,
            gmo_free=item.gmo_free,
            locally_sourced=item.locally_sourced,
            raw=item.raw,
            kosher=item.kosher,
            halal=item.halal,
            beef=item.beef,
            chicken=item.chicken,
            pork=item.pork,
            seafood=item.seafood,
            no_pork_products=item.no_pork_products,
            no_red_meat=item.no_red_meat,
            no_milk=item.no_milk,
            no_eggs=item.no_eggs,
            no_fish=item.no_fish,
            no_shellfish=item.no_shellfish,
            no_peanuts=item.no_peanuts,
            no_treenuts=item.no_treenuts,
            gluten_free=item.gluten_free,
            no_soy=item.no_soy,
            no_sesame=item.no_sesame,
            no_msg=item.no_msg,
            no_alliums=item.no_alliums,
            low_sugar=item.low_sugar,
            high_protein=item.high_protein,
            low_carb=item.low_carb,
            entree=item.entree,
            sweet=item.sweet
        )

        response_items.append(ItemResponse(
            id=item.id,
            vendor_id=item.vendor_id,
            name=item.name,
            price=item.price,
            pictures=item.pictures,
            dietary_flags=dietary_flags,
            rating=ItemRating(
                upvotes=item.upvotes,
                total_votes=item.total_votes,
                percentage=item.rating_percentage
            ),
            matches_user1=getattr(item, 'matches_user1', None),
            matches_user2=getattr(item, 'matches_user2', None),
            created_at=item.created_at
        ))

    return response_items
