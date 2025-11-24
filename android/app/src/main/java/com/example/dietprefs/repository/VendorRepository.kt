package com.example.dietprefs.repository

import android.util.Log
import com.example.dietprefs.network.DietPrefsApiService
import com.example.dietprefs.network.RetrofitClient
import com.example.dietprefs.network.models.VendorSearchRequest
import com.example.dietprefs.network.models.VendorSearchResponse
import com.example.dietprefs.network.models.ItemResponse

class VendorRepository(
    private val apiService: DietPrefsApiService = RetrofitClient.apiService
) {

    suspend fun searchVendors(
        user1Preferences: List<String>,
        user2Preferences: List<String>,
        user1MaxPrice: Float? = null,
        user2MaxPrice: Float? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        sortBy: String = "item_count",
        sortDirection: String = "desc",
        page: Int = 1,
        pageSize: Int = 10
    ): Result<VendorSearchResponse> {
        return try {
            val request = VendorSearchRequest(
                user1Preferences = user1Preferences,
                user2Preferences = user2Preferences,
                user1MaxPrice = user1MaxPrice,
                user2MaxPrice = user2MaxPrice,
                lat = latitude,
                lng = longitude,
                sortBy = sortBy,
                sortDirection = sortDirection,
                page = page,
                pageSize = pageSize
            )

            Log.d("VendorRepository", "Searching with location: lat=$latitude, lng=$longitude")
            Log.d("VendorRepository", "Request: $request")

            val response = apiService.searchVendors(request)
            Log.d("VendorRepository", "Response: ${response.vendors.size} vendors, first distance: ${response.vendors.firstOrNull()?.distanceMiles}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Search failed", e)
            Result.failure(e)
        }
    }

    suspend fun getVendorItems(
        vendorId: Int,
        user1Preferences: List<String>,
        user2Preferences: List<String>,
        user1MaxPrice: Float?,
        user2MaxPrice: Float?
    ): Result<List<ItemResponse>> {
        return try {
            val user1Prefs = user1Preferences.joinToString(",")
            val user2Prefs = user2Preferences.joinToString(",")

            Log.d("VendorRepository", "Fetching items for vendor $vendorId")
            Log.d("VendorRepository", "  user1: prefs=$user1Prefs, maxPrice=$user1MaxPrice")
            Log.d("VendorRepository", "  user2: prefs=$user2Prefs, maxPrice=$user2MaxPrice")

            val items = apiService.getVendorItems(
                vendorId, user1Prefs, user2Prefs, user1MaxPrice, user2MaxPrice
            )
            Log.d("VendorRepository", "Received ${items.size} items")
            Result.success(items)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Failed to fetch items", e)
            Result.failure(e)
        }
    }

    suspend fun voteOnItem(itemId: Int, voteType: String): Result<Unit> {
        return try {
            Log.d("VendorRepository", "Voting $voteType on item $itemId")
            apiService.voteOnItem(itemId, mapOf("vote" to voteType))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Failed to vote", e)
            Result.failure(e)
        }
    }
}
