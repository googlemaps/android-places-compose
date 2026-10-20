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
import kotlin.test.Test

internal class ToUsAddressTest {
    @Test
    fun `can parse US example to USDisplayAddress`() {
        withResourceTestFile(
            fileName = "reverse_geocoded_test_data_sf.json",
            klass = ReverseGeocodingResponse::class.java,
        ) { geocodingResponse ->
            val address = geocodingResponse.addresses.first()

            assert(
                address.formattedAddress ==
                    "101 The Embarcadero suite 200, San Francisco, CA 94105, USA",
            )

            val usAddress = address.toAddress("US").toDisplayAddress() as UsDisplayAddress
            assert(usAddress.streetAddress == "101 The Embarcadero")
            assert(usAddress.additionalAddressInfo == "suite 200")
            assert(usAddress.city == "San Francisco")
            assert(usAddress.state == "CA")
            assert(usAddress.zipCode == "94105")
            assert(usAddress.countryCode == "US")
            assert(usAddress.country == "United States")
        }
    }
}
