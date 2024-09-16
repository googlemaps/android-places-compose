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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.components.PlacesAutocompleteTextField
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.autocomplete.AutocompleteViewState
import com.google.android.libraries.places.compose.demo.presentation.common.CommonViewState
import com.google.android.libraries.places.compose.demo.presentation.components.GoogleMapContainer
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.`in`.IndiaDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us.UsDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.components.AddressDisplay

@Composable
fun AddressCompletionMainContent(
    modifier: Modifier,
    addressCompletionViewState: AddressCompletionViewState,
    autocompleteViewState: AutocompleteViewState,
    onAddressChanged: (DisplayAddress) -> Unit,
    onMapClick: (LatLng) -> Unit,
    onAddressSelected: (AutocompletePlace) -> Unit,
    onMapCloseClick: () -> Unit,
    commonViewState: CommonViewState = CommonViewState(),
    onQueryChanged: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
    ) {
        when (addressCompletionViewState) {
            is AddressCompletionViewState.Autocomplete -> {
                PlaceAutocompleteScreen(
                    autocompleteViewState = autocompleteViewState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onQueryChanged = onQueryChanged,
                    onAddressSelected = { autocompletePlace ->
                        keyboardController?.hide()
                        onAddressSelected(autocompletePlace)
                    }
                )
            }

            is AddressCompletionViewState.AddressEntry -> {
                AddressEntryForm(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onAddressChanged = onAddressChanged,
                    addressEntry = addressCompletionViewState
                )
            }
        }

        if (commonViewState.showMap) {
            val label = commonViewState.locationLabel ?: stringResource(R.string.unlabeled_location)
            GoogleMapContainer(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                    .weight(1f),
                header = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                markerLatLng = commonViewState.location,
                markerTitle = label,
                onMapClick = onMapClick,
                onMapCloseClick = onMapCloseClick,
            )
        }
    }
}

@Composable
fun PlaceAutocompleteScreen(
    autocompleteViewState: AutocompleteViewState,
    modifier: Modifier,
    onQueryChanged: (String) -> Unit,
    onAddressSelected: (AutocompletePlace) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        PlacesAutocompleteTextField(
            searchText = autocompleteViewState.searchText,
            predictions = autocompleteViewState.predictions,
            onQueryChanged = onQueryChanged,
            onSelected = onAddressSelected,
            selectedPlace = autocompleteViewState.selectedPlace,
            textFieldMaxLines = 4,
            scrollable = true,
            placeHolderText = stringResource(R.string.search_call_to_action)
        )
    }
}

@Composable
private fun AddressEntryForm(
    modifier: Modifier = Modifier,
    onAddressChanged: ((DisplayAddress) -> Unit)? = null,
    addressEntry: AddressCompletionViewState.AddressEntry,
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
                address = addressEntry.displayAddress,
                modifier = Modifier.fillMaxWidth(),
                nearbyObjects = addressEntry.nearbyObjects,
                onAddressChanged = onAddressChanged,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
private fun CartContentPreview_autocomplete() {
    PlaceAutocompleteScreen(
        autocompleteViewState = AutocompleteViewState(),
        modifier = Modifier.fillMaxSize(),
        onQueryChanged = { },
        onAddressSelected = { }
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_us_addressEntry() {
    AddressEntryForm(
        onAddressChanged = {},
        addressEntry = AddressCompletionViewState.AddressEntry(
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
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun CartContentPreview_in_addressEntry() {
    AddressEntryForm(
        onAddressChanged = {},
        addressEntry = AddressCompletionViewState.AddressEntry(
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
    )
}
