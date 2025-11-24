package com.example.dietprefs

/**
 * Application-wide constants for UI configuration.
 */
object Constants {
    /**
     * Number of vendors to load per page in search results.
     */
    const val PAGE_SIZE = 10

    /**
     * Minimum price option for price filter (in dollars).
     */
    const val MIN_PRICE = 5f

    /**
     * Maximum price option for price filter (in dollars).
     */
    const val MAX_PRICE = 30f

    /**
     * Price increment step for filter options (in dollars).
     */
    const val PRICE_STEP = 1f

    /**
     * Test location coordinates for development/emulator.
     * Location: Bozeman, MT
     * TODO: Remove for production or make configurable.
     */
    const val TEST_LATITUDE = 45.6770
    const val TEST_LONGITUDE = -111.0429
}
