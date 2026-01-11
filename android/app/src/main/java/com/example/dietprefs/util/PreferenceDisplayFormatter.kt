package com.example.dietprefs.util

import com.example.dietprefs.model.Preference

/**
 * Utility for formatting preference display text.
 *
 * This formatter uses backend-provided preference metadata (cached from /api/v1/preferences)
 * to ensure consistent display formatting across all clients (Android, iOS, Web).
 *
 * Falls back to hardcoded Preference enum values if backend metadata is unavailable.
 */
object PreferenceDisplayFormatter {

    /**
     * Build display text for a set of preferences and optional price filter.
     *
     * @param preferences Set of preferences to display
     * @param maxPrice Optional maximum price threshold
     * @param preferenceMetadata Map of api_name -> display_text from backend (optional)
     * @return Formatted display string (e.g., "vegetarian, gluten-free, under $15")
     *
     * Examples:
     * - buildDisplayText(setOf(VEGETARIAN), 10f, metadata) → "vegetarian, under $10"
     * - buildDisplayText(setOf(VEGAN, GLUTEN_FREE), null, metadata) → "vegan, gluten-free"
     * - buildDisplayText(emptySet(), 15f, metadata) → "under $15"
     */
    fun buildDisplayText(
        preferences: Set<Preference>,
        maxPrice: Float?,
        preferenceMetadata: Map<String, String> = emptyMap()
    ): String {
        val parts = mutableListOf<String>()

        // Add preferences (exclude LOW_PRICE as it's handled separately via maxPrice)
        val filteredPrefs = preferences.filter { it != Preference.LOW_PRICE }
        for (pref in filteredPrefs) {
            val displayText = getDisplayText(pref, preferenceMetadata)
            parts.add(displayText)
        }

        // Add price filter if present
        if (maxPrice != null) {
            parts.add("under $${"%.0f".format(maxPrice)}")
        }

        return parts.joinToString(", ")
    }

    /**
     * Get display text for a single preference.
     *
     * Tries backend metadata first, falls back to enum display value.
     *
     * @param preference The preference enum value
     * @param preferenceMetadata Map of api_name -> display_text from backend
     * @return Display text for the preference
     */
    private fun getDisplayText(
        preference: Preference,
        preferenceMetadata: Map<String, String>
    ): String {
        // Try backend metadata first (source of truth)
        val backendDisplay = preferenceMetadata[preference.apiName]
        if (backendDisplay != null) {
            return backendDisplay
        }

        // Fallback to enum display value (offline mode or fetch failure)
        return preference.display
    }
}
