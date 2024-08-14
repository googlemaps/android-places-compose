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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val viewState by viewModel.viewState.collectAsState()

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
                        SelectableButton(
                            viewState.buttonStates.currentLocation == ButtonState.SELECTED,
                            { viewModel.onEvent(AddressCompletionEvent.OnUseSystemLocation) },
                            R.drawable.baseline_my_location_24,
                            R.string.fill_address_from_current_location
                        )
                        NextLocationButton(
                            isSelected = viewState.buttonStates.mockLocation == ButtonState.SELECTED
                        ) {
                            viewModel.onEvent(AddressCompletionEvent.OnNextMockLocation)
                        }

                        SelectableButton(
                            isSelected = viewState.buttonStates.map == ButtonState.SELECTED,
                            iconId = R.drawable.baseline_map_24,
                            contentDescription = R.string.toggle_map,
                            onClick = { viewModel.onEvent(AddressCompletionEvent.OnToggleMap) }
                        )
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            MainContent(
                viewState = viewState,
                autocompleteViewState = autocompleteViewState,
                onQueryChanged = { request, actions ->
                    autocompleteViewModel.onEvent(AutocompleteEvent.OnQueryChanged(request, actions))
                },
                onAddressSelected = { autocompletePlace ->
                    viewModel.onEvent(AddressCompletionEvent.OnAddressSelected(autocompletePlace))
                },
                onHideAutocomplete = {
                    showAutocomplete = false
                },
                countryCode = countryCode,
                onAddressChanged = { displayAddress ->
                    viewModel.onEvent(AddressCompletionEvent.OnAddressChanged(displayAddress))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
private fun SelectableButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    iconId: Int,
    contentDescription: Int
) {
    IconButton(
        modifier = Modifier
            .then(
                if (isSelected) {
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                } else {
                    Modifier
                }
            ),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = stringResource(contentDescription),
        )
    }
}

@Composable
private fun MainContent(
    viewState: ViewState,
    autocompleteViewState: AutocompleteViewState,
    onQueryChanged: (String, FindAutocompletePredictionsRequest.Builder.() -> Unit) -> Unit,
    onAddressSelected: (AutocompletePlace) -> Unit,
    onHideAutocomplete: () -> Unit,
    countryCode: String,
    onAddressChanged: (DisplayAddress) -> Unit,
    modifier: Modifier = Modifier,
    onMapClick: (LatLng) -> Unit = {},
    onMapCloseClick: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    when (viewState.addressCompletionViewState) {
        is AddressCompletionViewState.Autocomplete -> {
            fun updateQuery(query: String) {
                onQueryChanged(query) {
                    locationBias = CircularBounds.newInstance(
                        /* center = */ viewState.location,
                        /* radius = */ 1000.0
                    )
                    origin = viewState.location
                    countries = listOf(countryCode)
                }
            }

            PlaceAutocompleteScreen(
                modifier = modifier,
                autocompleteViewState = autocompleteViewState,
                onQueryChanged = { query ->
                    updateQuery(query)
                },
                onAddressSelected = { autocompletePlace ->
                    onAddressSelected(autocompletePlace)
                    onHideAutocomplete()
                    keyboardController?.hide()
                },
                onHideAutocomplete = onHideAutocomplete
            )
        }

        is AddressCompletionViewState.AddressEntry -> {
            AddressEntryForm(
                viewState = viewState,
                addressEntryViewState = viewState.addressCompletionViewState,
                modifier = modifier,
                onAddressChanged = onAddressChanged,
                onMapClick = onMapClick,
                onMapCloseClick = onMapCloseClick
            )
        }
    }
}

@Composable
fun PlaceAutocompleteScreen(
    autocompleteViewState: AutocompleteViewState,
    modifier: Modifier,
    onQueryChanged: (String) -> Unit,
    onAddressSelected: (AutocompletePlace) -> Unit,
    onHideAutocomplete: () -> Unit,
) {
    PlacesAutocompleteTextField(
        modifier = modifier,
        searchText = autocompleteViewState.searchText,
        predictions = autocompleteViewState.predictions,
        onQueryChanged = onQueryChanged,
        onSelected = onAddressSelected,
        selectedPlace = autocompleteViewState.selectedPlace,
        textFieldMaxLines = 4,
        onBackClicked = onHideAutocomplete,
        scrollable = false,
        placeHolderText = stringResource(R.string.search_call_to_action)
    )
}

@Composable
private fun AddressEntryForm(
    addressEntryViewState: AddressCompletionViewState.AddressEntry,
    modifier: Modifier = Modifier,
    onAddressChanged: ((DisplayAddress) -> Unit)? = null,
    onMapClick: (LatLng) -> Unit = {},
    onMapCloseClick: () -> Unit = {},
    viewState: ViewState,
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
                address = addressEntryViewState.displayAddress,
                modifier = Modifier.fillMaxWidth(),
                nearbyObjects = addressEntryViewState.nearbyObjects,
                onAddressChanged = onAddressChanged
            )
        }
        if (viewState.showMap) {
            LaunchedEffect(viewState.locationLabel) {
                val headerString = viewState.locationLabel
                println(headerString)
            }

            GoogleMapContainer(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                    .height(350.dp),
                header = {
                    Text(
                        text = viewState.locationLabel,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                markerLatLng = viewState.location,
                markerTitle = viewState.locationLabel,
                onMapClick = onMapClick,
                onMapCloseClick = onMapCloseClick
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
private fun CartContentPreview_autocomplete() {
    MainContent(
        viewState = ViewState(),
        autocompleteViewState = AutocompleteViewState(),
        onQueryChanged =  { _, _ -> },
        onAddressSelected = {},
        onHideAutocomplete = {},
        countryCode = "US",
        onAddressChanged = {},
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_us_addressEntry() {
    MainContent(
        viewState = ViewState(
            showMap = false,
            location = LatLng(40.01924246438453, -105.259858527573),
            locationLabel = "Boulder, CO",
            addressCompletionViewState = AddressCompletionViewState.AddressEntry(
                displayAddress = UsDisplayAddress(
                    streetAddress = "123 Main St",
                    additionalAddressInfo = "Suite #110",
                    city = "Boulder",
                    state = "CO",
                    zipCode = "80301",
                    country = "United States",
                    countryCode = "US",
                ),
            )
        ),
        autocompleteViewState = AutocompleteViewState(),
        onQueryChanged =  { _, _ -> },
        onAddressSelected = {},
        onHideAutocomplete = {},
        countryCode = "US",
        onAddressChanged = {},
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_in_addressEntry() {
    MainContent(
        viewState = ViewState(
            showMap = false,
            location = LatLng(21.233980841304085, 72.9069776200368),
            locationLabel = "Akshar Dham Temple",
            addressCompletionViewState = AddressCompletionViewState.AddressEntry(
                displayAddress = IndiaDisplayAddress(
                    aptSuiteUnit = "Akshardham society 2/3",
                    streetAddress = "Sarthana - Kamrej Road Sarthana Jakat Naka Nana Varachha",
                    city = "Surat",
                    state = "Gujarat",
                    pinCode = "395006",
                    country = "India",
                    countryCode = "IN",
                ),
            )
        ),
        autocompleteViewState = AutocompleteViewState(),
        onQueryChanged =  { _, _ -> },
        onAddressSelected = {},
        onHideAutocomplete = {},
        countryCode = "IN",
        onAddressChanged = {},
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_us_addressEntry_with_map() {
    MainContent(
        viewState = ViewState(
            showMap = true,
            location = LatLng(40.01924246438453, -105.259858527573),
            locationLabel = "Boulder, CO",
            addressCompletionViewState = AddressCompletionViewState.AddressEntry(
                displayAddress = UsDisplayAddress(
                    streetAddress = "123 Main St",
                    additionalAddressInfo = "Suite #110",
                    city = "Boulder",
                    state = "CO",
                    zipCode = "80301",
                    country = "United States",
                    countryCode = "US",
                ),
            )
        ),
        autocompleteViewState = AutocompleteViewState(),
        onQueryChanged =  { _, _ -> },
        onAddressSelected = {},
        onHideAutocomplete = {},
        countryCode = "US",
        onAddressChanged = {},
        modifier = Modifier.fillMaxSize(),
        onMapCloseClick = {}
    )
}
