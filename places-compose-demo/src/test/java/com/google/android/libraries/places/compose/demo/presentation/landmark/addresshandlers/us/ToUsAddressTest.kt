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
package com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us

import com.google.android.libraries.places.compose.autocomplete.domain.mappers.toAddress
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.ReverseGeocodingResponse
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.toDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.withResourceTestFile
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

internal class ToUsAddressTest {
    @Test
    fun `can parse US example to USDisplayAddress`() {
        withResourceTestFile(
            fileName = "reverse_geocoded_test_data_sf.json",
            klass = ReverseGeocodingResponse::class.java
        ) { geocodingResponse ->
            val address = geocodingResponse.addresses.first()

            assertThat(address.formattedAddress).isEqualTo(
                "101 The Embarcadero suite 200, San Francisco, CA 94105, USA"
            )

            val usAddress = address.toAddress("US").toDisplayAddress() as UsDisplayAddress
            assertThat(usAddress.streetAddress).isEqualTo("101 The Embarcadero")
            assertThat(usAddress.additionalAddressInfo).isEqualTo("suite 200")
            assertThat(usAddress.city).isEqualTo("San Francisco")
            assertThat(usAddress.state).isEqualTo("CA")
            assertThat(usAddress.zipCode).isEqualTo("94105")
            assertThat(usAddress.countryCode).isEqualTo("US")
            assertThat(usAddress.country).isEqualTo("United States")
        }
    }
}