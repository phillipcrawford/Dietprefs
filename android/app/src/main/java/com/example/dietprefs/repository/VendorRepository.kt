package com.example.dietprefs.repository

import android.util.Log
import com.example.dietprefs.network.DietPrefsApiService
import com.example.dietprefs.network.RetrofitClient
import com.example.dietprefs.network.models.VendorSearchRequest
import com.example.dietprefs.network.models.VendorSearchResponse

class VendorRepository(
    private val apiService: DietPrefsApiService = RetrofitClient.apiService
) {

    suspend fun searchVendors(
        user1Preferences: List<String>,
        user2Preferences: List<String>,
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
}
