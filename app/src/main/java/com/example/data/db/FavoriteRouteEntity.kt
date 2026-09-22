package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_routes")
data class FavoriteRouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String, // e.g. "Work to Home", "Airport Trip"
    val pickupName: String,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffName: String,
    val dropoffAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val cityName: String,
    val isFavorite: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
