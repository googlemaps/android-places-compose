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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.ViewModelEvent
import com.google.android.libraries.places.compose.demo.presentation.common.CommonEvent
import com.google.android.libraries.places.compose.demo.presentation.common.CommonScreen
import com.google.android.libraries.places.compose.demo.presentation.common.CommonViewModel
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import dagger.hilt.android.AndroidEntryPoint

/**
 * The activity for selecting a landmark.
 *
 * This activity uses the [LandmarkSelectionViewModel] to manage the selection process.
 */
@AndroidEntryPoint
class LandmarkSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val commonViewModel: CommonViewModel by viewModels()
        val landmarkSelectionViewModel: LandmarkSelectionViewModel by viewModels()

        // Force the map to be shown initially.
        commonViewModel.onEvent(CommonEvent.OnToggleMap)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                landmarkSelectionViewModel.viewModelEventChannel.collect { event ->
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

            CommonScreen(
                titleId = R.string.landmark_selection_button,
                commonViewModel = commonViewModel,
                onNavigateUp = { finish() },
                snackbarHostState = snackbarHostState
            ) { paddingValues ->
                val location by landmarkSelectionViewModel.location.collectAsStateWithLifecycle()
                val nearbyObjectsWithLocations by landmarkSelectionViewModel.nearbyObjectsWithLatLngs.collectAsStateWithLifecycle()
                val displayAddress by landmarkSelectionViewModel.displayAddress.collectAsStateWithLifecycle()

                val userMarker = rememberUpdatedMarkerState(position = location)
                val commonViewState by commonViewModel.commonViewState.collectAsStateWithLifecycle()
                val landmarkMarkers by landmarkSelectionViewModel.landmarkMarkers.collectAsStateWithLifecycle()

                LandmarkSelectionContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    userLocation = location,
                    userMarker = userMarker,
                    onMapClicked = { latLng ->
                        landmarkSelectionViewModel.onEvent(LandmarkSelectionEvent.OnUserLocationChanged(latLng))
                    },
                    nearbyObjectsWithLocations = nearbyObjectsWithLocations,
                    address = displayAddress,
                    showMap = commonViewState.showMap,
                    landmarkMarkers = landmarkMarkers
                )
            }
        }
    }
}
