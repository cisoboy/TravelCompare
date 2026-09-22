package com.example.data.places

import com.example.data.places.model.GeocodingResponse
import com.example.data.places.model.PlaceDetailsResponse
import com.example.data.places.model.PlacesAutocompleteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApiService {

    @GET("maps/api/place/autocomplete/json")
    suspend fun autocomplete(
        @Query("input") input: String,
        @Query("key") apiKey: String,
        @Query("location") location: String? = null,
        @Query("radius") radius: Int? = null,
        @Query("language") language: String? = "en"
    ): PlacesAutocompleteResponse

    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String,
        @Query("fields") fields: String = "name,formatted_address,geometry",
        @Query("language") language: String? = "en"
    ): PlaceDetailsResponse

    @GET("maps/api/geocode/json")
    suspend fun reverseGeocode(
        @Query("latlng") latLng: String,
        @Query("key") apiKey: String,
        @Query("language") language: String? = "en"
    ): GeocodingResponse
}
