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
package com.google.android.libraries.places.compose.demo.presentation.landmark.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.`in`.IndiaAddressForm
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.`in`.IndiaDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us.UsAddressForm
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us.UsDisplayAddress

@Composable
fun AddressDisplay(
    address: DisplayAddress?,
    modifier: Modifier = Modifier,
    nearbyObjects: List<NearbyObject> = emptyList(),
    onNearbyLandmarkSelected: (NearbyObject) -> Unit = {},
    onAddressChanged: ((DisplayAddress) -> Unit)? = null,
    selectedLandmark: NearbyObject? = null,
) {
    when (address) {
        is IndiaDisplayAddress -> {
            IndiaAddressForm(
                modifier = modifier,
                address = address,
                nearbyObjects = nearbyObjects,
                onAddressChanged = onAddressChanged,
                onNearbyLandmarkSelected = onNearbyLandmarkSelected,
                selectedObject = selectedLandmark,
            )
        }
        is UsDisplayAddress -> {
            UsAddressForm(
                modifier = modifier,
                address = address,
                nearbyObjects = nearbyObjects,
                onAddressChanged = onAddressChanged,
                onNearbyLandmarkSelected = onNearbyLandmarkSelected,
                selectedObject = selectedLandmark,
            )
        }
    }
}
