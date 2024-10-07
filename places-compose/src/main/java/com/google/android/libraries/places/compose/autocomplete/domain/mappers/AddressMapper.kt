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
package com.google.android.libraries.places.compose.autocomplete.domain.mappers

import android.util.Log
import com.google.android.libraries.places.compose.autocomplete.models.Address
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.AddressComponent
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.AddressDto

fun AddressDto.toAddress(countryCode: String): Address {
    return Address(
        countryCode = countryCode,
        componentMap = AddressComponentMultiMap(addressComponents),
    )
}

fun Collection<AddressComponent>.toAddressComponentMultiMap(): AddressComponentMultiMap {
    return AddressComponentMultiMap(this)
}

fun AddressComponentMultiMap.toAddress(): Address {
    val countryCode = shortName(AddressComponentType.COUNTRY) ?: run {
        Log.w("AddressComponentMap", "Missing country code")
        "US"
    }

    return Address(
        countryCode = countryCode,
        componentMap = this,
    )
}

fun Collection<AddressComponent>.toAddress(): Address {
    return this.toAddressComponentMultiMap().toAddress()
}

fun List<AddressComponent>.toAddress(): Address {
    return this.toAddressComponentMultiMap().toAddress()
}

class AddressComponentMultiMap(components: Collection<AddressComponent>) {
    // Initialize a map to store address components grouped by their types
    private val componentsByType: Map<String, List<AddressComponent>> = components
        .flatMap { addressComponent -> 
            addressComponent.types.map { type -> type to addressComponent } 
        }
        .groupBy({ it.first }, { it.second })

    operator fun get(type: AddressComponentType) =
        componentsByType[type.name.lowercase()]

    fun getValue(type: AddressComponentType) =
        componentsByType.getValue(type.name.lowercase())

    fun longName(type: AddressComponentType, separator: String = " ") = get(type)?.joinToString(separator) { it.longName }

    fun shortName(type: AddressComponentType, separator: String = " ") = get(type)?.joinToString(separator) { it.shortName }
}

// From https://developers.google.com/maps/documentation/geocoding/requests-geocoding#Types
enum class AddressComponentType {
    /**
     * A precise street address.
     */
    STREET_NUMBER,

    /**
     * A precise street address.
     */
    STREET_ADDRESS,

    /**
     * A named route (such as "US 101").
     */
    ROUTE,

    /**
     * A major intersection, usually of two major roads.
     */
    INTERSECTION,

    /**
     * A political entity. Usually, this type indicates a polygon of some civil administration.
     */
    POLITICAL,

    /**
     * The national political entity, typically the highest order type returned by the Geocoder.
     */
    COUNTRY,

    /**
     * A first-order civil entity below the country level. Within the United States, these administrative levels are states. Not all nations exhibit these administrative levels.
     */
    ADMINISTRATIVE_AREA_LEVEL_1,

    /**
     * A second-order civil entity below the country level. Within the United States, these administrative levels are counties. Not all nations exhibit these administrative levels.
     */
    ADMINISTRATIVE_AREA_LEVEL_2,

    /**
     * A third-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
     */
    ADMINISTRATIVE_AREA_LEVEL_3,

    /**
     * A fourth-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
     */
    ADMINISTRATIVE_AREA_LEVEL_4,

    /**
     * A fifth-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
     */
    ADMINISTRATIVE_AREA_LEVEL_5,

    /**
     * A sixth-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
     */
    ADMINISTRATIVE_AREA_LEVEL_6,

    /**
     * A seventh-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
     */
    ADMINISTRATIVE_AREA_LEVEL_7,

    /**
     * A commonly-used alternative name for the entity.
     */
    COLLOQUIAL_AREA,

    /**
     * An incorporated city or town political entity.
     */
    LOCALITY,

    /**
     * A first-order civil entity below a locality. For some locations may receive one of the additional types: sublocality_level_1 to sublocality_level_5. Each sublocality level is a civil entity. Larger numbers indicate a smaller geographic area.
     */
    SUBLOCALITY,

    SUBLOCALITY_LEVEL_1,
    SUBLOCALITY_LEVEL_2,
    SUBLOCALITY_LEVEL_3,

    /**
     * A named neighborhood
     */
    NEIGHBORHOOD,

    /**
     * A named location, usually a building or collection of buildings with a common name
     */
    PREMISE,

    /**
     * A first-order entity below a named location, usually a singular building within a collection of buildings with a common name
     */
    SUBPREMISE,

    /**
     * An encoded location reference, derived from latitude and longitude. Plus codes can be used as a replacement for street addresses in places where they do not exist (where buildings are not numbered or streets are not named). See https://plus.codes for details.
     */
    PLUS_CODE,

    /**
     * A postal code as used to address postal mail within the country.
     */
    POSTAL_CODE,

    /**
     * A prominent natural feature.
     */
    NATURAL_FEATURE,

    /**
     * An airport.
     */
    AIRPORT,

    /**
     * A named park.
     */
    PARK,

    /**
     * A named point of interest. Typically, these "POI"s are prominent local entities that don't easily fit in another category, such as "Empire State Building" or "Eiffel Tower".
     */
    POINT_OF_INTEREST,

    /**
     * A nearby place that is used as a reference, to aid navigation
     */
    LANDMARK,
}
