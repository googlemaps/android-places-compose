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
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.domain.mappers.toAddress
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.demo.data.repositories.GeocoderRepository
import com.google.android.libraries.places.compose.demo.data.repositories.MergedLocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.PlaceRepository
import com.google.android.libraries.places.compose.demo.mappers.toNearbyObjects
import com.google.android.libraries.places.compose.demo.presentation.ViewModelEvent
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.toDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us.UsDisplayAddress
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

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
    val location = mergedLocationRepository.location.map { it.latLng }.onEach {
        Log.e("LandmarkSelectionViewModel", "Location changed: $it")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = LatLng(0.0, 0.0)
    )

    private val geocoderResult = location.filterNotNull().mapNotNull { location ->
        Log.e("LandmarkSelectionViewModel", "Geocoding location: $location")
        geocoderRepository.reverseGeocode(location, includeAddressDescriptors = true)
    }

    private val nearbyObjects = geocoderResult.mapNotNull { result ->
        Log.e("LandmarkSelectionViewModel", "Nearby objects: ${result.addressDescriptor}")
        result.addressDescriptor?.toNearbyObjects()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val nearbyObjectsWithLatLngs = nearbyObjects.mapLatest { nearbyObjects ->
        Log.e("LandmarkSelectionViewModel", "Getting latlngs for nearby objects: $nearbyObjects")

        val places = nearbyObjects.map { nearbyObject ->
            viewModelScope.async { placesRepository.getPlaceLatLng(nearbyObject.placeId) }
        }

        places.awaitAll().map { place ->
            nearbyObjects.first { address -> address.placeId == place.first } to place.second
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val displayAddress = geocoderResult.mapLatest { geocoderDto ->
        geocoderDto.addresses.firstOrNull()?.let { address ->
            address.toAddress(address.getCountryCode() ?: "US").toDisplayAddress()
        } ?: UsDisplayAddress()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = null
    )

    private val _viewModelEventChannel = MutableSharedFlow<ViewModelEvent>()
    val viewModelEventChannel: SharedFlow<ViewModelEvent> = _viewModelEventChannel.asSharedFlow()

    /**
     * Handles events from the UI.
     */
    fun onEvent(event: LandmarkSelectionEvent) {
        when (event) {
            is LandmarkSelectionEvent.OnUserLocationChanged -> {
                mergedLocationRepository.setMockLocation(event.location)
            }

            is LandmarkSelectionEvent.OnNearbyObjectSelected ->  {
                selectedNearbyObject = event.nearbyObject
            }

            LandmarkSelectionEvent.OnCloseAddressDisplayClicked -> {
                selectedNearbyObject = null
            }
        }
    }
}
