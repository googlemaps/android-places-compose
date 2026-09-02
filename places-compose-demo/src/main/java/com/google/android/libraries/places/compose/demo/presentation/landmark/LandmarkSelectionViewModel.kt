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
package com.google.android.libraries.places.compose.demo.presentation.landmark

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.compose.autocomplete.domain.mappers.toAddress
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.ReverseGeocodingResponse
import com.google.android.libraries.places.compose.demo.data.repositories.GeocoderRepository
import com.google.android.libraries.places.compose.demo.data.repositories.MergedLocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.PlaceRepository
import com.google.android.libraries.places.compose.demo.mappers.toNearbyObjects
import com.google.android.libraries.places.compose.demo.presentation.ViewModelEvent
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.toDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us.UsDisplayAddress
import com.google.maps.android.compose.MarkerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LandmarkMarker(
    val landmark: NearbyObject.NearbyLandmark,
    val marker: MarkerState,
    val latLng: LatLng,
)

/**
 * ViewModel for the landmark selection screen.
 *
 * @param geocoderRepository The repository for accessing address descriptor data.
 */
@SuppressLint("MissingPermission")
@HiltViewModel
class LandmarkSelectionViewModel
@Inject constructor(
    private val mergedLocationRepository: MergedLocationRepository,
    private val geocoderRepository: GeocoderRepository,
    private val placesRepository: PlaceRepository
) : ViewModel() {
    private var selectedNearbyObject by mutableStateOf<NearbyObject?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val location = mergedLocationRepository.location.map { it.latLng }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = LatLng(0.0, 0.0)
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _geocoderResult = MutableStateFlow<ReverseGeocodingResponse?>(null)

    private val nearbyObjects: StateFlow<List<NearbyObject>> = _geocoderResult.map { result ->
        result?.addressDescriptor?.toNearbyObjects() ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val nearbyObjectsWithLatLngs: StateFlow<List<Pair<NearbyObject, Place>>> = nearbyObjects.mapLatest { objects ->
        objects.map { nearbyObject ->
            viewModelScope.async { placesRepository.getPlaceLatLng(nearbyObject.placeId) }
        }.awaitAll().map { place ->
            objects.first { address -> address.placeId == place.first } to place.second
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = emptyList()
    )

    private val _displayAddress = MutableStateFlow<DisplayAddress?>(null)
    val displayAddress = _displayAddress.asStateFlow()

    private val _viewModelEventChannel = MutableSharedFlow<ViewModelEvent>()
    val viewModelEventChannel: SharedFlow<ViewModelEvent> = _viewModelEventChannel.asSharedFlow()

    init {
        viewModelScope.launch {
            location.collect { loc ->
                if (loc.latitude != 0.0 || loc.longitude != 0.0) {
                    _isLoading.value = true
                    try {
                        val response = geocoderRepository.reverseGeocode(loc, includeAddressDescriptors = true)
                        _geocoderResult.value = response
                        if (response != null) {
                            if (response.status == "OK") {
                                val addr = response.addresses.firstOrNull()?.let { address ->
                                    address.toAddress(address.getCountryCode() ?: "US").toDisplayAddress()
                                } ?: UsDisplayAddress()
                                _displayAddress.value = addr

                                val landmarkCount = response.addressDescriptor?.landmarks?.size ?: 0
                                if (landmarkCount > 0) {
                                    _viewModelEventChannel.emit(
                                        ViewModelEvent.UserMessage("Found $landmarkCount nearby landmarks.")
                                    )
                                }
                            } else {
                                val err = response.errorMessage ?: "Geocoding status: ${response.status}"
                                _viewModelEventChannel.emit(
                                    ViewModelEvent.UserMessage("Geocoding notice: $err")
                                )
                            }
                        } else {
                            _viewModelEventChannel.emit(
                                ViewModelEvent.UserMessage("Reverse geocoding request failed. Check network or API key restrictions.")
                            )
                        }
                    } catch (e: Exception) {
                        _viewModelEventChannel.emit(
                            ViewModelEvent.UserMessage("Geocoding error: ${e.localizedMessage ?: "Unknown"}")
                        )
                    } finally {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    val landmarkMarkers: StateFlow<List<LandmarkMarker>> = nearbyObjectsWithLatLngs.map { list ->
        list.filter {
            it.first is NearbyObject.NearbyLandmark
        }.mapNotNull { (nearbyObject, place) ->
            place.location?.let { latLng ->
                LandmarkMarker(
                    landmark = nearbyObject as NearbyObject.NearbyLandmark,
                    latLng = latLng,
                    marker = MarkerState(position = latLng)
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = emptyList()
    )

    /**
     * Handles events from the UI.
     */
    fun onEvent(event: LandmarkSelectionEvent) {
        when (event) {
            is LandmarkSelectionEvent.OnUserLocationChanged -> {
                viewModelScope.launch {
                    val lat = "%.4f".format(event.location.latitude)
                    val lng = "%.4f".format(event.location.longitude)
                    _viewModelEventChannel.emit(
                        ViewModelEvent.UserMessage("Pin set to ($lat, $lng). Looking up address...")
                    )
                }
                mergedLocationRepository.setMockLocation(event.location)
            }

            is LandmarkSelectionEvent.OnNearbyObjectSelected ->  {
                selectedNearbyObject = event.nearbyObject
            }

            is LandmarkSelectionEvent.OnAddressChanged -> {
                _displayAddress.value = event.address
            }

            LandmarkSelectionEvent.OnCloseAddressDisplayClicked -> {
                selectedNearbyObject = null
            }
        }
    }
}
