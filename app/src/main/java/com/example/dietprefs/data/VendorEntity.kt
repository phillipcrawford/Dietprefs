package com.example.dietprefs.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lat: Double,
    val lng: Double,
    val region: Int,
    val delivery: Boolean,
    val takeout: Boolean,
    val website: String,
    val grubhub: Boolean,
    val doordash: Boolean,
    val ubereats: Boolean,
    val postmates: Boolean,
    val yelp: Boolean,
    val googleReviews: Boolean,
    val tripadvisor: Boolean,
    val hours: String, // store JSON or pipe-delimited string for simplicity
    val seo: String,   // pipe-delimited e.g. "american|burgers"
    val phone: Long,
    val address: String,
    val zipcode: Int,
    val customByNature: Boolean
)