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

import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress

data class UsDisplayAddress(
    val streetAddress: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val additionalAddressInfo: String = "",
    // for suite, unit, floor, etc.
    override val countryCode: String = "US",
    override val country: String = "United States",
) : DisplayAddress {
    override fun toFormattedAddress(): String = """
        $streetAddress $additionalAddressInfo
        $city, $state $zipCode
        $country
    """.trimIndent()
}