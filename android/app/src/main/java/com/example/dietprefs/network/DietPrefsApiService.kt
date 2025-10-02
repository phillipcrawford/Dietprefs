package com.example.dietprefs.network

import com.example.dietprefs.network.models.VendorSearchRequest
import com.example.dietprefs.network.models.VendorSearchResponse
import com.example.dietprefs.network.models.ItemResponse
import retrofit2.http.*

interface DietPrefsApiService {

    @POST("/api/v1/vendors/search")
    suspend fun searchVendors(
        @Body request: VendorSearchRequest
    ): VendorSearchResponse

    @GET("/api/v1/vendors/{id}/items")
    suspend fun getVendorItems(
        @Path("id") vendorId: Int,
        @Query("user1_preferences") user1Prefs: String = "",
        @Query("user2_preferences") user2Prefs: String = ""
    ): List<ItemResponse>

    @POST("/api/v1/items/{id}/vote")
    suspend fun voteOnItem(
        @Path("id") itemId: Int,
        @Body vote: Map<String, String>
    )
}
