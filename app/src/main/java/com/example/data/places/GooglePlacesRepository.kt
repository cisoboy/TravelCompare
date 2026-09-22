package com.example.data.places

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import com.example.data.places.model.PlaceSearchResult
import com.example.model.CityPreset
import com.example.model.CityPresets
import com.example.model.LocationPoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit

class GooglePlacesRepository {

    private val apiService: GooglePlacesApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GooglePlacesApiService::class.java)
    }

    val isApiKeyConfigured: Boolean
        get() {
            val key = getApiKey()
            return key.isNotBlank() &&
                    key != "YOUR_GOOGLE_PLACES_API_KEY" &&
                    key != "MY_GOOGLE_PLACES_API_KEY"
        }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GOOGLE_PLACES_API_KEY.trim()
        } catch (e: Throwable) {
            ""
        }
    }

    /**
     * Searches for places matching [query] using Google Places Autocomplete API.
     * If the API key is not configured or an API error occurs, falls back to local curated points.
     */
    suspend fun searchPlaces(query: String, city: CityPreset?): List<PlaceSearchResult> =
        withContext(Dispatchers.IO) {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isEmpty()) {
                return@withContext getCuratedSuggestions(city)
            }

            if (isApiKeyConfigured) {
                try {
                    val locationParam = city?.let { "${it.centerLat},${it.centerLng}" }
                    val response = apiService.autocomplete(
                        input = trimmedQuery,
                        apiKey = getApiKey(),
                        location = locationParam,
                        radius = 45000
                    )

                    if (response.status == "OK" && response.predictions.isNotEmpty()) {
                        return@withContext response.predictions.map { pred ->
                            PlaceSearchResult(
                                placeId = pred.placeId,
                                mainText = pred.structuredFormatting?.mainText ?: pred.description,
                                secondaryText = pred.structuredFormatting?.secondaryText ?: "",
                                fullAddress = pred.description,
                                isFromGoogleApi = true
                            )
                        }
                    } else {
                        Log.d("GooglePlacesRepo", "Google Places status: ${response.status} - ${response.errorMessage}")
                    }
                } catch (e: Exception) {
                    Log.w("GooglePlacesRepo", "Places Autocomplete request failed, falling back to local database", e)
                }
            }

            // Fallback: search local curated landmarks & cities
            return@withContext filterLocalLocations(trimmedQuery, city)
        }

    /**
     * Resolves exact coordinates and details for a selected place.
     */
    suspend fun getPlaceCoordinates(
        placeId: String,
        cachedName: String,
        cachedAddress: String
    ): LocationPoint? = withContext(Dispatchers.IO) {
        if (placeId.startsWith("local_")) {
            val localPoint = findLocalPointById(placeId)
            if (localPoint != null) return@withContext localPoint
        }

        if (isApiKeyConfigured && !placeId.startsWith("local_")) {
            try {
                val response = apiService.getPlaceDetails(
                    placeId = placeId,
                    apiKey = getApiKey()
                )
                val result = response.result
                if (response.status == "OK" && result?.geometry != null) {
                    return@withContext LocationPoint(
                        name = result.name.ifBlank { cachedName },
                        address = result.formattedAddress ?: cachedAddress,
                        latitude = result.geometry.location.lat,
                        longitude = result.geometry.location.lng,
                        tag = "Google Place"
                    )
                }
            } catch (e: Exception) {
                Log.w("GooglePlacesRepo", "Place details fetch failed", e)
            }
        }

        // If local match exists
        val fallback = findLocalPointByName(cachedName)
        if (fallback != null) return@withContext fallback

        // Generated nearby coordinate for query
        LocationPoint(
            name = cachedName,
            address = cachedAddress,
            latitude = 51.5074,
            longitude = -0.1278,
            tag = "Custom"
        )
    }

    /**
     * Reverse-geocodes device coordinates to human-readable address.
     */
    suspend fun reverseGeocodeCoordinates(
        lat: Double,
        lng: Double,
        context: Context
    ): LocationPoint = withContext(Dispatchers.IO) {
        // 1. Try Android built-in Geocoder
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val featureName = addr.featureName
                    val thoroughfare = addr.thoroughfare
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                    val line = addr.getAddressLine(0)

                    val name = when {
                        !featureName.isNullOrBlank() && featureName != thoroughfare -> featureName
                        !thoroughfare.isNullOrBlank() -> thoroughfare
                        !locality.isBlank() -> "Current Location ($locality)"
                        else -> "Device Position"
                    }

                    val fullAddress = line ?: "$name, $locality"
                    return@withContext LocationPoint(
                        name = name,
                        address = fullAddress,
                        latitude = lat,
                        longitude = lng,
                        tag = "Device GPS"
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("GooglePlacesRepo", "Android Geocoder error: ${e.message}")
        }

        // 2. Try Google Geocoding API if key is set
        if (isApiKeyConfigured) {
            try {
                val response = apiService.reverseGeocode(
                    latLng = "$lat,$lng",
                    apiKey = getApiKey()
                )
                if (response.status == "OK" && response.results.isNotEmpty()) {
                    val first = response.results[0]
                    return@withContext LocationPoint(
                        name = "Device Position",
                        address = first.formattedAddress,
                        latitude = lat,
                        longitude = lng,
                        tag = "Device GPS"
                    )
                }
            } catch (e: Exception) {
                Log.w("GooglePlacesRepo", "Google reverse geocode error", e)
            }
        }

        // 3. Fallback coordinates representation
        LocationPoint(
            name = "Device Position",
            address = String.format(Locale.US, "%.5f, %.5f", lat, lng),
            latitude = lat,
            longitude = lng,
            tag = "Device GPS"
        )
    }

    private fun getCuratedSuggestions(city: CityPreset?): List<PlaceSearchResult> {
        val list = mutableListOf<PlaceSearchResult>()
        city?.let { c ->
            c.popularLocations.forEachIndexed { idx, loc ->
                list.add(
                    PlaceSearchResult(
                        placeId = "local_${c.id}_$idx",
                        mainText = loc.name,
                        secondaryText = loc.address,
                        fullAddress = "${loc.name}, ${loc.address}",
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        isFromGoogleApi = false
                    )
                )
            }
        }
        return list
    }

    private fun filterLocalLocations(query: String, city: CityPreset?): List<PlaceSearchResult> {
        val allPoints = mutableListOf<Pair<LocationPoint, String>>()

        city?.popularLocations?.forEach { allPoints.add(it to city.name) }

        // Also check all other cities
        listOf(
            CityPresets.LONDON,
            CityPresets.PARIS,
            CityPresets.WARSAW,
            CityPresets.BERLIN,
            CityPresets.AMSTERDAM,
            CityPresets.NEW_YORK
        ).forEach { cp ->
            if (cp.id != city?.id) {
                cp.popularLocations.forEach { allPoints.add(it to cp.name) }
            }
        }

        val q = query.lowercase(Locale.ROOT)
        val filtered = allPoints.filter { (point, cityName) ->
            point.name.lowercase(Locale.ROOT).contains(q) ||
                    point.address.lowercase(Locale.ROOT).contains(q) ||
                    cityName.lowercase(Locale.ROOT).contains(q)
        }

        if (filtered.isEmpty()) {
            // Provide a synthetic custom place prediction based on user typing
            val baseLat = city?.centerLat ?: 51.5074
            val baseLng = city?.centerLng ?: -0.1278
            val offset = (query.hashCode() % 100) / 4000.0
            return listOf(
                PlaceSearchResult(
                    placeId = "local_custom_${query.hashCode()}",
                    mainText = query,
                    secondaryText = city?.let { "${it.name}, ${it.country}" } ?: "Coordinates Search",
                    fullAddress = "$query, ${city?.name ?: ""}",
                    latitude = baseLat + offset,
                    longitude = baseLng + offset,
                    isFromGoogleApi = false
                )
            )
        }

        return filtered.mapIndexed { idx, (point, cityName) ->
            PlaceSearchResult(
                placeId = "local_${point.name.hashCode()}_$idx",
                mainText = point.name,
                secondaryText = "${point.address} ($cityName)",
                fullAddress = "${point.name}, ${point.address}",
                latitude = point.latitude,
                longitude = point.longitude,
                isFromGoogleApi = false
            )
        }
    }

    private fun findLocalPointById(placeId: String): LocationPoint? {
        val allPresets = listOf(
            CityPresets.LONDON,
            CityPresets.PARIS,
            CityPresets.WARSAW,
            CityPresets.BERLIN,
            CityPresets.AMSTERDAM,
            CityPresets.NEW_YORK
        )
        for (c in allPresets) {
            c.popularLocations.forEachIndexed { idx, loc ->
                if (placeId == "local_${c.id}_$idx") return loc
            }
        }
        return null
    }

    private fun findLocalPointByName(name: String): LocationPoint? {
        val allPresets = listOf(
            CityPresets.LONDON,
            CityPresets.PARIS,
            CityPresets.WARSAW,
            CityPresets.BERLIN,
            CityPresets.AMSTERDAM,
            CityPresets.NEW_YORK
        )
        for (c in allPresets) {
            val found = c.popularLocations.find { it.name.equals(name, ignoreCase = true) }
            if (found != null) return found
        }
        return null
    }
}
