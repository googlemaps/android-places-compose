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
package com.google.android.libraries.places.compose.demo.data.repositories

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity.GRANULARITY_FINE
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of the [LocationRepository] interface that provides location updates using the Fused Location Provider API.
 *
 * TODO: consider passing in the location provider?
 * What about the location request parameters?
 *
 * @param context The application context.
 * @param scope The coroutine scope in which the location updates will be emitted.
 */
class LocationRepository(context: Context, private val scope: CoroutineScope) {
  private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
  private var locationRequest = with(LocationRequest.Builder(/* intervalMillis = */ 10.seconds.inWholeMilliseconds)) {
    setPriority(Priority.PRIORITY_HIGH_ACCURACY)
    setWaitForAccurateLocation(true)
    build()
  }

  private val _latestLocation = MutableStateFlow<LatLng?>(null)
  val latestLocation: StateFlow<LatLng?> = _latestLocation

  @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
  @SuppressLint("MissingPermission")
  private val _locationUpdates = callbackFlow {
    val callback = object : LocationCallback() {
      override
      fun onLocationResult(result: LocationResult) {
        result.lastLocation?.let { location ->
          trySend(location)
        }
      }
    }

    fusedLocationClient.requestLocationUpdates(
      locationRequest,
      callback,
      Looper.getMainLooper()
    ).addOnFailureListener {
      close(it)
    }

    awaitClose {
      fusedLocationClient.removeLocationUpdates(callback)
    }
  }.shareIn(
    scope,
    replay = 0,
    started = SharingStarted.WhileSubscribed()
  )

  /**
   * Provides a [Flow] of [Location] updates.
   *
   * This function requires the following permissions:
   * - `ACCESS_COARSE_LOCATION`
   * - `ACCESS_FINE_LOCATION`
   *
   * @param locationRequest The [LocationRequest] object specifying the desired location updates.
   *  If null, the default location request will be used.
   *
   * @return A [Flow] that emits [Location] updates.
   */
  @ExperimentalCoroutinesApi
  @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
  fun continuousLocation(locationRequest: LocationRequest?): StateFlow<Location> {
    if (locationRequest != null) {
      this.locationRequest = locationRequest
    }

    return _locationUpdates.stateIn(
      scope = scope,
      SharingStarted.WhileSubscribed(5000),
      initialValue = Location("MockLocation")
    )
  }

  @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
  fun updateLocation() {
    scope.launch {
      _latestLocation.value = getLastLocation()
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
  suspend fun getLastLocation(): LatLng {
    val cancellationTokenSource = CancellationTokenSource()

    val result = fusedLocationClient.getCurrentLocation(
      CurrentLocationRequest.Builder()
        .setPriority(PRIORITY_BALANCED_POWER_ACCURACY)
        .setDurationMillis(5.seconds.inWholeMilliseconds)
        .setMaxUpdateAgeMillis(1.minutes.inWholeMilliseconds)
        .setGranularity(GRANULARITY_FINE)
        .build(),
      cancellationTokenSource.token,
    ).await(cancellationTokenSource)

    return result.let { LatLng(it.latitude, it.longitude) }
  }
}
