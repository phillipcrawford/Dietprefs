package com.example.dietprefs.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

@Database(entities = [VendorEntity::class, ItemEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vendorDao(): VendorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Ensures database is only seeded on first creation
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.vendorDao())
                }
            }
        }
    }
}

// Function that seeds the database with initial vendors and items
suspend fun populateDatabase(dao: VendorDao) {
    for (i in 1..20) {
        val vendor = VendorEntity(
            name = "Vendor $i",
            address = "123 Vendor St, Town $i",
            customByNature = i % 2 == 0,
            delivery = i % 3 == 0,
            doordash = i % 4 == 0,
            googleReviews = i % 2 != 0,
            grubhub = i % 5 == 0,
            hours = "Mon-Fri 10am-9pm",
            lat = 37.7749,
            lng = -122.4194,
            postmates = i % 6 == 0,
            phone = 1234567890,
            region = i,
            seo = "food,restaurants",
            takeout = i % 7 == 0,
            tripadvisor = i % 8 == 0,
            ubereats = i % 9 == 0,
            website = "https://www.example.com",
            zipcode = 12345,
            yelp = i % 10 == 0

        )
        val vendorId = dao.insertVendor(vendor)

        for (j in 1..7) {
            val totalVotes = Random.nextInt(5, 51) // Random total votes between 5 and 50
            val upvotes = Random.nextInt(0, totalVotes + 1) // Random upvotes between 0 and totalVotes

            val item = ItemEntity(
                vendorId = vendorId.toInt(),
                name = "Item $j",
                vegetarian = (j % 3 == 0),
                pescetarian = (j % 2 == 0),
                vegan = (j % 4 == 0), // Varied this a bit
                keto = (j % 5 == 0),  // Varied this a bit
                organic = (j % 2 != 0), // Varied this a bit
                gmoFree = false,
                locallySourced = false,
                raw = false,
                entree = (j == 1 || j == 2), // Example: first two items are entrees
                sweet = (j == 5),          // Example: last item is sweet
                kosher = true,
                halal = false,
                beef = false,
                chicken = (j == 1), // Example
                pork = false,
                seafood = false,
                lowSugar = false,
                highProtein = true,
                lowCarb = (j % 2 == 0),
                noAlliums = false,
                noPorkProducts = false,
                noRedMeat = false,
                noMsg = false,
                noSesame = false,
                noMilk = false,
                noEggs = false,
                noFish = false,
                noShellfish = false,
                noPeanuts = false,
                noTreenuts = false,
                glutenFree = (j % 3 != 0), // Example
                noSoy = false,
                price = Random.nextDouble(5.0, 25.0), // Random price
                upvotes = upvotes,     // Use generated upvotes
                totalVotes = totalVotes, // Use generated totalVotes
                pictures = ""
            )
            dao.insertItem(item)
        }
    }
}
