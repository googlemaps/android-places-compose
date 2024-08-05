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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.compose.autocomplete.components.PlacesAutocompleteTextField
import com.google.android.libraries.places.compose.autocomplete.data.LocalUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.getUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.ViewModelEvent
import com.google.android.libraries.places.compose.demo.presentation.autocomplete.AutocompleteEvent
import com.google.android.libraries.places.compose.demo.presentation.autocomplete.AutocompleteViewModel
import com.google.android.libraries.places.compose.demo.presentation.autocomplete.AutocompleteViewState
import com.google.android.libraries.places.compose.demo.presentation.components.GoogleMapContainer
import com.google.android.libraries.places.compose.demo.presentation.components.NextLocationButton
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.`in`.IndiaDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us.UsDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.components.AddressDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressCompletionScreen(
    viewModel: AddressCompletionViewModel,
    autocompleteViewModel: AutocompleteViewModel,
    onNavigateUp: () -> Unit = {}
) {
    val autocompleteViewState by autocompleteViewModel.autocompleteViewState.collectAsState()
    val addressValidationViewState by viewModel.viewState.collectAsState()

    var showAutocomplete by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val countryCode by viewModel.countryCode.collectAsState()

    // Respond to view model events, such as showing a snackbar.
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

    // Determine which units converter to use based on the country.
    val unitsConverter = remember(countryCode) {
        getUnitsConverter(countryCode)
    }

    CompositionLocalProvider(LocalUnitsConverter provides unitsConverter) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = { Text(stringResource(R.string.cart)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (showAutocomplete) {
                                showAutocomplete = false
                            } else {
                                onNavigateUp()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // TODO: viewModel is creeping into the UI.
                        IconButton(onClick = { viewModel.onEvent(AddressCompletionEvent.OnUseLocation) }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_my_location_24),
                                contentDescription = "Fill in the address from the current location"
                            )
                        }
                        NextLocationButton {
                            viewModel.onEvent(AddressCompletionEvent.OnNextMockLocation)
                        }
                        IconButton(onClick = { viewModel.onEvent(AddressCompletionEvent.OnToggleMap) }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_map_24),
                                contentDescription = "Toggle map"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            MainContent(
                addressValidationViewState = addressValidationViewState,
                autocompleteViewState = autocompleteViewState,
                onQueryChanged = { request, actions ->
                    autocompleteViewModel.onEvent(AutocompleteEvent.OnQueryChanged(request, actions))
                },
                onAddressSelected = { autocompletePlace ->
                    viewModel.onEvent(AddressCompletionEvent.OnAddressSelected(autocompletePlace))
                },
                showAutocomplete = showAutocomplete,
                onHideAutocomplete = {
                    showAutocomplete = false
                },
                onStreetFocused = {
                    // Need to snapshot the address here
                    showAutocomplete = true
                },
                countryCode = countryCode,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onAddressChanged = { displayAddress ->
                    viewModel.onEvent(AddressCompletionEvent.OnAddressChanged(displayAddress))
                },
                onMapClick = { latLng ->
                    viewModel.onEvent(AddressCompletionEvent.OnMapClicked(latLng))
                },
                onMapCloseClick = {
                    viewModel.onEvent(AddressCompletionEvent.OnMapCloseClicked)
                }
            )
        }
    }
}

@Composable
private fun MainContent(
    addressValidationViewState: AddressValidationViewState,
    autocompleteViewState: AutocompleteViewState,
    onQueryChanged: (String, FindAutocompletePredictionsRequest.Builder.() -> Unit) -> Unit,
    onAddressSelected: (AutocompletePlace) -> Unit,
    showAutocomplete: Boolean,
    onHideAutocomplete: () -> Unit,
    onStreetFocused: () -> Unit,
    countryCode: String,
    onAddressChanged: ((DisplayAddress) -> Unit),
    modifier: Modifier = Modifier,
    onMapClick: (LatLng) -> Unit = {},
    onMapCloseClick: () -> Unit = {}
) {
    when (addressValidationViewState) {
        is AddressValidationViewState.Error -> {
            Text(text = addressValidationViewState.message)
        }

        AddressValidationViewState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.loading))
            }
        }

        is AddressValidationViewState.AddressEntry -> {
            fun updateQuery(query: String) {
                onQueryChanged(query) {
                    locationBias = CircularBounds.newInstance(
                        /* center = */ addressValidationViewState.location,
                        /* radius = */ 1000.0
                    )
                    origin = addressValidationViewState.location
                    countries = listOf(countryCode)
                }
            }

            CartContent(
                showAutocomplete = showAutocomplete,
                modifier = modifier,
                autocompleteViewState = autocompleteViewState,
                onQueryChanged = { query ->
                    updateQuery(query)
                },
                onAddressSelected = onAddressSelected,
                onHideAutocomplete = onHideAutocomplete,
                addressValidationViewState = addressValidationViewState,
                onStreetFocused = {
                    // This set the initial query to the current address
                    updateQuery(addressValidationViewState.displayAddress.toFormattedAddress())
                    onStreetFocused()
                },
                onAddressChanged = onAddressChanged,
                onMapClick = onMapClick,
                onMapCloseClick = onMapCloseClick
            )
        }
    }
}

@Composable
private fun CartContent(
    showAutocomplete: Boolean,
    modifier: Modifier,
    autocompleteViewState: AutocompleteViewState,
    onQueryChanged: (String) -> Unit,
    onAddressSelected: (AutocompletePlace) -> Unit,
    onHideAutocomplete: () -> Unit,
    addressValidationViewState: AddressValidationViewState.AddressEntry,
    onStreetFocused: () -> Unit,
    onAddressChanged: ((DisplayAddress) -> Unit),
    onMapClick: (LatLng) -> Unit = {},
    onMapCloseClick: () -> Unit = {}
) {
    if (showAutocomplete) {
        val keyboardController = LocalSoftwareKeyboardController.current

        Column(modifier = modifier) {
            // TODO: make this compatible with the form and move it there instead.
            PlacesAutocompleteTextField(
                searchText = autocompleteViewState.searchText,
                predictions = autocompleteViewState.predictions,
                onQueryChanged = onQueryChanged,
                modifier = Modifier.weight(1f),
                onSelected = { autocompletePlace ->
                    onAddressSelected(autocompletePlace)
                    onHideAutocomplete()
                    keyboardController?.hide()
                },
                selectedPlace = autocompleteViewState.selectedPlace,
                textFieldMaxLines = 4,
                onBackClicked = onHideAutocomplete,
                scrollable = false,
                placeHolderText = stringResource(R.string.search_call_to_action)
            )
        }
    } else {
        AddressEntryForm(
            viewState = addressValidationViewState,
            onStreetFocused = onStreetFocused,
            modifier = modifier,
            onAddressChanged = onAddressChanged,
            onMapClick = onMapClick,
            onMapCloseClick = onMapCloseClick
        )
    }
}

@Composable
private fun AddressEntryForm(
    viewState: AddressValidationViewState.AddressEntry,
    onStreetFocused: () -> Unit,
    modifier: Modifier = Modifier,
    onAddressChanged: ((DisplayAddress) -> Unit)? = null,
    onMapClick: (LatLng) -> Unit = {},
    onMapCloseClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.delivery_information),
            style = MaterialTheme.typography.headlineSmall
        )

        Column(modifier = Modifier
            .weight(1f)
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState()),
        ) {
            AddressDisplay(
                address = viewState.displayAddress,
                modifier = Modifier.fillMaxWidth(),
                nearbyObjects = viewState.nearbyObjects,
                onAddressChanged = onAddressChanged,
                onStreetFocused = onStreetFocused
            )
        }
        if (viewState.showMap) {
            val label = viewState.locationLabel ?: stringResource(R.string.location_bias)

            LaunchedEffect(viewState.locationLabel) {
                val headerString = viewState.locationLabel ?: "Not set"
                println(headerString)
            }

            GoogleMapContainer(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                    .height(350.dp),
                header = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                markerLatLng = viewState.location,
                markerTitle = label,
                onMapClick = onMapClick,
                onMapCloseClick = onMapCloseClick
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_loading() {
    MainContent(
        addressValidationViewState = AddressValidationViewState.Loading,
        autocompleteViewState = AutocompleteViewState(),
        onQueryChanged = { _, _ -> },
        onAddressSelected = {},
        modifier = Modifier.fillMaxSize(),
        onStreetFocused = {},
        onHideAutocomplete = {},
        showAutocomplete = false,
        countryCode = "US",
        onAddressChanged = {}
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_us_addressEntry() {
    MainContent(
        addressValidationViewState = AddressValidationViewState.AddressEntry(
            displayAddress = UsDisplayAddress(
                streetAddress = "123 Main St",
                additionalAddressInfo = "Suite #110",
                city = "Boulder",
                state = "CO",
                zipCode = "80301",
                country = "United States",
                countryCode = "US",
            ),
            showMap = false,
            location = LatLng(40.01924246438453, -105.259858527573),
            locationLabel = "Boulder, CO"
        ),
        autocompleteViewState = AutocompleteViewState(),
        onQueryChanged =  { _, _ -> },
        onAddressSelected = {},
        modifier = Modifier.fillMaxSize(),
        onStreetFocused = {},
        onHideAutocomplete = {},
        showAutocomplete = false,
        countryCode = "US",
        onAddressChanged = {},
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_in_addressEntry() {
    MainContent(
        addressValidationViewState = AddressValidationViewState.AddressEntry(
            displayAddress = IndiaDisplayAddress(
                aptSuiteUnit = "Akshardham society 2/3",
                streetAddress = "Sarthana - Kamrej Road Sarthana Jakat Naka Nana Varachha",
                city = "Surat",
                state = "Gujarat",
                pinCode = "395006",
                country = "India",
                countryCode = "IN",
            ),
            showMap = false,
            location = LatLng(40.01924246438453, -105.259858527573),
            locationLabel = "India"
        ),
        autocompleteViewState = AutocompleteViewState(),
        onQueryChanged =  { _, _ -> },
        onAddressSelected = {},
        modifier = Modifier.fillMaxSize(),
        onStreetFocused = {},
        onHideAutocomplete = {},
        showAutocomplete = false,
        countryCode = "US",
        onAddressChanged = {},
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_us_addressEntry_with_map() {
    MainContent(
        addressValidationViewState = AddressValidationViewState.AddressEntry(
            displayAddress = UsDisplayAddress(
                streetAddress = "123 Main St",
                additionalAddressInfo = "Suite #110",
                city = "Boulder",
                state = "CO",
                zipCode = "80301",
                country = "United States",
                countryCode = "US",
            ),
            showMap = true,
            location = LatLng(40.01924246438453, -105.259858527573),
            locationLabel = "Boulder, CO"
        ),
        autocompleteViewState = AutocompleteViewState(),
        onQueryChanged =  { _, _ -> },
        onAddressSelected = {},
        modifier = Modifier.fillMaxSize(),
        onStreetFocused = {},
        onHideAutocomplete = {},
        showAutocomplete = false,
        countryCode = "US",
        onAddressChanged = {},
        onMapCloseClick = {}
    )
}
