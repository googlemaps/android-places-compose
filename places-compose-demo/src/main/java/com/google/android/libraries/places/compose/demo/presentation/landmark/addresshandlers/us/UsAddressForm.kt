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
package com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.DisplayName
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Landmark
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.data.repositories.CountriesRepository
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.AddressTextField
import com.google.android.libraries.places.compose.demo.ui.theme.AndroidPlacesComposeDemoTheme
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.components.NearbyObjectsSelector

@Composable
fun UsAddressForm(
    modifier: Modifier,
    address: UsDisplayAddress,
    nearbyObjects: List<NearbyObject>,
    onNearbyLandmarkSelected: (String?) -> Unit = {},
    onAddressChanged: ((DisplayAddress) -> Unit)? = null,
    selectedPlaceId: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AddressTextField(
            value = address.streetAddress,
            label = R.string.us_address_address,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = onAddressChanged?.let {
                { onAddressChanged.invoke(address.copy(streetAddress = it)) }
            }
        )

        AddressTextField(
            value = address.additionalAddressInfo,
            label = R.string.us_address_suite_unit_floor,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = onAddressChanged?.let {
                { onAddressChanged.invoke(address.copy(additionalAddressInfo = it)) }
            },
        )

        AddressTextField(
            value = address.city,
            label = R.string.us_address_city,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = onAddressChanged?.let {
                { onAddressChanged.invoke(address.copy(city = it)) }
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AddressTextField(
                value = address.state,
                label = R.string.us_address_state,
                modifier = Modifier.weight(1f),
                onValueChange = onAddressChanged?.let {
                    { onAddressChanged.invoke(address.copy(state = it)) }
                },
            )
            AddressTextField(
                value = address.zipCode,
                label = R.string.us_address_zip,
                modifier = Modifier.weight(1f),
                onValueChange = onAddressChanged?.let {
                    { onAddressChanged.invoke(address.copy(zipCode = it)) }
                },
            )
        }

        if (nearbyObjects.isNotEmpty()) {
            NearbyObjectsSelector(
                nearbyObjects = nearbyObjects,
                onNearbyLandmarkSelected = onNearbyLandmarkSelected,
                selectedPlaceId = selectedPlaceId
            )
        }

        AddressTextField(
            value = address.country,
            label = R.string.us_address_country,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = CountriesRepository.countries[address.countryCode]?.flag?.let {
                { Text(it) }
            },
            onValueChange = onAddressChanged?.let {
                { onAddressChanged.invoke(address.copy(country = it)) }
            }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun USAddressPreviewNoNearbyObjects() {
    AndroidPlacesComposeDemoTheme {
        UsAddressForm(
            modifier = Modifier.fillMaxWidth(),
            address = UsDisplayAddress(
                streetAddress = "123 Main St",
                additionalAddressInfo = "Suite #110",
                city = "New York",
                state = "NY",
                zipCode = "10001",
                countryCode = "US",
                country = "United States"
            ),
            nearbyObjects = emptyList(),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun USAddressPreviewWithNearbyObjects() {
    AndroidPlacesComposeDemoTheme {
        Surface {
            UsAddressForm(
                modifier = Modifier.fillMaxWidth(),
                address = UsDisplayAddress(
                    streetAddress = "123 Main St",
                    additionalAddressInfo = "Suite #110",
                    city = "New York",
                    state = "NY",
                    zipCode = "10001",
                    countryCode = "US",
                    country = "United States",
                ),
                nearbyObjects = listOf(
                    NearbyObject.NearbyLandmark(
                        Landmark(
                            placeId = "placeId",
                            displayName = DisplayName(
                                languageCode = "en",
                                text = "Landmark"
                            ),
                            spatialRelationship = "",
                            straightLineDistanceMeters = 1000.0,
                            travelDistanceMeters = 1000.0,
                            types = emptyList()
                        )
                    )
                ),
            )
        }
    }
}
