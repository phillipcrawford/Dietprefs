package com.example.dietprefs.network.models

import com.google.gson.annotations.SerializedName

// ===== Config Models =====

data class AppConfig(
    val version: String,
    val pricing: PricingConfig,
    val pagination: PaginationConfig,
    val location: LocationConfig,
    val sorting: SortingConfig
)

data class PricingConfig(
    @SerializedName("min_price")
    val minPrice: Float,
    @SerializedName("max_price")
    val maxPrice: Float,
    @SerializedName("price_step")
    val priceStep: Float,
    @SerializedName("default_options")
    val defaultOptions: List<Float>
)

data class PaginationConfig(
    @SerializedName("default_page_size")
    val defaultPageSize: Int,
    @SerializedName("max_page_size")
    val maxPageSize: Int
)

data class LocationConfig(
    @SerializedName("max_distance_miles")
    val maxDistanceMiles: Float,
    @SerializedName("default_latitude")
    val defaultLatitude: Double,
    @SerializedName("default_longitude")
    val defaultLongitude: Double,
    @SerializedName("default_location_name")
    val defaultLocationName: String
)

data class SortOption(
    val id: String,
    val display: String,
    @SerializedName("default_direction")
    val defaultDirection: String
)

data class SortingConfig(
    val options: List<SortOption>,
    @SerializedName("default_sort_by")
    val defaultSortBy: String,
    @SerializedName("default_sort_direction")
    val defaultSortDirection: String
)

// ===== Request Models =====

data class VendorSearchRequest(
    @SerializedName("user1_preferences")
    val user1Preferences: List<String> = emptyList(),
    @SerializedName("user2_preferences")
    val user2Preferences: List<String> = emptyList(),
    @SerializedName("user1_max_price")
    val user1MaxPrice: Float? = null,
    @SerializedName("user2_max_price")
    val user2MaxPrice: Float? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    @SerializedName("search_query")
    val searchQuery: String? = null,
    @SerializedName("sort_by")
    val sortBy: String = "item_count",
    @SerializedName("sort_direction")
    val sortDirection: String = "desc",
    val page: Int = 1,
    @SerializedName("page_size")
    val pageSize: Int = 10
)

// ===== Response Models =====

data class VendorSearchResponse(
    val vendors: List<VendorResponse>,
    val pagination: PaginationMeta,
    @SerializedName("user1_display")
    val user1Display: String = "",
    @SerializedName("user2_display")
    val user2Display: String = ""
)

data class PaginationMeta(
    val page: Int,
    @SerializedName("page_size")
    val pageSize: Int,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

data class VendorResponse(
    val id: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String?,
    val zipcode: Int?,
    val phone: String?,
    val website: String?,
    val hours: String?,
    @SerializedName("seo_tags")
    val seoTags: String?,
    val region: Int?,
    @SerializedName("custom_by_nature")
    val customByNature: Boolean,
    @SerializedName("distance_miles")
    val distanceMiles: Double?,
    val rating: VendorRating,
    @SerializedName("item_counts")
    val itemCounts: ItemCounts,
    @SerializedName("delivery_options")
    val deliveryOptions: DeliveryOptions
)

data class VendorRating(
    val upvotes: Int,
    @SerializedName("total_votes")
    val totalVotes: Int,
    val percentage: Float
)

data class ItemCounts(
    @SerializedName("user1_matches")
    val user1Matches: Int,
    @SerializedName("user2_matches")
    val user2Matches: Int,
    @SerializedName("total_relevant")
    val totalRelevant: Int
)

data class DeliveryOptions(
    val delivery: Boolean,
    val takeout: Boolean,
    val grubhub: Boolean,
    val doordash: Boolean,
    val ubereats: Boolean,
    val postmates: Boolean
)

// ===== Item Models (for future use) =====

data class ItemResponse(
    val id: Int,
    @SerializedName("vendor_id")
    val vendorId: Int,
    val name: String,
    val price: Double?,
    val pictures: String?,
    @SerializedName("dietary_flags")
    val dietaryFlags: DietaryFlags,
    val rating: ItemRating,
    @SerializedName("matches_user1")
    val matchesUser1: Boolean?,
    @SerializedName("matches_user2")
    val matchesUser2: Boolean?,
    @SerializedName("created_at")
    val createdAt: String
)

data class ItemRating(
    val upvotes: Int,
    @SerializedName("total_votes")
    val totalVotes: Int,
    val percentage: Float
)

data class DietaryFlags(
    val vegetarian: Boolean,
    val pescetarian: Boolean,
    val vegan: Boolean,
    val keto: Boolean,
    val organic: Boolean,
    @SerializedName("gmo_free")
    val gmoFree: Boolean,
    @SerializedName("locally_sourced")
    val locallySourced: Boolean,
    val raw: Boolean,
    val kosher: Boolean,
    val halal: Boolean,
    val beef: Boolean,
    val chicken: Boolean,
    val pork: Boolean,
    val seafood: Boolean,
    @SerializedName("no_pork_products")
    val noPorkProducts: Boolean,
    @SerializedName("no_red_meat")
    val noRedMeat: Boolean,
    @SerializedName("no_milk")
    val noMilk: Boolean,
    @SerializedName("no_eggs")
    val noEggs: Boolean,
    @SerializedName("no_fish")
    val noFish: Boolean,
    @SerializedName("no_shellfish")
    val noShellfish: Boolean,
    @SerializedName("no_peanuts")
    val noPeanuts: Boolean,
    @SerializedName("no_treenuts")
    val noTreenuts: Boolean,
    @SerializedName("gluten_free")
    val glutenFree: Boolean,
    @SerializedName("no_soy")
    val noSoy: Boolean,
    @SerializedName("no_sesame")
    val noSesame: Boolean,
    @SerializedName("no_msg")
    val noMsg: Boolean,
    @SerializedName("no_alliums")
    val noAlliums: Boolean,
    @SerializedName("low_sugar")
    val lowSugar: Boolean,
    @SerializedName("high_protein")
    val highProtein: Boolean,
    @SerializedName("low_carb")
    val lowCarb: Boolean,
    val entree: Boolean,
    val sweet: Boolean
)
