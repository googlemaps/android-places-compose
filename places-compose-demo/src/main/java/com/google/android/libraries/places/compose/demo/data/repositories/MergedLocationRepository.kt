package com.google.android.libraries.places.compose.demo.data.repositories

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

data class CompositeLocation(
    val latLng: LatLng = LatLng(0.0, 0.0),
    val label: String? = null,
    val isMockLocation: Boolean = true,
)

class MergedLocationRepository
@Inject
constructor(
    private val locationRepository: LocationRepository,
    private val mockLocationRepository: MockLocationRepository,
) {
  private val _useMockLocation = MutableStateFlow(true)

  @SuppressLint("MissingPermission")
  @ExperimentalCoroutinesApi
  @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
  val location = _useMockLocation.flatMapLatest { useMockLocation ->
    if (useMockLocation) {
      mockLocationRepository.location.map {
        CompositeLocation(
            latLng = it.latLng,
            label = it.label,
            isMockLocation = true,
        )
      }
    } else {
      locationRepository.latestLocation.mapNotNull { latLng ->
        latLng?.let {
          CompositeLocation(
            latLng = it,
            label = "Current Location",
            isMockLocation = false,
          )
        }
      }
    }
  }

  /**
   * If we are already sending the mock location, advance to the next mock location, otherwise
   * switch to presenting the mock location.
   */
  fun nextMockLocation() {
    if (_useMockLocation.value) {
      mockLocationRepository.nextMockLocation()
    } else {
      _useMockLocation.value = true
    }
  }

  @ExperimentalCoroutinesApi
  @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
  fun useSystemLocation() {
    _useMockLocation.value = false
    locationRepository.updateLocation()
  }

  fun setMockLocation(latLng: LatLng) {
    mockLocationRepository.setMockLocation(latLng)
    _useMockLocation.value = true
  }
}