package org.verumomnis.forensic.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import org.verumomnis.forensic.core.ForensicLocation
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Location Service for Forensic Evidence
 * 
 * Captures precise location data for jurisdiction determination.
 * This is critical for:
 * - Determining applicable laws and regulations
 * - Establishing chain of custody
 * - Providing context for legal AI analysis
 * - Verifying evidence authenticity
 * 
 * Privacy Notice:
 * - Location data is stored only within the sealed forensic report
 * - No location data is transmitted externally
 * - User must explicitly grant permission
 */
class ForensicLocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get current location with full forensic metadata
     * 
     * @return ForensicLocation with complete jurisdiction data
     * @throws SecurityException if permission not granted
     * @throws LocationUnavailableException if location cannot be determined
     */
    suspend fun getCurrentLocation(): ForensicLocation {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }
        
        val location = getCurrentLocationRaw()
            ?: throw LocationUnavailableException("Unable to determine current location")
        
        return buildForensicLocation(location)
    }
    
    /**
     * Get location with fallback to last known location
     */
    suspend fun getLocationWithFallback(): ForensicLocation? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            val location = getCurrentLocationRaw() ?: getLastKnownLocation()
            location?.let { buildForensicLocation(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Create a manual location entry (for offline/no-GPS scenarios)
     */
    fun createManualLocation(
        latitude: Double,
        longitude: Double,
        locality: String?,
        administrativeArea: String?,
        country: String?,
        countryCode: String?
    ): ForensicLocation {
        val timezoneId = TimeZone.getDefault().id
        
        return ForensicLocation(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = -1f, // Manual entry indicator
            altitudeMeters = null,
            country = country,
            countryCode = countryCode,
            administrativeArea = administrativeArea,
            locality = locality,
            fullAddress = buildFullAddress(locality, administrativeArea, country),
            timezoneId = timezoneId
        )
    }
    
    private suspend fun getCurrentLocationRaw(): Location? {
        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
            
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    private suspend fun getLastKnownLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    private fun buildForensicLocation(location: Location): ForensicLocation {
        // Get timezone for this location
        val timezoneId = TimeZone.getDefault().id
        
        // Attempt reverse geocoding
        var country: String? = null
        var countryCode: String? = null
        var administrativeArea: String? = null
        var locality: String? = null
        var fullAddress: String? = null
        
        try {
            val addresses = getAddressesFromLocation(location.latitude, location.longitude)
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                country = address.countryName
                countryCode = address.countryCode
                administrativeArea = address.adminArea
                locality = address.locality ?: address.subAdminArea
                fullAddress = buildAddressString(address)
            }
        } catch (e: Exception) {
            // Geocoding failed - proceed without address data
        }
        
        return ForensicLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            altitudeMeters = if (location.hasAltitude()) location.altitude else null,
            country = country,
            countryCode = countryCode,
            administrativeArea = administrativeArea,
            locality = locality,
            fullAddress = fullAddress,
            timezoneId = timezoneId
        )
    }
    
    @Suppress("DEPRECATION")
    private fun getAddressesFromLocation(latitude: Double, longitude: Double): List<Address> {
        return try {
            // Using the synchronous method which works on all API levels
            // Note: The deprecated warning is suppressed as async geocoding adds complexity
            // for minimal benefit in an offline-first application
            geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun buildAddressString(address: Address): String {
        val parts = mutableListOf<String>()
        
        // Build full address from components
        for (i in 0..address.maxAddressLineIndex) {
            parts.add(address.getAddressLine(i))
        }
        
        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            buildFullAddress(
                address.locality,
                address.adminArea,
                address.countryName
            )
        }
    }
    
    private fun buildFullAddress(locality: String?, adminArea: String?, country: String?): String {
        val parts = listOfNotNull(locality, adminArea, country)
        return parts.joinToString(", ")
    }
}

/**
 * Exception thrown when location cannot be determined
 */
class LocationUnavailableException(message: String) : Exception(message)
