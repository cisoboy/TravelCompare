package com.example.model

import java.util.Locale

data class LocationPoint(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val tag: String = "" // e.g., "Airport", "Train", "Hotel", "Custom", "Device GPS", "Google Place"
) {
    fun formattedCoordinates(): String =
        String.format(Locale.US, "%.4f, %.4f", latitude, longitude)

    fun formattedDms(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lngDir = if (longitude >= 0) "E" else "W"
        return String.format(
            Locale.US,
            "%.3f° %s, %.3f° %s",
            kotlin.math.abs(latitude),
            latDir,
            kotlin.math.abs(longitude),
            lngDir
        )
    }

    val isDevicePosition: Boolean
        get() = tag.contains("GPS", ignoreCase = true) ||
                tag.contains("Device", ignoreCase = true)
}
