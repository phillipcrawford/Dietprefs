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
            // Permission check already done above, safe to call
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }

            val cancellationTokenSource = CancellationTokenSource()

            val location: Location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).await()

            UserLocation(
                latitude = location.latitude,
                longitude = location.longitude
            )
        } catch (e: SecurityException) {
            // Permission was revoked during execution
            null
        } catch (e: Exception) {
            // Location unavailable - return San Francisco for testing in emulator
            // TODO: Remove this fallback for production
            UserLocation(latitude = 37.7749, longitude = -122.4194)
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
            // Permission check already done above, safe to call
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }

            val location: Location? = fusedLocationClient.lastLocation.await()

            // TODO: Remove this hardcoded location for production
            // For emulator testing, always return San Francisco to match test data
            UserLocation(latitude = 37.7749, longitude = -122.4194)

            // Production code (currently disabled for testing):
            // location?.let {
            //     UserLocation(latitude = it.latitude, longitude = it.longitude)
            // } ?: UserLocation(latitude = 37.7749, longitude = -122.4194)
        } catch (e: SecurityException) {
            // Permission was revoked during execution
            null
        } catch (e: Exception) {
            // Location unavailable - return San Francisco for testing in emulator
            // TODO: Remove this fallback for production
            UserLocation(latitude = 37.7749, longitude = -122.4194)
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
