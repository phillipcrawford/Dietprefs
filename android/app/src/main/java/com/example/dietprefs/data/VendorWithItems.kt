package com.example.dietprefs.data

import androidx.room.Embedded
import androidx.room.Relation

data class VendorWithItems(
    @Embedded val vendor: VendorEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "vendorId"
    )
    val items: List<ItemEntity>
)
