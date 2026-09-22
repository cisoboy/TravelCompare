package com.example.data.places.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlacesAutocompleteResponse(
    @Json(name = "predictions") val predictions: List<AutocompletePredictionDto> = emptyList(),
    @Json(name = "status") val status: String = "",
    @Json(name = "error_message") val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class AutocompletePredictionDto(
    @Json(name = "description") val description: String = "",
    @Json(name = "place_id") val placeId: String = "",
    @Json(name = "structured_formatting") val structuredFormatting: StructuredFormattingDto? = null
)

@JsonClass(generateAdapter = true)
data class StructuredFormattingDto(
    @Json(name = "main_text") val mainText: String = "",
    @Json(name = "secondary_text") val secondaryText: String? = null
)

@JsonClass(generateAdapter = true)
data class PlaceDetailsResponse(
    @Json(name = "result") val result: PlaceDetailsResultDto? = null,
    @Json(name = "status") val status: String = "",
    @Json(name = "error_message") val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class PlaceDetailsResultDto(
    @Json(name = "name") val name: String = "",
    @Json(name = "formatted_address") val formattedAddress: String? = null,
    @Json(name = "geometry") val geometry: PlaceGeometryDto? = null
)

@JsonClass(generateAdapter = true)
data class PlaceGeometryDto(
    @Json(name = "location") val location: PlaceLocationDto = PlaceLocationDto()
)

@JsonClass(generateAdapter = true)
data class PlaceLocationDto(
    @Json(name = "lat") val lat: Double = 0.0,
    @Json(name = "lng") val lng: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: List<GeocodingResultDto> = emptyList(),
    @Json(name = "status") val status: String = "",
    @Json(name = "error_message") val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
    @Json(name = "formatted_address") val formattedAddress: String = "",
    @Json(name = "geometry") val geometry: PlaceGeometryDto = PlaceGeometryDto()
)

/**
 * Domain model representing a search prediction from Google Places or local fallback.
 */
data class PlaceSearchResult(
    val placeId: String,
    val mainText: String,
    val secondaryText: String,
    val fullAddress: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFromGoogleApi: Boolean = true
)
