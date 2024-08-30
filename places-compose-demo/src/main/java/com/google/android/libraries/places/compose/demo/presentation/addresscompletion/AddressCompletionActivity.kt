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
package com.google.android.libraries.places.compose.demo.presentation.addresscompletion

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.compose.demo.presentation.ViewModelEvent
import com.google.android.libraries.places.compose.demo.presentation.autocomplete.AutocompleteEvent
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.libraries.places.compose.demo.presentation.autocomplete.AutocompleteViewModel
import com.google.android.libraries.places.compose.demo.presentation.common.CommonEvent
import com.google.android.libraries.places.compose.demo.presentation.common.CommonScreen
import com.google.android.libraries.places.compose.demo.presentation.common.CommonViewModel

@AndroidEntryPoint
class AddressCompletionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val addressCompletionViewModel: AddressCompletionViewModel by viewModels()
        val autocompleteViewModel: AutocompleteViewModel by viewModels()
        val commonViewModel: CommonViewModel by viewModels()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            CommonScreen(
                commonViewModel = commonViewModel,
                onNavigateUp = { finish() },
                snackbarHostState = snackbarHostState
            ) { paddingValues ->
                val addressCompletionViewState by addressCompletionViewModel.addressCompletionViewState.collectAsState()
                val autocompleteViewState by autocompleteViewModel.autocompleteViewState.collectAsState()
                val commonViewState by commonViewModel.commonViewState.collectAsState()

                LaunchedEffect(Unit) {
                    addressCompletionViewModel.viewModelEventChannel.collect { event ->
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

                AddressCompletionMainContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    addressCompletionViewState = addressCompletionViewState,
                    autocompleteViewState = autocompleteViewState,
                    onAddressChanged = { displayAddress ->
                        addressCompletionViewModel.onEvent(AddressCompletionEvent.OnAddressChanged(displayAddress))
                    },
                    onMapClick = { latLng ->
                        addressCompletionViewModel.onEvent(AddressCompletionEvent.OnMapClicked(latLng))
                    },
                    onAddressSelected = { autocompletePlace ->
                        addressCompletionViewModel.onEvent(AddressCompletionEvent.OnAddressSelected(autocompletePlace))
                    },
                    onMapCloseClick = {
                        commonViewModel.onEvent(CommonEvent.OnMapCloseClicked)
                    },
                    commonViewState = commonViewState,
                    onQueryChanged = { query ->
                        autocompleteViewModel.onEvent(
                            AutocompleteEvent.OnQueryChanged(query) {
                                locationBias = CircularBounds.newInstance(
                                    /* center = */ commonViewState.location,
                                    /* radius = */ 1000.0
                                )
                                origin = commonViewState.location
                                countries = listOf(commonViewState.countryCode)
                            }
                        )
                    }
                )
            }
        }
    }
}
