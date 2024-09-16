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
package com.google.android.libraries.places.compose.demo.presentation.landmark

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.components.AddressDisplay
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LandmarkSelectionContent(
    userLocation: LatLng,
    userMarker: MarkerState,
    onMapClicked: (LatLng) -> Unit,
    nearbyObjectsWithLocations: List<Pair<NearbyObject, Place>>,
    address: DisplayAddress?,
    showMap: Boolean,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 15f)
    }

    LaunchedEffect(userLocation) {
        userMarker.position = userLocation
        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 15f)
    }

    Column(
        modifier = modifier
    ) {
        if (showMap) {
            NearbyLandmarksMap(
                cameraPositionState = cameraPositionState,
                userMarker = userMarker,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                onMapClick = onMapClicked,
                nearbyObjectsWithLocations = nearbyObjectsWithLocations,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        ) {

            if (address != null) {
                AddressDisplay(
                    address = address,
                    modifier = Modifier.fillMaxWidth(),
                    nearbyObjects = nearbyObjectsWithLocations.map { it.first },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.no_address_to_show)
                    )
                }
            }
        }
    }
}