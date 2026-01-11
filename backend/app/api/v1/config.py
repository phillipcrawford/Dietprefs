"""Configuration endpoint for app-wide settings."""
from fastapi import APIRouter
from app.schemas.config import (
    AppConfig,
    PricingConfig,
    PaginationConfig,
    LocationConfig,
    SortingConfig,
    SortOption,
    PreferencesConfig,
    PreferenceMetadata,
    PreferenceCategory
)
from app.config import settings

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
            max_distance_miles=settings.MAX_DISTANCE_MILES,
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


@router.get("/preferences", response_model=PreferencesConfig)
async def get_preferences():
    """
    Get dietary preference metadata.

    Returns all available dietary preferences with their:
    - API names (snake_case for backend)
    - Display names (user-facing text)
    - Categories (dietary, meat, allergen, etc.)
    - Descriptions

    This ensures all clients (Android, iOS, Web) display preferences
    with identical formatting without hardcoding display strings.
    """
    return PreferencesConfig(
        version="1.0.0",
        preferences=[
            # Dietary preferences
            PreferenceMetadata(
                api_name="vegetarian",
                display="vegetarian",
                category=PreferenceCategory.DIETARY,
                description="No meat, poultry, or seafood"
            ),
            PreferenceMetadata(
                api_name="pescetarian",
                display="pescetarian",
                category=PreferenceCategory.DIETARY,
                description="No meat or poultry, seafood allowed"
            ),
            PreferenceMetadata(
                api_name="vegan",
                display="vegan",
                category=PreferenceCategory.DIETARY,
                description="No animal products"
            ),
            PreferenceMetadata(
                api_name="keto",
                display="keto",
                category=PreferenceCategory.DIETARY,
                description="Low-carb, high-fat diet"
            ),
            PreferenceMetadata(
                api_name="organic",
                display="organic",
                category=PreferenceCategory.DIETARY,
                description="Organically grown ingredients"
            ),
            PreferenceMetadata(
                api_name="gmo_free",
                display="gmo-free",
                category=PreferenceCategory.DIETARY,
                description="No genetically modified organisms"
            ),
            PreferenceMetadata(
                api_name="locally_sourced",
                display="locally sourced",
                category=PreferenceCategory.DIETARY,
                description="Ingredients from local producers"
            ),
            PreferenceMetadata(
                api_name="raw",
                display="raw",
                category=PreferenceCategory.DIETARY,
                description="Uncooked, unprocessed foods"
            ),
            PreferenceMetadata(
                api_name="kosher",
                display="Kosher",
                category=PreferenceCategory.DIETARY,
                description="Prepared according to Jewish dietary law"
            ),
            PreferenceMetadata(
                api_name="halal",
                display="Halal",
                category=PreferenceCategory.DIETARY,
                description="Prepared according to Islamic dietary law"
            ),

            # Meat types
            PreferenceMetadata(
                api_name="beef",
                display="beef",
                category=PreferenceCategory.MEAT,
                description="Contains beef"
            ),
            PreferenceMetadata(
                api_name="chicken",
                display="chicken",
                category=PreferenceCategory.MEAT,
                description="Contains chicken"
            ),
            PreferenceMetadata(
                api_name="pork",
                display="bacon/pork/ham",
                category=PreferenceCategory.MEAT,
                description="Contains pork products"
            ),
            PreferenceMetadata(
                api_name="seafood",
                display="seafood",
                category=PreferenceCategory.MEAT,
                description="Contains fish or shellfish"
            ),
            PreferenceMetadata(
                api_name="no_pork_products",
                display="no pork products",
                category=PreferenceCategory.MEAT,
                description="Free from pork and pork derivatives"
            ),
            PreferenceMetadata(
                api_name="no_red_meat",
                display="no red meat",
                category=PreferenceCategory.MEAT,
                description="Free from beef, pork, lamb"
            ),

            # Allergens
            PreferenceMetadata(
                api_name="no_milk",
                display="no milk",
                category=PreferenceCategory.ALLERGEN,
                description="Dairy-free, lactose-free"
            ),
            PreferenceMetadata(
                api_name="no_eggs",
                display="no eggs",
                category=PreferenceCategory.ALLERGEN,
                description="Free from eggs and egg products"
            ),
            PreferenceMetadata(
                api_name="no_fish",
                display="no fish",
                category=PreferenceCategory.ALLERGEN,
                description="Free from fish"
            ),
            PreferenceMetadata(
                api_name="no_shellfish",
                display="no shellfish",
                category=PreferenceCategory.ALLERGEN,
                description="Free from shellfish"
            ),
            PreferenceMetadata(
                api_name="no_peanuts",
                display="no peanuts",
                category=PreferenceCategory.ALLERGEN,
                description="Free from peanuts"
            ),
            PreferenceMetadata(
                api_name="no_treenuts",
                display="no treenuts",
                category=PreferenceCategory.ALLERGEN,
                description="Free from tree nuts (almonds, cashews, etc.)"
            ),
            PreferenceMetadata(
                api_name="gluten_free",
                display="gluten-free",
                category=PreferenceCategory.ALLERGEN,
                description="Free from wheat, barley, rye"
            ),
            PreferenceMetadata(
                api_name="no_soy",
                display="no soy",
                category=PreferenceCategory.ALLERGEN,
                description="Free from soy products"
            ),
            PreferenceMetadata(
                api_name="no_sesame",
                display="no sesame",
                category=PreferenceCategory.ALLERGEN,
                description="Free from sesame seeds and oil"
            ),
            PreferenceMetadata(
                api_name="no_msg",
                display="no msg",
                category=PreferenceCategory.ALLERGEN,
                description="No monosodium glutamate"
            ),
            PreferenceMetadata(
                api_name="no_alliums",
                display="no alliums",
                category=PreferenceCategory.ALLERGEN,
                description="No onions, garlic, leeks, shallots"
            ),

            # Nutritional
            PreferenceMetadata(
                api_name="low_sugar",
                display="low sugar",
                category=PreferenceCategory.NUTRITIONAL,
                description="Reduced sugar content"
            ),
            PreferenceMetadata(
                api_name="high_protein",
                display="high protein",
                category=PreferenceCategory.NUTRITIONAL,
                description="High in protein content"
            ),
            PreferenceMetadata(
                api_name="low_carb",
                display="low carb",
                category=PreferenceCategory.NUTRITIONAL,
                description="Low in carbohydrates"
            ),

            # Classification
            PreferenceMetadata(
                api_name="entree",
                display="entree",
                category=PreferenceCategory.CLASSIFICATION,
                description="Main dish or entree"
            ),
            PreferenceMetadata(
                api_name="sweet",
                display="sweet",
                category=PreferenceCategory.CLASSIFICATION,
                description="Desserts and sweet items"
            ),

            # Price (special handling)
            PreferenceMetadata(
                api_name="low_price",
                display="low price",
                category=PreferenceCategory.PRICE,
                description="Price filter (formatted as 'under $X')"
            ),
        ]
    )
