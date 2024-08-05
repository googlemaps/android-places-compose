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
import androidx.activity.viewModels
import com.google.android.libraries.places.compose.demo.presentation.landmark.GetLocationPermission
import com.google.android.libraries.places.compose.demo.ui.theme.AndroidPlacesComposeDemoTheme
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

        setContent {
            AndroidPlacesComposeDemoTheme {
                GetLocationPermission {
                    val viewModel: AutocompleteViewModel by viewModels()
                    AutocompleteDemoScreen(viewModel) {
                        finish()
                    }
                }
            }
        }
    }
}
