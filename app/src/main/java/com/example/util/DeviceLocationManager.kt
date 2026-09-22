package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object DeviceLocationManager {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun getDevicePosition(
        context: Context,
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError("Location permission not granted. Please allow location access.")
            return
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        onSuccess(loc.latitude, loc.longitude)
                    } else {
                        // Try last location if immediate current is null
                        fusedClient.lastLocation
                            .addOnSuccessListener { lastLoc ->
                                if (lastLoc != null) {
                                    onSuccess(lastLoc.latitude, lastLoc.longitude)
                                } else {
                                    onError("Device position currently unavailable. Check that GPS/Location is enabled.")
                                }
                            }
                            .addOnFailureListener {
                                onError("Could not obtain device location: ${it.localizedMessage}")
                            }
                    }
                }
                .addOnFailureListener {
                    // Try last location on error
                    fusedClient.lastLocation
                        .addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onSuccess(lastLoc.latitude, lastLoc.longitude)
                            } else {
                                onError("Could not acquire device position: ${it.localizedMessage}")
                            }
                        }
                        .addOnFailureListener { err ->
                            onError("Could not acquire device position: ${err.localizedMessage}")
                        }
                }
        } catch (e: SecurityException) {
            onError("Location permission error: ${e.message}")
        } catch (e: Exception) {
            onError("Error accessing device location: ${e.message}")
        }
    }
}
