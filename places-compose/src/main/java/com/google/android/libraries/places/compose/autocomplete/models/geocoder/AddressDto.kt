// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.libraries.places.compose.autocomplete.models.geocoder

import com.google.gson.annotations.SerializedName

data class ReverseGeocodingResponse(
    @SerializedName("status") val status: String,
    @SerializedName("address_descriptor") val addressDescriptor: AddressDescriptor?,
    @SerializedName("plus_code") val plusCode: PlusCode?,
    @SerializedName("results") val addresses: List<AddressDto>
)

data class AddressDescriptor(
    @SerializedName("areas") val areas: List<Area>,
    @SerializedName("landmarks") val landmarks: List<Landmark>
)

data class Area(
    @SerializedName("containment") val containment: String,
    @SerializedName("display_name") val displayName: DisplayName,
    @SerializedName("place_id") val placeId: String
)

data class Landmark(
    @SerializedName("display_name") val displayName: DisplayName,
    @SerializedName("place_id") val placeId: String,
    @SerializedName("spatial_relationship") val spatialRelationship: String,
    @SerializedName("straight_line_distance_meters") val straightLineDistanceMeters: Double,
    @SerializedName("travel_distance_meters") val travelDistanceMeters: Double,
    @SerializedName("types") val types: List<String>
)

data class DisplayName(
    @SerializedName("language_code") val languageCode: String,
    @SerializedName("text") val text: String
)

data class PlusCode(
    @SerializedName("compound_code") val compoundCode: String,
    @SerializedName("global_code") val globalCode: String
)

data class AddressDto(
    @SerializedName("address_components") val addressComponents: List<AddressComponent>,
    @SerializedName("formatted_address") val formattedAddress: String,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("place_id") val placeId: String,
    @SerializedName("plus_code") val plusCode: PlusCode,
    @SerializedName("types") val types: List<String>
) {
    fun getCountryCode(): String? {
        return addressComponents.firstOrNull { it.types.contains("country") }?.shortName
    }
}

data class AddressComponent(
    @SerializedName("long_name") val longName: String,
    @SerializedName("short_name") val shortName: String,
    val types: List<String>
)

data class Geometry(
    val location: Location,
    @SerializedName("location_type") val locationType: String,
    val viewport: Viewport,
    val bounds: Bounds?
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Location,
    val southwest: Location
)

data class Bounds(
    val northeast: Location,
    val southwest: Location
)
