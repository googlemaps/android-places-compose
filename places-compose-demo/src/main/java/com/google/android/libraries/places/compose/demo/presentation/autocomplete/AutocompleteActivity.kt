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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.demo.presentation.common.CommonEvent
import com.google.android.libraries.places.compose.demo.presentation.common.CommonScreen
import com.google.android.libraries.places.compose.demo.presentation.common.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * This activity demonstrates the use of Autocomplete, a feature that provides suggestions for
 * completing place names based on the user's input.
 *
 * The activity uses a ViewModel to manage the data and logic for the Autocomplete functionality.
 * The ViewModel observes the user's input and fetches predictions from a the Google Places API. The
 * activity then displays these predictions to the user.
 *
 * When the user selects a prediction, the activity displays the details of the selected place.
 */
@AndroidEntryPoint
class AutocompleteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val autocompleteViewModel: AutocompleteViewModel by viewModels()
        val commonViewModel: CommonViewModel by viewModels()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            CommonScreen(
                commonViewModel = commonViewModel,
                onNavigateUp = { finish() },
                snackbarHostState = snackbarHostState
            ) { paddingValues ->

                val commonViewState by commonViewModel.commonViewState.collectAsState()
                val autocompleteViewState by autocompleteViewModel.autocompleteViewState.collectAsState()

                AutocompleteDemo(
                    onQueryChanged = { query ->
                        autocompleteViewModel.onEvent(
                            AutocompleteEvent.OnQueryChanged(query) {
                                autocompleteViewModel.onEvent(
                                    AutocompleteEvent.OnQueryChanged(query) {
                                        locationBias = CircularBounds.newInstance(
                                            /* center = */ commonViewState.location,
                                            /* radius = */ 1000.meters.value
                                        )
                                        origin = commonViewState.location
                                        countries = listOf(commonViewState.countryCode)
                                    }
                                )
                            }
                        )
                    },
                    onPlaceSelected = {
                        autocompleteViewModel.onEvent(AutocompleteEvent.OnPlaceSelected(it))
                    },
                    modifier = Modifier.padding(paddingValues),
                    autocompleteViewState = autocompleteViewState,
                    onMapCloseClick = {
                        commonViewModel.onEvent(CommonEvent.OnMapCloseClicked)
                    },
                    location = commonViewState.location,
                    showMap = commonViewState.showMap,
                    locationLabel = commonViewState.locationLabel
                )
            }
        }
    }
}
