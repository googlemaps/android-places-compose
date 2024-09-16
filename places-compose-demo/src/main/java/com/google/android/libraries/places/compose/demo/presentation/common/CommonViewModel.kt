package com.google.android.libraries.places.compose.demo.presentation.common

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.models.Address
import com.google.android.libraries.places.compose.demo.data.repositories.CompositeLocation
import com.google.android.libraries.places.compose.demo.data.repositories.GeocoderRepository
import com.google.android.libraries.places.compose.demo.data.repositories.MergedLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import com.google.maps.android.ktx.utils.sphericalDistance
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull

enum class ButtonState {
    NORMAL, SELECTED
}

data class ButtonStates(
    val currentLocation: ButtonState = ButtonState.NORMAL,
    val mockLocation: ButtonState = ButtonState.NORMAL,
    val map: ButtonState = ButtonState.NORMAL
)

data class CommonViewState(
    val location: LatLng = LatLng(0.0, 0.0),
    val locationLabel: String? = null,
    val buttonStates: ButtonStates = ButtonStates(),
    val showMap: Boolean = false,
    val countryCode: String? = null
)

@HiltViewModel
class CommonViewModel
@Inject constructor(
    private val geocoderRepository: GeocoderRepository,
    private val mergedLocationRepository: MergedLocationRepository
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val location = mergedLocationRepository.location.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = CompositeLocation()
    )

    private var lastGeocodedLocation: LatLng? = null

    private val geocoderResult = location
        .filter { location ->
            lastGeocodedLocation == null || lastGeocodedLocation?.sphericalDistance(location.latLng)!! > 50
        }.mapNotNull { location ->
            lastGeocodedLocation = location.latLng
            geocoderRepository.reverseGeocode(location.latLng, includeAddressDescriptors = true)
        }

    private val _showMap = MutableStateFlow(false)
    private val showMap = _showMap.asStateFlow()

    private val currentAddress = geocoderResult.mapNotNull { result ->
        result.addresses.firstOrNull()
    }

    // Determine the country code based on the location not the address.
    private val countryCode = currentAddress.filterNotNull().mapNotNull { address ->
        address.getCountryCode()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = "US"
    )

    private var buttonStates = combine(
        location,
        showMap
    ) { location, showMap ->
        ButtonStates(
            currentLocation = if (location.isMockLocation) ButtonState.NORMAL else ButtonState.SELECTED,
            mockLocation = if (location.isMockLocation) ButtonState.SELECTED else ButtonState.NORMAL,
            map = if (showMap) ButtonState.SELECTED else ButtonState.NORMAL
        )
    }

    val commonViewState = combine(
        location,
        buttonStates,
        showMap,
        countryCode
    ) { location, buttonStates, showMap, countryCode ->
        CommonViewState(
            location = location.latLng,
            locationLabel = location.label,
            buttonStates = buttonStates,
            showMap = showMap,
            countryCode = countryCode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = CommonViewState()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    fun onEvent(event: CommonEvent) {
        when (event) {
            is CommonEvent.OnNextMockLocation -> {
                mergedLocationRepository.nextMockLocation()
            }
            is CommonEvent.OnToggleMap -> {
                _showMap.value = !_showMap.value
            }
            is CommonEvent.OnUseSystemLocation -> {
                mergedLocationRepository.useSystemLocation()
            }
            is CommonEvent.SetMapVisible -> {
                _showMap.value = event.visible
            }
        }
    }
}
