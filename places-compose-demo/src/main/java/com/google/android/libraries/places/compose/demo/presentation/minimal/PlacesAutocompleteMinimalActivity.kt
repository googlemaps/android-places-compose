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
package com.google.android.libraries.places.compose.demo.presentation.minimal

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.compose.autocomplete.components.PlacesAutocompleteTextField
import com.google.android.libraries.places.compose.autocomplete.data.LocalUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.Meters
import com.google.android.libraries.places.compose.autocomplete.data.getUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.autocomplete.models.toPlaceDetails
import com.google.android.libraries.places.compose.demo.BuildConfig
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.data.repositories.ApiKeyProvider
import com.google.android.libraries.places.compose.demo.data.repositories.GeocoderRepository
import com.google.android.libraries.places.compose.demo.data.repositories.LocationRepository
import com.google.android.libraries.places.compose.demo.presentation.landmark.GetLocationPermission
import com.google.android.libraries.places.compose.demo.ui.theme.AndroidPlacesComposeDemoTheme
import com.google.android.libraries.places.ktx.api.net.awaitFindAutocompletePredictions
import com.google.maps.android.ktx.utils.withSphericalOffset
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Duration.Companion.milliseconds

class PlacesAutocompleteMinimalActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class, FlowPreview::class,
        DelicateCoroutinesApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.PLACES_API_KEY)
        val placesClient = Places.createClient(this)

        // These should normally be injected by DI and held in a view model
        val locationRepository = LocationRepository(this, GlobalScope)
        val apiKeyProvider = ApiKeyProvider(this)
        val geocoder = GeocoderRepository(this, apiKeyProvider)

        val locationFlow = MutableStateFlow<LatLng?>(null)
        val searchTextFlow = MutableStateFlow("")
        val deviceCountry = resources.configuration.locales.get(0).country

        setContent {
            AndroidPlacesComposeDemoTheme {
                GetLocationPermission {
                    val location by locationFlow.collectAsStateWithLifecycle()

                    // We just want to get the last location once.
                    LaunchedEffect(Unit) {
                        locationFlow.value = locationRepository.getLastLocation()
                    }

                    val searchText by searchTextFlow.collectAsStateWithLifecycle()

                    val country by remember {
                        locationFlow.mapNotNull { location ->
                            location?.let {
                                geocoder.reverseGeocode(it).addresses.firstOrNull()
                                    ?.getCountryCode() ?: deviceCountry
                            }
                        }
                    }.collectAsState(initial = deviceCountry)

                    // Determine which units converter to use based on the country.
                    val unitsConverter = remember(country) {
                        getUnitsConverter(country)
                    }

                    // TODO: this feels like it would make a great UseCase.
                    val predictions by remember(country, location) {
                        searchTextFlow.debounce(500.milliseconds).map { query ->
                            if (query.isBlank()) {
                                emptyList()
                            } else {
                                placesClient.awaitFindAutocompletePredictions {
                                    origin = location
                                    locationBias = location?.toRectangularBounds()
                                    typesFilter = listOf(PlaceTypes.ESTABLISHMENT)
                                    this.query = query
                                    countries = listOf(country)
                                }.autocompletePredictions.map { it.toPlaceDetails() }
                            }
                        }
                    }.collectAsState(initial = emptyList())

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Places Autocomplete") }
                            )
                        }
                    ) { paddingValues ->
                        CompositionLocalProvider(LocalUnitsConverter provides unitsConverter) {

                            if (location == null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                ) {
                                    Text(
                                        modifier = Modifier.align(Center),
                                        text = stringResource(R.string.getting_current_location),
                                    )
                                }
                            } else {
                                PlacesAutocompleteTextField(
                                    searchText = searchText,
                                    predictions = predictions,
                                    onQueryChanged = { searchTextFlow.value = it },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues),
                                    onSelected = { autocompletePlace ->
                                        // Handle the selected place
                                        Toast.makeText(
                                            this@PlacesAutocompleteMinimalActivity,
                                            "Selected: ${autocompletePlace.primaryText}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    scrollable = false,
                                    placeHolderText = stringResource(R.string.search_call_to_action),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Creates a rectangular bounds around this LatLng by offsetting it by a given radius.
 *
 * @param offset The distance in meters for the radius of the bounds. Defaults to 1000 meters.
 * @return A RectangularBounds object representing the area around this LatLng.
 */
private fun LatLng.toRectangularBounds(offset: Meters = 1000.meters): RectangularBounds {
    return RectangularBounds.newInstance(
        withSphericalOffset(offset.value, -135.0), // SW lat, lng
        withSphericalOffset(offset.value, 45.0), // NE lat, lng
    )
}
