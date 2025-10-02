package com.example.dietprefs.network

import com.example.dietprefs.network.models.VendorSearchRequest
import com.example.dietprefs.network.models.VendorSearchResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface DietPrefsApiService {

    @POST("/api/v1/vendors/search")
    suspend fun searchVendors(
        @Body request: VendorSearchRequest
    ): VendorSearchResponse

    // Future endpoints can be added here:
    // @GET("/api/v1/vendors/{id}")
    // suspend fun getVendorDetails(@Path("id") vendorId: Int): VendorDetailResponse

    // @GET("/api/v1/vendors/{id}/items")
    // suspend fun getVendorItems(
    //     @Path("id") vendorId: Int,
    //     @Query("user1_preferences") user1Prefs: String = "",
    //     @Query("user2_preferences") user2Prefs: String = ""
    // ): List<ItemResponse>
}
