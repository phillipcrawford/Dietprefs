package com.example.dietprefs.model

import com.example.dietprefs.data.ItemEntity

/**
 * One source of truth for every preference:
 *  - display: what the user sees on the UI
 *  - matcher: how to check an ItemEntity. If null (e.g. "low price"), we ignore it in filtering.
 */
enum class Preference(
    val display: String,
    val matcher: ((ItemEntity) -> Boolean)?
) {
    VEGETARIAN("vegetarian", { it.vegetarian }),
    PESCETARIAN("pescetarian", { it.pescetarian }),
    VEGAN("vegan", { it.vegan }),
    KETO("keto", { it.keto }),
    ORGANIC("organic", { it.organic }),
    GMO_FREE("gmo-free", { it.gmoFree }),
    LOCALLY_SOURCED("locally sourced", { it.locallySourced }),
    RAW("raw", { it.raw }),
    ENTREE("entree", { it.entree }),
    SWEET("sweet", { it.sweet }),
    KOSHER("Kosher", { it.kosher }),
    HALAL("Halal", { it.halal }),
    BEEF("beef", { it.beef }),
    CHICKEN("chicken", { it.chicken }),
    PORK_FAMILY("bacon/pork/ham", { it.pork }),
    SEAFOOD("seafood", { it.seafood }),
    LOW_SUGAR("low sugar", { it.lowSugar }),
    HIGH_PROTEIN("high protein", { it.highProtein }),
    LOW_CARB("low carb", { it.lowCarb }),
    NO_ALLIUMS("no alliums", { it.noAlliums }),
    NO_PORK_PRODUCTS("no pork products", { it.noPorkProducts }),
    NO_RED_MEAT("no red meat", { it.noRedMeat }),
    NO_MSG("no msg", { it.noMsg }),
    NO_SESAME("no sesame", { it.noSesame }),
    NO_MILK("no milk", { it.noMilk }),
    NO_EGGS("no eggs", { it.noEggs }),
    NO_FISH("no fish", { it.noFish }),
    NO_SHELLFISH("no shellfish", { it.noShellfish }),
    NO_PEANUTS("no peanuts", { it.noPeanuts }),
    NO_TREENUTS("no treenuts", { it.noTreenuts }),
    GLUTEN_FREE("gluten-free", { it.glutenFree }),
    NO_SOY("no soy", { it.noSoy }),

    // Doesn't exist on ItemEntity; keep for UI, ignore in filtering.
    LOW_PRICE("low price", null);

    /** Returns true if this preference matches the item, or ignores if matcher == null */
    fun matches(item: ItemEntity): Boolean {
        return matcher?.invoke(item) ?: true
    }

    companion object {
        /** Order them *exactly* as you want to show in PreferenceScreen. */
        val orderedForUI: List<Preference> = listOf(
            VEGETARIAN, PESCETARIAN, VEGAN, KETO, ORGANIC, GMO_FREE,
            LOCALLY_SOURCED, RAW, ENTREE, SWEET, KOSHER, HALAL,
            BEEF, CHICKEN, PORK_FAMILY, SEAFOOD,
            LOW_SUGAR, HIGH_PROTEIN, LOW_CARB, NO_ALLIUMS,
            NO_PORK_PRODUCTS, NO_RED_MEAT, NO_MSG, NO_SESAME,
            NO_MILK, NO_EGGS, NO_FISH, NO_SHELLFISH,
            NO_PEANUTS, NO_TREENUTS, GLUTEN_FREE, NO_SOY,
            LOW_PRICE // last row with the user toggle
        )

        /** Helper to map display string (e.g. "low sugar") to its enum. */
        fun fromDisplay(display: String): Preference? {
            return values().firstOrNull { it.display.equals(display, ignoreCase = true) }
        }
    }
}
