package com.example.dietprefs.viewmodel

data class DisplayVendor(
    val vendorName: String,
    val user1Count: Int,
    val user2Count: Int,
    val distanceMiles: Double = 1.2,
    val querySpecificRatingString: String = "0/0",
    val querySpecificRatingValue: Float = 0f,
    val combinedRelevantItemCount: Int = 0
)