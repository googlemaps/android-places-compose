// Copyright 2026 Google LLC
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

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.data.Meters
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.maps.android.SphericalUtil
import kotlin.test.Test

/**
 * Checks whether this [LatLng] is within [tolerance] of [other] using spherical geometry.
 *
 * When evaluated inside a Kotlin Power-Assert `assert(...)` block, Power-Assert automatically
 * captures and displays both [LatLng] coordinates, the [tolerance], and the boolean outcome.
 */
internal fun LatLng.isWithin(other: LatLng, tolerance: Meters): Boolean =
    SphericalUtil.computeDistanceBetween(this, other).meters <= tolerance

/**
 * Unit tests verifying [MockLocationRepository] cycling, custom user locations, and spherical
 * proximity assertions under Kotlin Power-Assert.
 */
internal class MockLocationRepositoryTest {

    @Test
    fun getCurrentMockLocation_startsAtFirstPresetLocation() {
        val repository = MockLocationRepository()

        val (label, coordinates) = repository.getCurrentMockLocation()

        assert(label == "Boulder, CO")
        assert(coordinates == MockLocationRepository.mockLocations.first().second)
    }

    @Test
    fun nextMockLocation_cyclesThroughAllPresetsAndWrapsAround() {
        val repository = MockLocationRepository()
        val totalPresets = MockLocationRepository.mockLocations.size

        for (index in 1 until totalPresets) {
            val current = repository.nextMockLocation()
            assert(current == MockLocationRepository.mockLocations[index])
        }

        // Advancing one more step wraps back to index 0
        val wrapped = repository.nextMockLocation()
        assert(wrapped == MockLocationRepository.mockLocations.first())
    }

    @Test
    fun isWithin_verifiesLocationProximityWithinTolerance() {
        val repository = MockLocationRepository()
        val (_, boulderOrigin) = repository.getCurrentMockLocation()

        // ~5 meters north of the Boulder preset (~0.000045 degrees latitude)
        val nearbyPoint = LatLng(boulderOrigin.latitude + 0.000045, boulderOrigin.longitude)
        // ~100 meters north of the Boulder preset (~0.0009 degrees latitude)
        val distantPoint = LatLng(boulderOrigin.latitude + 0.0009, boulderOrigin.longitude)

        assert(nearbyPoint.isWithin(boulderOrigin, 10.meters))
        assert(!distantPoint.isWithin(boulderOrigin, 10.meters))
    }
}
