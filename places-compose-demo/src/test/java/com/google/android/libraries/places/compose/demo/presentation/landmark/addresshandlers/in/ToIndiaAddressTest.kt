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
import kotlin.test.Test

internal class ToIndiaAddressTest {
    @Test
    fun `can parse Gurugram India example to IndiaDisplayAddress`() {
        withResourceTestFile(
            fileName = "reverse_geocoded_test_data_gururam_india.json",
            klass = ReverseGeocodingResponse::class.java,
        ) { geocodingResponse ->
            val address = geocodingResponse.addresses.first()

            assert(
                address.formattedAddress ==
                    "93, White Wood St, Malibu Town, Sector 47, Gurugram, Haryana 122022, India",
            )

            val indiaDisplayAddress = address.toAddress("IN").toDisplayAddress() as IndiaDisplayAddress
            assert(indiaDisplayAddress.aptSuiteUnit == "93")
            assert(indiaDisplayAddress.streetAddress == "White Wood Street Malibu Town Sector 47")
            assert(indiaDisplayAddress.city == "Gurugram")
            assert(indiaDisplayAddress.state == "Haryana")
            assert(indiaDisplayAddress.country == "India")
            assert(indiaDisplayAddress.countryCode == "IN")
            assert(
                indiaDisplayAddress.toFormattedAddress() ==
                    "93\nWhite Wood Street Malibu Town Sector 47\nGurugram, Haryana 122022\nIndia",
            )
        }
    }

    @Test
    fun `can parse India example to IndiaDisplayAddress`() {
        withResourceTestFile(
            fileName = "reverse_geocoded_test_data_in.json",
            klass = ReverseGeocodingResponse::class.java,
        ) {
            val address = it.addresses.first()

            assert(
                address.formattedAddress ==
                    "akshardham society, 2/3, Sarthana - Kamrej Rd, " +
                    "opp. navjivan restorant, Sarthana Jakat Naka, Nana Varachha, " +
                    "Surat, Khadsad, Gujarat 395006, India",
            )

            val indiaDisplayAddress = address.toAddress("IN").toDisplayAddress() as IndiaDisplayAddress
            assert(indiaDisplayAddress.aptSuiteUnit == "akshardham society 2/3")
            assert(
                indiaDisplayAddress.streetAddress ==
                    "Sarthana - Kamrej Road Sarthana Jakat Naka Nana Varachha",
            )
            assert(indiaDisplayAddress.city == "Surat")
            assert(indiaDisplayAddress.state == "Gujarat")
            assert(indiaDisplayAddress.country == "India")
            assert(indiaDisplayAddress.countryCode == "IN")
        }
    }
}
