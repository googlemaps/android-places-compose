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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.domain.mappers.toAddress
import com.google.android.libraries.places.compose.autocomplete.models.Address
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.demo.data.repositories.GeocoderRepository
import com.google.android.libraries.places.compose.demo.data.repositories.LocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.MockLocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.PlaceRepository
import com.google.android.libraries.places.compose.demo.mappers.toNearbyObjects
import com.google.android.libraries.places.compose.demo.presentation.ViewModelEvent
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.toDisplayAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

sealed class AddressValidationViewState {
    data object Loading : AddressValidationViewState()
    data class Error(val message: String) : AddressValidationViewState()
    data class AddressEntry(
        val displayAddress: DisplayAddress,
        val showMap: Boolean,
        val location: LatLng,
        val locationLabel: String?,
        val nearbyObjects: List<NearbyObject> = emptyList(),
    ) : AddressValidationViewState()
}

@HiltViewModel
class AddressCompletionViewModel
@Inject constructor(
    private val geocoderRepository: GeocoderRepository,
    private val mockLocationRepository: MockLocationRepository,
    private val locationRepository: LocationRepository,
    private val placesRepository: PlaceRepository,
) : ViewModel() {
    private val _address = MutableStateFlow<Address?>(null)
    val address = _address.asStateFlow()

    private val _viewModelEventChannel = MutableSharedFlow<ViewModelEvent>()
    val viewModelEventChannel: SharedFlow<ViewModelEvent> = _viewModelEventChannel.asSharedFlow()
    private val _location = MutableStateFlow<LatLng?>(null)
    private val _locationLabel = MutableStateFlow<String?>(null)
    private val _displayAddress = MutableStateFlow<DisplayAddress?>(null)
    private val _showMap = MutableStateFlow(false)

    private val geocoderResult = _location.filterNotNull().mapNotNull { location ->
        geocoderRepository.reverseGeocode(location, includeAddressDescriptors = true)
    }

    private val _currentAddress = geocoderResult.mapNotNull { result ->
        result.addresses.firstOrNull()
    }

    // Determine the country code based on the location not the address.
    val countryCode = _currentAddress.filterNotNull().mapNotNull { address ->
        address.getCountryCode()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = "US"
    )

    private val _nearbyObjects = geocoderResult.filterNotNull().mapNotNull { result ->
        result.addressDescriptor?.toNearbyObjects()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = emptyList()
    )

    val viewState = combine(
        _displayAddress,
        _showMap,
        _location,
        _locationLabel,
        _nearbyObjects
    ) { displayAddress, showMap, location, locationLabel, nearbyObjects ->

        if (displayAddress == null || location == null) {
            AddressValidationViewState.Loading
        } else {
            AddressValidationViewState.AddressEntry(
                displayAddress = displayAddress,
                showMap = showMap,
                location = location,
                locationLabel = locationLabel,
                nearbyObjects = nearbyObjects
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = AddressValidationViewState.Loading
    )

    init {
        viewModelScope.launch {
            _location.value = mockLocationRepository.selectedMockLocation.value
            _locationLabel.value = mockLocationRepository.labeledLocation.value.first
        }

        viewModelScope.launch {
            // We have to update the _displayAddress using a collect because it can also come from the UI.
            _currentAddress.filterNotNull().collect { address ->
                _displayAddress.value = address.toAddress(address.getCountryCode() ?: "US")
                    .toDisplayAddress()
            }
        }
    }

    fun onEvent(event: AddressCompletionEvent) {
        when (event) {
            is AddressCompletionEvent.OnAddressSelected -> {
                event.autocompletePlace.placeId

                viewModelScope.launch {
                    val place = placesRepository.getPlaceAddress(event.autocompletePlace.placeId)

                    _location.value = place.latLng
                    _locationLabel.value = place.name ?: place.address

                    place.addressComponents?.asList()?.toAddress()?.toDisplayAddress()?.let {
                        _displayAddress.value = it
                    }
                }
            }

            AddressCompletionEvent.OnCurrentLocationClick -> {
                reverseGeocodeCurrentLocation()
            }

            AddressCompletionEvent.OnNextMockLocation -> {
                val (label, location) = mockLocationRepository.nextMockLocation()
                _location.value = location
                _locationLabel.value = label
            }

            AddressCompletionEvent.OnToggleMap -> {
                _showMap.value = !_showMap.value
            }

            AddressCompletionEvent.OnUseLocation -> {
                viewModelScope.launch {
                    _location.value = locationRepository.getLastLocation()
                    _locationLabel.value = "Current Location"
                }
            }

            is AddressCompletionEvent.OnAddressChanged -> {
                _displayAddress.value = event.displayAddress
            }

            is AddressCompletionEvent.OnMapClicked -> {
                _location.value = event.latLng
                // TODO: to fix this we will need to add a UiText class to wrap the string
                _locationLabel.value = "Dropped Pin"
            }

            AddressCompletionEvent.OnMapCloseClicked -> {
                _showMap.value = false
            }
        }
    }

    private fun reverseGeocodeCurrentLocation() {
        viewModelScope.launch {
            locationRepository.getLastLocation().let { location ->
                val address = geocoderRepository.reverseGeocode(
                    location,
                    includeAddressDescriptors = true
                ).addresses.firstOrNull()
                val countryCode = address?.getCountryCode()
                if (countryCode != null) {
                    _address.value = address.toAddress(countryCode)
                }
            }
        }
    }
}
