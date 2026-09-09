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

import com.google.android.libraries.places.api.model.AddressComponent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests verifying conversion of Places SDK [AddressComponent] lists into domain [Address] models.
 */
class PlaceMapperTest {

    @Test
    fun toAddress_mapsPlacesSdkAddressComponentsToDomainAddress() {
        val placesComponents = listOf(
            AddressComponent.builder("India", listOf("country", "political"))
                .setShortName("IN")
                .build(),
            AddressComponent.builder("Bengaluru", listOf("locality", "political"))
                .setShortName("BLR")
                .build(),
            AddressComponent.builder("Whitefield", listOf("sublocality", "political"))
                .setShortName("Whitefield")
                .build()
        )

        val domainAddress = placesComponents.toAddress()

        assertThat(domainAddress.countryCode).isEqualTo("IN")
        val countryComponent = domainAddress.componentMap[AddressComponentType.COUNTRY]
        assertThat(countryComponent).isNotNull()
        assertThat(countryComponent?.first()?.longName).isEqualTo("India")
        assertThat(countryComponent?.first()?.shortName).isEqualTo("IN")
    }

    @Test
    fun toAddress_handlesNullShortNameGracefully() {
        val placesComponents = listOf(
            AddressComponent.builder("United States", listOf("country", "political"))
                .setShortName(null)
                .build()
        )

        val domainAddress = placesComponents.toAddress()

        val countryComponent = domainAddress.componentMap[AddressComponentType.COUNTRY]
        assertThat(countryComponent?.first()?.shortName).isEmpty()
    }
}
