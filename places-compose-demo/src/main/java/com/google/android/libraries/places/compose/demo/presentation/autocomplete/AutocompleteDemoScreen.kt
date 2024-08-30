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

package com.google.android.libraries.places.compose.demo.presentation.autocomplete

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.components.PlacesAutocompleteTextField
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.components.GoogleMapContainer
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun AutocompleteDemo(
    onQueryChanged: (String) -> Unit,
    onPlaceSelected: (AutocompletePlace) -> Unit,
    autocompleteViewState: AutocompleteViewState,
    modifier: Modifier = Modifier,
    onMapCloseClick: () -> Unit = {},
    location: LatLng,
    showMap: Boolean,
    locationLabel: String?,
) {
        Column(modifier.fillMaxSize()) {
            val selectedPlace = autocompleteViewState.selectedPlace

            PlacesAutocompleteTextField(
                modifier = Modifier.weight(1f),
                searchText = autocompleteViewState.searchText,
                predictions = autocompleteViewState.predictions,
                onQueryChanged = onQueryChanged,
                onSelected = { autocompletePlace ->
                    onPlaceSelected(autocompletePlace)
                },
                selectedPlace = selectedPlace,
                textFieldMaxLines = 4,
                scrollable = true,
                placeHolderText = stringResource(R.string.search_call_to_action)
            )

            val mapMarkerLocation = selectedPlace?.latLng ?: location

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(mapMarkerLocation, 15f)
            }

            if (showMap) {
                if (selectedPlace != null) {
                    GoogleMapContainer(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        cameraPositionState = cameraPositionState,
                        header = {
                            Column {
                                Text(
                                    text = selectedPlace.primaryText.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = Ellipsis
                                )
                                Text(
                                    text = selectedPlace.secondaryText.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = Ellipsis
                                )
                            }
                        },
                        markerLatLng = selectedPlace.latLng,
                        markerTitle = selectedPlace.primaryText.toString(),
                        markerSnippet = selectedPlace.secondaryText.toString(),
                        onMapCloseClick = onMapCloseClick
                    )
                } else {
                    val label = locationLabel ?: stringResource(R.string.unlabeled_location)

                    GoogleMapContainer(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        cameraPositionState = cameraPositionState,
                        header = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        markerLatLng = location,
                        markerTitle = label,
                        onMapCloseClick = onMapCloseClick
                    )
                }
            }
        }
}