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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import androidx.compose.material3.LinearProgressIndicator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.components.AddressDisplay
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LandmarkSelectionContent(
    userLocation: LatLng,
    userMarker: MarkerState,
    nearbyObjectsWithLocations: List<Pair<NearbyObject, Place>>,
    landmarkMarkers: List<LandmarkMarker>,
    address: DisplayAddress?,
    showMap: Boolean,
    onMapClicked: (LatLng) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onAddressChanged: ((DisplayAddress) -> Unit)? = null,
    onConfirmAddress: (() -> Unit)? = null,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 15f)
    }

    LaunchedEffect(userLocation) {
        userMarker.position = userLocation
        try {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(userLocation))
        } catch (_: Exception) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 15f)
        }
    }

    var selectedPlaceId by remember(landmarkMarkers) {
        mutableStateOf(landmarkMarkers.firstOrNull()?.landmark?.placeId)
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
                    .height(300.dp),
                onMapClick = { latLng ->
                    userMarker.position = latLng
                    onMapClicked(latLng)
                },
                selectedPlaceId = selectedPlaceId,
                onLandmarkSelected = {
                    selectedPlaceId = it
                },
                landmarkMarkers = landmarkMarkers
            )
            Text(
                text = stringResource(R.string.map_tap_instruction),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
            )
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )
            Text(
                text = "Looking up address & landmarks for (${"%.4f".format(userLocation.latitude)}, ${"%.4f".format(userLocation.longitude)})...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (nearbyObjectsWithLocations.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.address_descriptors_info_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = stringResource(R.string.address_descriptors_unavailable_message),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onMapClicked(LatLng(12.9794404, 77.7179181)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.switch_to_india_bangalore),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(
                                R.string.address_descriptors_available_hint,
                                landmarkMarkers.size
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (address != null) {
                AddressDisplay(
                    address = address,
                    modifier = Modifier.fillMaxWidth(),
                    nearbyObjects = nearbyObjectsWithLocations.map { it.first },
                    selectedPlaceId = selectedPlaceId,
                    onAddressChanged = onAddressChanged,
                    onNearbyLandmarkSelected = {
                        selectedPlaceId = it
                    }
                )

                Button(
                    onClick = { onConfirmAddress?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.confirm_address_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
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