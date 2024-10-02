package com.google.android.libraries.places.compose.demo.data.repositories

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge

data class LabeledLocation(
  val latLng: LatLng,
  val label: String,
)

class MockLocationRepository {
  private val _mockLocationNumber = MutableStateFlow(0)
  private val mockLocationNumber: StateFlow<Int> = _mockLocationNumber.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  private val mockLocation = mockLocationNumber.mapLatest {
    LabeledLocation(
      latLng = mockLocations[it].second,
      label = mockLocations[it].first,
    )
  }

  private val userClickedLocation = MutableStateFlow<LabeledLocation?>(null)

  val location = merge(mockLocation, userClickedLocation).mapNotNull { location ->
    location
  }

  fun setMockLocation(location: LatLng) {
    userClickedLocation.value = LabeledLocation(location, "User selected")
  }

  fun nextMockLocation(): Pair<String, LatLng> {
    _mockLocationNumber.value = (_mockLocationNumber.value + 1) % locations.size
    return locations[_mockLocationNumber.value]
  }

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
      "Nerul Maharashtra" to LatLng(19.031959, 73.021357),
      "Meera Road Maharashtra" to LatLng(19.280187, 72.8758939),
      "Pune Maharashtra" to LatLng(18.5771235, 73.8889267),
      "Hyderabad Andhra Pradesh" to LatLng(17.4984783, 78.3189972),
      "Jubilee Hills Andhra Pradesh" to LatLng(17.424182, 78.4266691),
      "Miyanpur Telengana" to LatLng(17.4995576, 78.347341),
      "Old Palasia Madhya Pradesh" to LatLng(22.7246176, 75.8903836),
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
      "Koramangala Bangalore" to LatLng(12.9338819, 77.6284051),
      "Google Bangalore" to LatLng(12.993964826380658, 77.66062458649924),
      "RR Nagar Bangalore" to LatLng(12.9312721, 77.5218683),
    )

    internal val locations = mockLocations
  }
}