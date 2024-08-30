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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.compose.autocomplete.components.PlacesAutocompleteTextField
import com.google.android.libraries.places.compose.autocomplete.data.LocalUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.getUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.autocomplete.data.toMeters
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.components.GoogleMapContainer
import com.google.android.libraries.places.compose.demo.presentation.components.NextLocationButton
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteDemoScreen(viewModel: AutocompleteViewModel, onNavigateBack: () -> Unit) {
    val viewState by viewModel.viewState.collectAsState()
    val myLocation = viewState.location
    val biasRadius = 500.0.meters
    val countryCode by viewModel.countryCode.collectAsState(
        initial = LocalConfiguration.current.locales.get(0).country
    )

    // Determine which units converter to use based on the country.
    val unitsConverter by remember(countryCode) {
        mutableStateOf(getUnitsConverter(countryCode))
    }

    CompositionLocalProvider(LocalUnitsConverter provides unitsConverter) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = { Text("Autocomplete Demo") },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(AutocompleteEvent.OnQueryChanged(""))
                                viewModel.onEvent(AutocompleteEvent.OnUseDeviceLocation)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_my_location_24),
                                contentDescription = "Set the bias to the current location"
                            )
                        }
                        NextLocationButton(
                            isSelected = false
                        ) {
                            viewModel.onEvent(AutocompleteEvent.OnQueryChanged(""))
                            viewModel.onEvent(AutocompleteEvent.OnNextMockLocation)
                        }
                        IconButton(onClick = { viewModel.onEvent(AutocompleteEvent.OnToggleMap) }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_map_24),
                                contentDescription = "Toggle map"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            AutocompleteDemo(
                onQueryChanged = {
                    viewModel.onEvent(
                        AutocompleteEvent.OnQueryChanged(it) {
                            locationBias =
                                CircularBounds.newInstance(myLocation, biasRadius.toMeters)
                            origin = myLocation
                            countries = listOf(countryCode)
                        }
                    )
                },
                onPlaceSelected = {
                    viewModel.onEvent(AutocompleteEvent.OnPlaceSelected(it))
                },
                modifier = Modifier.padding(paddingValues),
                viewState = viewState,
                onMapCloseClick = {
                    viewModel.onEvent(AutocompleteEvent.OnSetMapVisible(false))
                }
            )
        }
    }
}

@Composable
fun AutocompleteDemo(
    onQueryChanged: (String) -> Unit,
    onPlaceSelected: (AutocompletePlace) -> Unit,
    viewState: ViewState,
    modifier: Modifier = Modifier,
    onMapCloseClick: () -> Unit = {}
) {
        Column(modifier.fillMaxSize()) {
            val autocompleteViewState = viewState.autocompleteViewState
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

            val mapMarkerLocation = selectedPlace?.latLng ?: viewState.location

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(mapMarkerLocation, 15f)
            }

            if (viewState.showMap) {
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
                    val label = viewState.locationLabel

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
                        markerLatLng = viewState.location,
                        markerTitle = label,
                        onMapCloseClick = onMapCloseClick
                    )
                }
            }
        }
}