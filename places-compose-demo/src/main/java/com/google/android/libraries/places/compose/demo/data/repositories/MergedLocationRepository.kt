package com.google.android.libraries.places.compose.demo.data.repositories

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

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
    scope: CoroutineScope
) {
  private val _useMockLocation = MutableStateFlow(true)

  val mergedLocation = merge(
    locationRepository.latestLocation.mapNotNull { latLng ->
      latLng?.let {
        CompositeLocation(
          latLng = it,
          label = "Current Location",
          isMockLocation = false,
        )
      }
    },
    mockLocationRepository.location.map {
      CompositeLocation(
        latLng = it.latLng,
        label = it.label,
        isMockLocation = true,
      )
    },
  )

  @SuppressLint("MissingPermission")
  @ExperimentalCoroutinesApi
  @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
  val location = _useMockLocation.flatMapLatest { useMockLocation ->
    if (useMockLocation) {
      Log.d("MergedLocationRepository", "Emitting mock location")
      mockLocationRepository.location.map {
        CompositeLocation(
            latLng = it.latLng,
            label = it.label,
            isMockLocation = true,
        )
      }
    } else {
      Log.d("MergedLocationRepository", "Emitting system location")
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
  }.shareIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(5.seconds),
    replay = 1,
  )

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