package com.example.dietprefs.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * Service to get user's current location using Google Play Services.
 */
class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Check if location permissions are granted.
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get the user's current location.
     * Returns null if location cannot be obtained or permissions not granted.
     */
    suspend fun getCurrentLocation(): UserLocation? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()

            val location: Location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).await()

            UserLocation(
                latitude = location.latitude,
                longitude = location.longitude
            )
        } catch (e: Exception) {
            // Location unavailable (airplane mode, GPS off, etc.)
            null
        }
    }

    /**
     * Get last known location (faster, but might be stale).
     * Returns null if no cached location or permissions not granted.
     */
    suspend fun getLastKnownLocation(): UserLocation? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val location: Location? = fusedLocationClient.lastLocation.await()
            location?.let {
                UserLocation(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Simple data class to hold user location coordinates.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double
)
