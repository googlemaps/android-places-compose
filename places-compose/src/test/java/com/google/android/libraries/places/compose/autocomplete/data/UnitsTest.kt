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
package com.google.android.libraries.places.compose.autocomplete.data

import android.content.res.Resources
import com.google.android.libraries.places.compose.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests verifying distance measurement units, unit conversions, and localized formatting.
 *
 * This test suite covers:
 * 1. [Meters] value class arithmetic and comparisons.
 * 2. Extension operators converting numbers to [Meters] and [Meters] to scalar units.
 * 3. Country-specific [UnitsConverter] selection (Imperial for "US", Metric otherwise).
 * 4. Distance unit templates and formatted string generation using Android resources.
 */
class UnitsTest {

    // ----------------------------------------------------------------------------------
    // Meters Value Class: Basic Arithmetic & Comparisons
    // ----------------------------------------------------------------------------------

    @Test
    fun meters_compareTo_ordersCorrectly() {
        val shortDistance = 100.meters
        val longDistance = 500.meters

        assert(shortDistance < longDistance)
        assert(longDistance > shortDistance)
        assert(100.meters.compareTo(100.meters) == 0)
    }

    @Test
    fun meters_minus_computesDifferenceAccurately() {
        val distanceA = 500.meters
        val distanceB = 150.meters

        val difference = distanceA - distanceB

        assert(difference.value == 350.0)
    }

    // ----------------------------------------------------------------------------------
    // Number Extensions: Inlined Unit Constructors
    // ----------------------------------------------------------------------------------

    @Test
    fun numberExtensions_constructMetersEquivalently() {
        assert(50.meters.value == 50.0)
        assert(50.m.value == 50.0)

        // 1 km = 1,000 meters
        assert(2.km.value == 2000.0)

        // Feet to meters
        val feetMeters = 328.084.feet
        assert(abs(feetMeters.toFeet - 328.084) <= 0.1)

        // Miles to meters
        val oneMile = 1.0.miles
        assert(abs(oneMile.toMiles - 1.0) <= 0.001)
    }

    @Test
    fun metersExtensions_convertToScalarsCorrectly() {
        val distance = 1500.meters

        assert(distance.toMeters == 1500.0)
        assert(distance.toKilometers == 1.5)
        assert(abs(distance.toFeet - 1500.0 * METERS_PER_FOOT) <= 0.01)
        assert(abs(distance.toMiles - 1500.0 * MILES_PER_METER) <= 0.001)
    }

    // ----------------------------------------------------------------------------------
    // Units Converter Strategy: Country-Based Factory
    // ----------------------------------------------------------------------------------

    @Test
    fun getUnitsConverter_returnsImperialForUnitedStates() {
        val converter = getUnitsConverter("US")
        assert(converter === ImperialUnitsConverter)
    }

    @Test
    fun getUnitsConverter_returnsMetricForNonUsAndNull() {
        assert(getUnitsConverter("IN") === MetricUnitsConverter)
        assert(getUnitsConverter("GB") === MetricUnitsConverter)
        assert(getUnitsConverter(null) === MetricUnitsConverter)
    }

    // ----------------------------------------------------------------------------------
    // Imperial Units Converter Thresholds (< 0.25 miles vs >= 0.25 miles)
    // ----------------------------------------------------------------------------------

    @Test
    fun imperialUnitsConverter_belowQuarterMile_usesFeetTemplate() {
        // 0.1 miles is less than 0.25 miles threshold
        val distance = 0.1.miles
        val result = ImperialUnitsConverter.toDistanceUnits(distance)

        assert(result.unitsTemplate == R.string.in_feet)
        assert(abs(result.value - distance.toFeet) <= 0.1)
    }

    @Test
    fun imperialUnitsConverter_atOrAboveQuarterMile_usesMilesTemplate() {
        val distance = 2.5.miles
        val result = ImperialUnitsConverter.toDistanceUnits(distance)

        assert(result.unitsTemplate == R.string.in_miles)
        assert(result.value == 2.5)
    }

    // ----------------------------------------------------------------------------------
    // Metric Units Converter Thresholds (< 1000m vs >= 1000m)
    // ----------------------------------------------------------------------------------

    @Test
    fun metricUnitsConverter_belowKilometer_usesMetersTemplate() {
        val distance = 450.meters
        val result = MetricUnitsConverter.toDistanceUnits(distance)

        assert(result.unitsTemplate == R.string.in_meters)
        assert(result.value == 450.0)
    }

    @Test
    fun metricUnitsConverter_atOrAboveKilometer_usesKilometersTemplate() {
        val distance = 2500.meters
        val result = MetricUnitsConverter.toDistanceUnits(distance)

        assert(result.unitsTemplate == R.string.in_kilometers)
        assert(result.value == 2.5)
    }

    @Test
    fun toDistanceString_withMockedResources_formatsCorrectly() {
        val mockResources = mockk<Resources>()
        every { mockResources.getString(R.string.in_feet, *anyVararg()) } returns "528 ft"
        every { mockResources.getString(R.string.in_kilometers, *anyVararg()) } returns "2.5 km"

        val imperialString = ImperialUnitsConverter.toDistanceString(mockResources, 0.1.miles)
        assert(imperialString == "528 ft")

        val metricString = MetricUnitsConverter.toDistanceString(mockResources, 2500.meters)
        assert(metricString == "2.5 km")
    }
}
