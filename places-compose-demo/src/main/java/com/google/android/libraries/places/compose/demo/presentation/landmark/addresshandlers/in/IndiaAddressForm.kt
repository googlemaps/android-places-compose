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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.AddressTextField
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.CountryField
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.components.NearbyObjectsSelector

@Composable
fun IndiaAddressForm(
    modifier: Modifier,
    address: IndiaDisplayAddress,
    nearbyObjects: List<NearbyObject>,
    onNearbyLandmarkSelected: (String?) -> Unit = {},
    onAddressChanged: ((DisplayAddress) -> Unit)? = null,
    selectedPlaceId: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AddressTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.aptSuiteUnit,
            label = R.string.india_address_unit_number,
            onValueChange = { onAddressChanged?.invoke(address.copy(aptSuiteUnit = it)) },
        )

        AddressTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.streetAddress,
            label = R.string.india_address_street_address,
            onValueChange = { onAddressChanged?.invoke(address.copy(streetAddress = it)) },
        )

        if (nearbyObjects.isNotEmpty()) {
            NearbyObjectsSelector(
                nearbyObjects = nearbyObjects,
                onNearbyLandmarkSelected = onNearbyLandmarkSelected,
                selectedPlaceId = selectedPlaceId
            )
        }

        AddressTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.city,
            label = R.string.india_address_city,
            onValueChange = { onAddressChanged?.invoke(address.copy(city = it)) },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AddressTextField(
                modifier = Modifier.weight(1f),
                value = address.state,
                label = R.string.india_address_state,
                onValueChange = { onAddressChanged?.invoke(address.copy(state = it)) },
            )

           AddressTextField(
                modifier = Modifier.weight(1f),
                value = address.pinCode,
                label = R.string.india_address_pincode,
                onValueChange = { onAddressChanged?.invoke(address.copy(pinCode = it)) },
           )
        }

        CountryField(address)
    }
}

