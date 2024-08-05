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
package com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.`in`

import com.google.android.libraries.places.compose.autocomplete.domain.mappers.toAddress
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.ReverseGeocodingResponse
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.toDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.withResourceTestFile
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

internal class ToIndiaAddressTest {
    @Test
    fun `can parse Gurugram India example to IndiaDisplayAddress`() {
        withResourceTestFile(
            fileName = "reverse_geocoded_test_data_gururam_india.json",
            klass = ReverseGeocodingResponse::class.java
        ) { geocodingResponse ->
            val address = geocodingResponse.addresses.first()

            assertThat(address.formattedAddress)
                .isEqualTo(
                    "93, White Wood St, Malibu Town, Sector 47, Gurugram, Haryana 122022, India"
                )

            val indiaDisplayAddress = address.toAddress("IN").toDisplayAddress() as IndiaDisplayAddress
            assertThat(indiaDisplayAddress.aptSuiteUnit).isEqualTo("93")
            assertThat(indiaDisplayAddress.streetAddress).isEqualTo("White Wood Street Malibu Town Sector 47")
            assertThat(indiaDisplayAddress.city).isEqualTo("Gurugram")
            assertThat(indiaDisplayAddress.state).isEqualTo("Haryana")
            assertThat(indiaDisplayAddress.country).isEqualTo("India")
            assertThat(indiaDisplayAddress.countryCode).isEqualTo("IN")
        }
    }

    @Test
    fun `can parse India example to IndiaDisplayAddress`() {
        withResourceTestFile(
            fileName = "reverse_geocoded_test_data_in.json",
            klass = ReverseGeocodingResponse::class.java
        ) {
            val address = it.addresses.first()

            assertThat(address.formattedAddress)
                .isEqualTo(
                    "akshardham society, 2/3, Sarthana - Kamrej Rd, " +
                            "opp. navjivan restorant, Sarthana Jakat Naka, Nana Varachha, " +
                            "Surat, Khadsad, Gujarat 395006, India"
                )

            val indiaDisplayAddress = address.toAddress("IN").toDisplayAddress() as IndiaDisplayAddress
            assertThat(indiaDisplayAddress.aptSuiteUnit).isEqualTo("akshardham society 2/3")
            assertThat(indiaDisplayAddress.streetAddress).isEqualTo("Sarthana - Kamrej Road Sarthana Jakat Naka Nana Varachha")
            assertThat(indiaDisplayAddress.city).isEqualTo("Surat")
            assertThat(indiaDisplayAddress.state).isEqualTo("Gujarat")
            assertThat(indiaDisplayAddress.country).isEqualTo("India")
            assertThat(indiaDisplayAddress.countryCode).isEqualTo("IN")
        }
    }
}