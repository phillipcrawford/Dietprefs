package com.example.dietprefs.model

/**
 * One source of truth for every preference:
 *  - display: what the user sees on the UI
 *  - apiName: the name used in the backend API (snake_case format)
 */
enum class Preference(
    val display: String,
    val apiName: String
) {
    VEGETARIAN("vegetarian", "vegetarian"),
    PESCETARIAN("pescetarian", "pescetarian"),
    VEGAN("vegan", "vegan"),
    KETO("keto", "keto"),
    ORGANIC("organic", "organic"),
    GMO_FREE("gmo-free", "gmo_free"),
    LOCALLY_SOURCED("locally sourced", "locally_sourced"),
    RAW("raw", "raw"),
    ENTREE("entree", "entree"),
    SWEET("sweet", "sweet"),
    KOSHER("Kosher", "kosher"),
    HALAL("Halal", "halal"),
    BEEF("beef", "beef"),
    CHICKEN("chicken", "chicken"),
    PORK_FAMILY("bacon/pork/ham", "pork"),
    SEAFOOD("seafood", "seafood"),
    LOW_SUGAR("low sugar", "low_sugar"),
    HIGH_PROTEIN("high protein", "high_protein"),
    LOW_CARB("low carb", "low_carb"),
    NO_ALLIUMS("no alliums", "no_alliums"),
    NO_PORK_PRODUCTS("no pork products", "no_pork_products"),
    NO_RED_MEAT("no red meat", "no_red_meat"),
    NO_MSG("no msg", "no_msg"),
    NO_SESAME("no sesame", "no_sesame"),
    NO_MILK("no milk", "no_milk"),
    NO_EGGS("no eggs", "no_eggs"),
    NO_FISH("no fish", "no_fish"),
    NO_SHELLFISH("no shellfish", "no_shellfish"),
    NO_PEANUTS("no peanuts", "no_peanuts"),
    NO_TREENUTS("no treenuts", "no_treenuts"),
    GLUTEN_FREE("gluten-free", "gluten_free"),
    NO_SOY("no soy", "no_soy");

    companion object {
        /** Order them *exactly* as you want to show in PreferenceScreen. */
        val orderedForUI: List<Preference> = listOf(
            VEGETARIAN, PESCETARIAN, VEGAN, KETO, ORGANIC, GMO_FREE,
            LOCALLY_SOURCED, RAW, ENTREE, SWEET, KOSHER, HALAL,
            BEEF, CHICKEN, PORK_FAMILY, SEAFOOD,
            LOW_SUGAR, HIGH_PROTEIN, LOW_CARB, NO_ALLIUMS,
            NO_PORK_PRODUCTS, NO_RED_MEAT, NO_MSG, NO_SESAME,
            NO_MILK, NO_EGGS, NO_FISH, NO_SHELLFISH,
            NO_PEANUTS, NO_TREENUTS, GLUTEN_FREE, NO_SOY
        )

        /** Helper to map display string (e.g. "low sugar") to its enum. */
        fun fromDisplay(display: String): Preference? {
            return values().firstOrNull { it.display.equals(display, ignoreCase = true) }
        }
    }
}
