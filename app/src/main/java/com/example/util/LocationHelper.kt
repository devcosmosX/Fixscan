package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationHelper {

    // Default fallback coordinates (Central Mumbai, India)
    const val DEFAULT_LATITUDE = 19.0760
    const val DEFAULT_LONGITUDE = 72.8777

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission(context)) {
            continuation.resume(createDefaultLocation())
            return@suspendCancellableCoroutine
        }

        val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        continuation.invokeOnCancellation {
            cts.cancel()
        }

        try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // Fall back to last known location
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            continuation.resume(lastLoc ?: createDefaultLocation())
                        }.addOnFailureListener {
                            continuation.resume(createDefaultLocation())
                        }
                    }
                }
                .addOnFailureListener {
                    // Try last location fallback
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                        continuation.resume(lastLoc ?: createDefaultLocation())
                    }.addOnFailureListener {
                        continuation.resume(createDefaultLocation())
                    }
                }
        } catch (e: SecurityException) {
            continuation.resume(createDefaultLocation())
        }
    }

    private fun createDefaultLocation(): Location {
        return Location("default_provider").apply {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
        }
    }
}
