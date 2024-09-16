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
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.autocomplete.models.toPlaceDetails
import com.google.android.libraries.places.compose.demo.data.repositories.MergedLocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.PlaceRepository
import com.google.android.libraries.places.compose.demo.domain.usecases.GetAutocompletionPredictionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class AutocompleteViewState(
    val predictions: List<AutocompletePlace> = emptyList(),
    val searchText: String = "",
    val selectedPlace: AutocompletePlace? = null,
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
    private val mergedLocationRepository: MergedLocationRepository,
    private val placesRepository: PlaceRepository,
    private val getAutocompletionPredictionsUseCase: GetAutocompletionPredictionsUseCase,
) : ViewModel() {
    private val autocompleteDebounce = 500.milliseconds

    private val _searchText = MutableStateFlow("")
    private val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val actions =
        MutableStateFlow<FindAutocompletePredictionsRequest.Builder.() -> Unit> {}

    private val query = searchText.combine(actions) { text, actions ->
        AutocompleteQuery(text, actions)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val predictions = query
        .debounce(autocompleteDebounce)
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    val autocompleteViewState = combine(
        autocompletePlaces,
        searchText,
        selectedPlaceWithLocation,
    ) { autocompletePlaces, searchText, selectedPlaceWithLocation ->
        AutocompleteViewState(
            predictions = autocompletePlaces,
            searchText = searchText,
            selectedPlace = selectedPlaceWithLocation,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = AutocompleteViewState()
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
                } else {
                    _selectedPlace.value = event.autocompletePlace
                }
            }

            is AutocompleteEvent.OnQueryChanged -> {
                _searchText.value = event.query
                _selectedPlace.value = null
                actions.value = event.actions
            }

            is AutocompleteEvent.OnMapClicked -> {
                _selectedPlace.value = null
                mergedLocationRepository.setMockLocation(event.latLng)
            }
        }
    }
}
