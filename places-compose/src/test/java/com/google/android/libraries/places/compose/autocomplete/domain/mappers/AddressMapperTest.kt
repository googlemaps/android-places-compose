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

import com.google.android.libraries.places.compose.autocomplete.models.geocoder.AddressComponent
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.AddressDto
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Geometry
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Location
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.PlusCode
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Viewport
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests verifying address component mapping and multimap lookups.
 *
 * This test suite covers:
 * 1. [AddressComponentMultiMap] indexing components across multiple type tags.
 * 2. Lookup behaviors by [AddressComponentType], including [AddressComponentMultiMap.get] and [AddressComponentMultiMap.getValue].
 * 3. Formatted string aggregations using [AddressComponentMultiMap.longName] and [AddressComponentMultiMap.shortName].
 * 4. Conversions from collections, lists, and [AddressDto] instances to domain [Address] models.
 * 5. Country code extraction and fallback logic.
 */
class AddressMapperTest {

    private val sampleComponents = listOf(
        AddressComponent(
            longName = "1600",
            shortName = "1600",
            types = listOf("street_number"),
        ),
        AddressComponent(
            longName = "Amphitheatre Parkway",
            shortName = "Amphitheatre Pkwy",
            types = listOf("route"),
        ),
        AddressComponent(
            longName = "Mountain View",
            shortName = "Mountain View",
            types = listOf("locality", "political"),
        ),
        AddressComponent(
            longName = "Santa Clara County",
            shortName = "Santa Clara",
            types = listOf("administrative_area_level_2", "political"),
        ),
        AddressComponent(
            longName = "California",
            shortName = "CA",
            types = listOf("administrative_area_level_1", "political"),
        ),
        AddressComponent(
            longName = "United States",
            shortName = "US",
            types = listOf("country", "political"),
        ),
        AddressComponent(
            longName = "94043",
            shortName = "94043",
            types = listOf("postal_code"),
        ),
    )

    // ----------------------------------------------------------------------------------
    // MultiMap Grouping & Lookups
    // ----------------------------------------------------------------------------------

    @Test
    fun multiMap_groupsByTypesCorrectly() {
        val multiMap = sampleComponents.toAddressComponentMultiMap()

        val country = assertNotNull(multiMap[AddressComponentType.COUNTRY])
        assert(country.size == 1)
        assert(country.first().longName == "United States")

        val politicalComponents = multiMap.getValue(AddressComponentType.POLITICAL)
        assert(politicalComponents.size == 4)
    }

    @Test
    fun multiMap_longNameAndShortName_aggregateWithSeparator() {
        val multiMap = sampleComponents.toAddressComponentMultiMap()

        val routeLong = multiMap.longName(AddressComponentType.ROUTE)
        assert(routeLong == "Amphitheatre Parkway")

        val routeShort = multiMap.shortName(AddressComponentType.ROUTE)
        assert(routeShort == "Amphitheatre Pkwy")

        val politicalShort = multiMap.shortName(AddressComponentType.POLITICAL, separator = ", ")
        assert(politicalShort == "Mountain View, Santa Clara, CA, US")
    }

    @Test
    fun multiMap_missingType_returnsNull() {
        val multiMap = sampleComponents.toAddressComponentMultiMap()

        val airport = multiMap[AddressComponentType.AIRPORT]
        assertNull(airport)
        assertNull(multiMap.longName(AddressComponentType.AIRPORT))
        assertNull(multiMap.shortName(AddressComponentType.AIRPORT))
    }

    // ----------------------------------------------------------------------------------
    // Conversions to Domain Address Model
    // ----------------------------------------------------------------------------------

    @Test
    fun toAddress_fromMultiMap_extractsCountryCode() {
        val multiMap = sampleComponents.toAddressComponentMultiMap()
        val address = multiMap.toAddress()

        assert(address.countryCode == "US")
        assert(address.componentMap == multiMap)
    }

    @Test
    fun toAddress_fromMultiMap_fallsBackToUsWhenCountryMissing() {
        val componentsWithoutCountry = sampleComponents.filterNot { it.types.contains("country") }
        val multiMap = componentsWithoutCountry.toAddressComponentMultiMap()

        val address = multiMap.toAddress()
        assert(address.countryCode == "US")
    }

    @Test
    fun toAddress_fromCollectionAndList_convertsCorrectly() {
        val addressFromCollection = (sampleComponents as Collection<AddressComponent>).toAddress()
        assert(addressFromCollection.countryCode == "US")

        val addressFromList = sampleComponents.toAddress()
        assert(addressFromList.countryCode == "US")
    }

    @Test
    fun toAddress_fromAddressDto_usesSuppliedCountryCode() {
        val addressDto = AddressDto(
            addressComponents = sampleComponents,
            formattedAddress = "1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA",
            geometry = Geometry(
                location = Location(37.422, -122.084),
                locationType = "ROOFTOP",
                viewport = Viewport(
                    northeast = Location(37.423, -122.083),
                    southwest = Location(37.421, -122.085),
                ),
                bounds = null,
            ),
            placeId = "ChIJ2eUgeAK6j4ARbn5u_wAGqWA",
            plusCode = PlusCode("849VCWC8+R9", "849VCWC8+R9 Mountain View"),
            types = listOf("street_address"),
        )

        val address = addressDto.toAddress("US")
        assert(address.countryCode == "US")
        assert(address.componentMap[AddressComponentType.STREET_NUMBER]?.first()?.longName == "1600")
    }
}
