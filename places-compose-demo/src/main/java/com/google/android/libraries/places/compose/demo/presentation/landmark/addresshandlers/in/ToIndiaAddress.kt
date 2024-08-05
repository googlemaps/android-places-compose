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

import com.google.android.libraries.places.compose.autocomplete.domain.mappers.AddressComponentType
import com.google.android.libraries.places.compose.autocomplete.models.Address

fun Address.toIndiaDisplayAddress(): IndiaDisplayAddress {
    fun getComponents(vararg types: AddressComponentType): List<String?> {
        return types.asList().map {
            componentMap.longName(it)
        }
    }

    fun join(vararg types: AddressComponentType): String {
        return types.asList().mapNotNull {
            componentMap.longName(it)
        }.filter { it.isNotBlank() }.joinToString(" ")
    }

    fun firstOf(vararg types: AddressComponentType): String? {
        return types.asList().mapNotNull {
            componentMap.longName(it)
        }.firstOrNull { it.isNotBlank() }
    }

    val aptSuiteUnit = getComponents(
        AddressComponentType.SUBPREMISE,
        AddressComponentType.PREMISE,
        AddressComponentType.STREET_NUMBER,
    ).filterNotNull().filter { it.isNotBlank() }.joinToString(" ")

    val streetAddress = getComponents(
        AddressComponentType.ROUTE,
        AddressComponentType.SUBLOCALITY_LEVEL_3,
        AddressComponentType.SUBLOCALITY_LEVEL_2,
        AddressComponentType.SUBLOCALITY_LEVEL_1,
    ).filterNotNull().filter { it.isNotBlank() }.joinToString(" ")

    return IndiaDisplayAddress(
        aptSuiteUnit = aptSuiteUnit,
        streetAddress = streetAddress,

        city = firstOf(
            AddressComponentType.LOCALITY,
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_3,
        ) ?: "",

        state = join(
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1,
        ),

        country = componentMap.longName(AddressComponentType.COUNTRY) ?: "India",
        countryCode = componentMap.shortName(AddressComponentType.COUNTRY) ?: "IN",
        pinCode = componentMap.longName(AddressComponentType.POSTAL_CODE) ?: "",
    )
}
