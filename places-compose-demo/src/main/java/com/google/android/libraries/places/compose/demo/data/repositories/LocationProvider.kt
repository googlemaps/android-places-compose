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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


/**
 * A client for accessing location updates.
 *
 * This interface provides a method to obtain a flow of [Location] objects, which represent the
 * current location of the device.
 *
 * **Note:** This interface is experimental and may be subject to change in future versions of the
 * library.
 */
interface LocationRepository {
  @ExperimentalCoroutinesApi
  @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
  fun locationFlow(locationRequest: LocationRequest? = null): StateFlow<Location>

  // TODO: change to location
  suspend fun getLastLocation(): LatLng
}

class MockLocationRepository(private val scope: CoroutineScope) : LocationRepository {
  private val _mockLocationNumber = MutableStateFlow<Int>(0)
  private val mockLocationNumber: StateFlow<Int> = _mockLocationNumber.asStateFlow()

  private val mockLocationFlow = MutableStateFlow("" to LatLng(0.0, 0.0))

  @ExperimentalCoroutinesApi
  override fun locationFlow(locationRequest: LocationRequest?): StateFlow<Location> {
    return selectedMockLocation.transform { location ->
      emit(Location("MockLocation").apply {
        latitude = location.latitude
        longitude = location.longitude
      })
    }.stateIn(
      scope = scope,
      started = SharingStarted.Eagerly,
      initialValue = Location("MockLocation")
    )
  }

  override suspend fun getLastLocation(): LatLng {
    return selectedMockLocation.value
  }

  fun setMockLocation(location: LatLng) {
    mockLocationFlow.value = "User clicked" to location
  }

  fun nextMockLocation(): Pair<String, LatLng> {
    _mockLocationNumber.value = (_mockLocationNumber.value + 1) % locations.size
    return locations[_mockLocationNumber.value]
  }

  val labeledLocation: StateFlow<Pair<String, LatLng>> = mockLocationNumber.map { index ->
    locations[index]
  }.stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = locations[0]
  )

  @OptIn(ExperimentalCoroutinesApi::class)
  val selectedMockLocation = labeledLocation.mapLatest { location ->
    location.second
  }.stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = locations[0].second
  )

  companion object {
    val mockLocations = listOf(
      "Boulder, CO" to LatLng(40.01924246438453, -105.259858527573),
      "San Francisco" to LatLng(37.79394100222431, -122.39235812762442),
      "Karnataka Trade Promotion Organisation" to LatLng(12.9794404, 77.7179181),
      "Gurugram India" to LatLng(28.42659051528613, 77.04881164397028),
      "Akshar Dham Temple" to LatLng(21.233980841304085, 72.9069776200368),
      "Google India" to LatLng(28.461689066807388, 77.0484750110405),
    )

    internal val extraLocations = listOf(
      "Karnataka Trade Promotion Organisation" to LatLng(12.9794404, 77.7179181),
      "Gurugram India" to LatLng(28.42659051528613, 77.04881164397028),
      "Nerul Maharashtra" to LatLng(19.031959,73.021357),
      "Meera Road Maharashtra" to LatLng(19.280187,72.8758939),
      "Pune Maharashtra" to LatLng(18.5771235, 73.8889267),
      "Hyderabad Andhra Pradesh" to LatLng(17.4984783,78.3189972),
      "Jubilee Hills Andhra Pradesh" to LatLng(17.424182,78.4266691),
      "Miyanpur Telengana" to LatLng(17.4995576,78.347341),
      "Old Palasia Madhya Pradesh" to LatLng(22.7246176,75.8903836),
      "Shivaji Nagar Madhya Pradesh" to LatLng(23.2263448, 77.4357658),
      "LDA Lucknow" to LatLng(26.788363560112813, 80.90508838223072),
      "SingarNagar Lucknow" to LatLng(26.807075978495284, 80.89320513941539),
      "Gomti Nagar" to LatLng(26.8532357057197, 81.00580530397885),
      "AishBagh Lucknow" to LatLng(26.84309109881356, 80.8995092862628),
      "Old Lucknow" to LatLng(26.87558681981076, 80.9034278315022),
      "Sec 2 GGN" to LatLng(28.50691074489905, 77.03628338041477),
      "Malibu Home" to LatLng(28.42659051528613, 77.04881164397028),
      "GGN Shopping Mall" to LatLng(28.421701843523877, 77.05311158884382),
      "GGN Apartment" to LatLng(28.422313432453112, 77.05246950435951),
      "ITC Maurya Delhi" to LatLng(28.597462919464256, 77.17342515946939),
      "Google Gurgaon" to LatLng(28.461661879500202, 77.0487079323859),
      "Hauz Khas Delhi" to LatLng(28.543058806781815, 77.20461070103957),
      "Rohini Delhi" to LatLng(28.698966896035877, 77.1083132365776),
      "Adarsh Nagar Jaipur" to LatLng(26.89847314718748, 75.82830154255896),
      "Akshar Dham Temple" to LatLng(21.233980841304085, 72.9069776200368),
      "Koramangala Bangalore" to LatLng(12.9338819,77.6284051),
      "Google Bangalore" to LatLng(12.993964826380658, 77.66062458649924),
      "RR Nagar Bangalore" to LatLng(12.9312721,77.5218683),
    )

    internal val locations = mockLocations
  }
}

/**
 * Implementation of the [LocationRepository] interface that provides location updates using the Fused Location Provider API.
 *
 * TODO: consider passing in the location provider?
 * What about the location request parameters?
 *
 * @param context The application context.
 * @param scope The coroutine scope in which the location updates will be emitted.
 */
class LocationRepositoryImpl(context: Context, private val scope: CoroutineScope) : LocationRepository {
  private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
  private var locationRequest = with(LocationRequest.Builder(/* intervalMillis = */ 10.seconds.inWholeMilliseconds)) {
    setPriority(Priority.PRIORITY_HIGH_ACCURACY)
    setWaitForAccurateLocation(true)
    build()
  }

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
  override fun locationFlow(locationRequest: LocationRequest?): StateFlow<Location> {
    if (locationRequest != null) {
      this.locationRequest = locationRequest
    }

    return _locationUpdates.stateIn(
      scope = scope,
      SharingStarted.WhileSubscribed(5000),
      initialValue = Location("MockLocation")
    )
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
  override suspend fun getLastLocation(): LatLng {
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
