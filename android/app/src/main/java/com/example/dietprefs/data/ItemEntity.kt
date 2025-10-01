package com.example.dietprefs.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vendorId: Int,
    val name: String,
    val vegetarian: Boolean,
    val pescetarian: Boolean,
    val vegan: Boolean,
    val keto: Boolean,
    val organic: Boolean,
    val gmoFree: Boolean,
    val locallySourced: Boolean,
    val raw: Boolean,
    val entree: Boolean,
    val sweet: Boolean,
    val kosher: Boolean,
    val halal: Boolean,
    val beef: Boolean,
    val chicken: Boolean,
    val pork: Boolean,
    val seafood: Boolean,
    val lowSugar: Boolean,
    val highProtein: Boolean,
    val lowCarb: Boolean,
    val noAlliums: Boolean,
    val noPorkProducts: Boolean,
    val noRedMeat: Boolean,
    val noMsg: Boolean,
    val noSesame: Boolean,
    val noMilk: Boolean,
    val noEggs: Boolean,
    val noFish: Boolean,
    val noShellfish: Boolean,
    val noPeanuts: Boolean,
    val noTreenuts: Boolean,
    val glutenFree: Boolean,
    val noSoy: Boolean,
    val price: Double,
    val upvotes: Int,
    val totalVotes: Int,
    val pictures: String // comma-separated image filenames
)
