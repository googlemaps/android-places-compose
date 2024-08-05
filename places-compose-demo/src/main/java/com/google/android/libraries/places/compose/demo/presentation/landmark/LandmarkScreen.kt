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

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.google.android.libraries.places.compose.autocomplete.data.LocalUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.getUnitsConverter
import com.google.android.libraries.places.compose.demo.presentation.ViewModelEvent

/**
 * Composable function that represents the landmark screen.
 *
 * @param viewModel The [LandmarkSelectionViewModel] associated with this screen.
 */
@Composable
fun LandmarkScreen(
    viewModel: LandmarkSelectionViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val countryCode by viewModel.countryCode.collectAsState()

    // Determine which units converter to use based on the country.
    val unitsConverter = remember(countryCode) {
        getUnitsConverter(countryCode)
    }

    val userLocation by viewModel.location.collectAsState()
    val displayAddress by viewModel.displayAddress.collectAsState()
    val nearbyObjectsWithLocations by viewModel.nearbyObjectsWithLatLngs.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.viewModelEventChannel.collect { event ->
            when (event) {
                is ViewModelEvent.UserMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    CompositionLocalProvider(LocalUnitsConverter provides unitsConverter) {
        LandmarkSelectionScreen(
            userLocation = userLocation,
            onNextLocationClicked = {
                viewModel.onEvent(LandmarkSelectionEvent.OnNextMockLocation)
            },
            onCurrentLocationClicked = {
                viewModel.onEvent(LandmarkSelectionEvent.OnUseDeviceLocation)
            },
            onMapClicked = { latLng ->
                viewModel.onEvent(LandmarkSelectionEvent.OnUserLocationChanged(latLng))
            },
            snackbarHostState = snackbarHostState,
            nearbyObjectsWithLocations = nearbyObjectsWithLocations,
            onNavigateBack = onNavigateBack,
            address = displayAddress,
        )
    }
}