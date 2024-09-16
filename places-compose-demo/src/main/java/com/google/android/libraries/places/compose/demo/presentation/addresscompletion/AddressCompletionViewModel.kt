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

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.compose.autocomplete.domain.mappers.toAddress
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.demo.data.repositories.MergedLocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.GeocoderRepository
import com.google.android.libraries.places.compose.demo.data.repositories.PlaceRepository
import com.google.android.libraries.places.compose.demo.mappers.toNearbyObjects
import com.google.android.libraries.places.compose.demo.presentation.ViewModelEvent
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.toDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us.UsDisplayAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

enum class UiState {
    AUTOCOMPLETE, ADDRESS_ENTRY
}

sealed class AddressCompletionViewState {
    data object Autocomplete : AddressCompletionViewState()

    data class AddressEntry(
        val displayAddress: DisplayAddress = UsDisplayAddress(),
        val nearbyObjects: List<NearbyObject> = emptyList(),
    ) : AddressCompletionViewState()
}

@HiltViewModel
class AddressCompletionViewModel
@Inject constructor(
    private val geocoderRepository: GeocoderRepository,
    private val placesRepository: PlaceRepository,
    private val mergedLocationRepository: MergedLocationRepository
) : ViewModel() {
    private val _viewModelEventChannel = MutableSharedFlow<ViewModelEvent>()
    val viewModelEventChannel: SharedFlow<ViewModelEvent> = _viewModelEventChannel.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val geocoderResult = mergedLocationRepository.location.mapNotNull { location ->
        // TODO: require a certain amount of change from the last location before geocoding.
        geocoderRepository.reverseGeocode(location.latLng, includeAddressDescriptors = true)
    }

    private val _nearbyObjects = geocoderResult.filterNotNull().mapNotNull { result ->
        result.addressDescriptor?.toNearbyObjects()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState.AUTOCOMPLETE)

    private val displayAddress = MutableStateFlow<DisplayAddress?>(null)

    private val _addressEntryViewState = combine(
        displayAddress,
        _nearbyObjects
    ) { displayAddress, nearbyObjects ->
        AddressCompletionViewState.AddressEntry(
            displayAddress = displayAddress ?: UsDisplayAddress(),
            nearbyObjects = nearbyObjects
        )
    }

    val addressCompletionViewState = combine(
        _addressEntryViewState,
        _uiState
    ) { addressEntryViewState, uiState ->
        when (uiState) {
            UiState.ADDRESS_ENTRY -> addressEntryViewState
            UiState.AUTOCOMPLETE -> AddressCompletionViewState.Autocomplete
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        AddressCompletionViewState.Autocomplete
    )

    @SuppressLint("MissingPermission")
    fun onEvent(event: AddressCompletionEvent) {
        when (event) {
            is AddressCompletionEvent.OnAddressSelected -> {
                viewModelScope.launch {
                    val place = placesRepository.getPlaceAddress(event.autocompletePlace.placeId)

                    place.addressComponents?.asList()?.toAddress()?.toDisplayAddress()?.let {
                        displayAddress.value = it
                    }

                    _uiState.value = UiState.ADDRESS_ENTRY
                }
            }
            is AddressCompletionEvent.OnAddressChanged -> {
                displayAddress.value = event.displayAddress
            }

            is AddressCompletionEvent.OnMapClicked -> {
                mergedLocationRepository.setMockLocation(event.latLng)
            }

            AddressCompletionEvent.OnNavigateUp -> reset()
        }
    }

    private fun reset() {
        displayAddress.value = null
    }
}
