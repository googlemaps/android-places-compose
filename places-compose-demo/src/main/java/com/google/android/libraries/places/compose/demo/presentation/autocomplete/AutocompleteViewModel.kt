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

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.autocomplete.models.toPlaceDetails
import com.google.android.libraries.places.compose.demo.data.repositories.GeocoderRepository
import com.google.android.libraries.places.compose.demo.data.repositories.LocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.MockLocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.PlaceRepository
import com.google.android.libraries.places.compose.demo.domain.usecases.GetAutocompletionPredictionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class AutocompleteViewState(
    val predictions: List<AutocompletePlace> = emptyList(),
    val searchText: String = "",
    val selectedPlace: AutocompletePlace? = null,

    @Deprecated("Move out of AutocompleteViewState")
    val showMap: Boolean = false,

    @Deprecated("Move out of AutocompleteViewState")
    val location: LatLng = LatLng(0.0, 0.0),

    @Deprecated("Move out of AutocompleteViewState")
    val locationLabel: String? = null,
)

// TODO: expose this from the view model instead of AutocompleteViewState
data class ViewState(
    val location: LatLng = LatLng(0.0, 0.0),
    val locationLabel: String = "",
    val showMap: Boolean = false,
    val autocompleteViewState: AutocompleteViewState = AutocompleteViewState()
)

private data class AutocompleteQuery(
    val query: String,
    val actions: FindAutocompletePredictionsRequest.Builder.() -> Unit = {}
)

/**
 * ViewModel for the Autocomplete feature.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class AutocompleteViewModel
@Inject
constructor(
    private val locationRepository: LocationRepository,
    private val mockLocationRepository: MockLocationRepository,
    private val placesRepository: PlaceRepository,
    private val geocoderRepository: GeocoderRepository,
    private val getAutocompletionPredictionsUseCase: GetAutocompletionPredictionsUseCase,
) : ViewModel() {
    private val _searchText = MutableStateFlow("")
    private val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val actions =
        MutableStateFlow<FindAutocompletePredictionsRequest.Builder.() -> Unit> {}

    private val query = searchText.combine(actions) { text, actions ->
        AutocompleteQuery(text, actions)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val predictions = query
        .debounce(AUTOCOMPLETE_DEBOUNCE)
        .mapLatest { query ->
            if (query.query.isNotBlank()) {
                getAutocompletionPredictionsUseCase(query.query, query.actions)
            } else {
                emptyList()
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), emptyList())

    private val autocompletePlaces = predictions.map { predictions ->
        predictions.map(AutocompletePrediction::toPlaceDetails)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), emptyList())

    private val _selectedPlace: MutableStateFlow<AutocompletePlace?> = MutableStateFlow(null)
    private val selectedPlace: StateFlow<AutocompletePlace?> = _selectedPlace.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPlaceWithLocation = selectedPlace.mapLatest { selectedPlace ->
        selectedPlace?.let { place ->
            placesRepository.getPlaceLatLng(place.placeId).second.latLng?.let { latLng ->
                place.copy(latLng = latLng)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _location = MutableStateFlow(mockLocationRepository.selectedMockLocation.value)
    val location = _location.asStateFlow()

    private val _locationLabel = MutableStateFlow<String?>(null)

    private val _showMap = MutableStateFlow(false)

    val countryCode = _location.map { location ->
        // Get the country code the user's location.
        location.let {
            geocoderRepository.reverseGeocode(
                location,
                includeAddressDescriptors = false
            ).addresses.firstOrNull()?.getCountryCode()
        } ?: "US"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "US"
    )

    val autocompleteViewState = combine(
        _location,
        autocompletePlaces,
        searchText,
        selectedPlaceWithLocation,
        _showMap,
        _locationLabel
    ) { location, autocompletePlaces, searchText, selectedPlaceWithLocation, showMap, locationLabel ->
        AutocompleteViewState(
            predictions = autocompletePlaces,
            searchText = searchText,
            selectedPlace = selectedPlaceWithLocation,
            location = location,
            showMap = showMap,
            locationLabel = locationLabel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = AutocompleteViewState()
    )

    val viewState = combine(
        autocompleteViewState,
        _locationLabel,
        _location,
        _showMap
    ) { autocompleteViewState, locationLabel, location, showMap ->
        ViewState(
            location = location,
            locationLabel = locationLabel ?: "Unlabeled location",
            showMap = showMap,
            autocompleteViewState = autocompleteViewState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = ViewState()
    )

    /**
     * Handles Autocomplete events.
     *
     * @param event The Autocomplete event.
     */
    @SuppressLint("MissingPermission")
    fun onEvent(event: AutocompleteEvent) {
        when (event) {
            is AutocompleteEvent.OnPlaceSelected -> {
                if (_selectedPlace.value == event.autocompletePlace) {
                    _selectedPlace.value = null
                    _showMap.value = false
                } else {
                    _selectedPlace.value = event.autocompletePlace
                    _showMap.value = true
                }
            }

            is AutocompleteEvent.OnQueryChanged -> {
                _searchText.value = event.query
                _selectedPlace.value = null
                actions.value = event.actions
            }

            AutocompleteEvent.OnNextMockLocation -> {
                val (label, location) = mockLocationRepository.nextMockLocation()
                _location.value = location
                _locationLabel.value = label
            }

            AutocompleteEvent.OnToggleMap -> {
                _showMap.value = !_showMap.value
            }

            is AutocompleteEvent.OnSetMapVisible -> {
                _showMap.value = event.visible
                if (!event.visible) {
                    _selectedPlace.value = null
                }
            }

            AutocompleteEvent.OnUseDeviceLocation -> {
                viewModelScope.launch {
                    _location.value = locationRepository.getLastLocation()
                    _locationLabel.value = "Current Location"
                }
            }
        }
    }

    companion object {
        // TODO: should be configurable
        private val AUTOCOMPLETE_DEBOUNCE = 500.milliseconds
    }
}

inline fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> {
    return combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
        )
    }
}
