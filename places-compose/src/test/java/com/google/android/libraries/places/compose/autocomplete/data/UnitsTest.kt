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
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

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

        assertThat(shortDistance).isLessThan(longDistance)
        assertThat(longDistance).isGreaterThan(shortDistance)
        assertThat(100.meters).isEquivalentAccordingToCompareTo(100.meters)
    }

    @Test
    fun meters_minus_computesDifferenceAccurately() {
        val distanceA = 500.meters
        val distanceB = 150.meters

        val difference = distanceA - distanceB

        assertThat(difference.value).isEqualTo(350.0)
    }

    // ----------------------------------------------------------------------------------
    // Number Extensions: Inlined Unit Constructors
    // ----------------------------------------------------------------------------------

    @Test
    fun numberExtensions_constructMetersEquivalently() {
        assertThat(50.meters.value).isEqualTo(50.0)
        assertThat(50.m.value).isEqualTo(50.0)

        // 1 km = 1,000 meters
        assertThat(2.km.value).isEqualTo(2000.0)

        // Feet to meters
        val feetMeters = 328.084.feet
        assertThat(feetMeters.toFeet).isWithin(0.1).of(328.084)

        // Miles to meters
        val oneMile = 1.0.miles
        assertThat(oneMile.toMiles).isWithin(0.001).of(1.0)
    }

    @Test
    fun metersExtensions_convertToScalarsCorrectly() {
        val distance = 1500.meters

        assertThat(distance.toMeters).isEqualTo(1500.0)
        assertThat(distance.toKilometers).isEqualTo(1.5)
        assertThat(distance.toFeet).isWithin(0.01).of(1500.0 * METERS_PER_FOOT)
        assertThat(distance.toMiles).isWithin(0.001).of(1500.0 * MILES_PER_METER)
    }

    // ----------------------------------------------------------------------------------
    // Units Converter Strategy: Country-Based Factory
    // ----------------------------------------------------------------------------------

    @Test
    fun getUnitsConverter_returnsImperialForUnitedStates() {
        val converter = getUnitsConverter("US")
        assertThat(converter).isSameInstanceAs(ImperialUnitsConverter)
    }

    @Test
    fun getUnitsConverter_returnsMetricForNonUsAndNull() {
        assertThat(getUnitsConverter("IN")).isSameInstanceAs(MetricUnitsConverter)
        assertThat(getUnitsConverter("GB")).isSameInstanceAs(MetricUnitsConverter)
        assertThat(getUnitsConverter(null)).isSameInstanceAs(MetricUnitsConverter)
    }

    // ----------------------------------------------------------------------------------
    // Imperial Units Converter Thresholds (< 0.25 miles vs >= 0.25 miles)
    // ----------------------------------------------------------------------------------

    @Test
    fun imperialUnitsConverter_belowQuarterMile_usesFeetTemplate() {
        // 0.1 miles is less than 0.25 miles threshold
        val distance = 0.1.miles
        val result = ImperialUnitsConverter.toDistanceUnits(distance)

        assertThat(result.unitsTemplate).isEqualTo(R.string.in_feet)
        assertThat(result.value).isWithin(0.1).of(distance.toFeet)
    }

    @Test
    fun imperialUnitsConverter_atOrAboveQuarterMile_usesMilesTemplate() {
        val distance = 2.5.miles
        val result = ImperialUnitsConverter.toDistanceUnits(distance)

        assertThat(result.unitsTemplate).isEqualTo(R.string.in_miles)
        assertThat(result.value).isEqualTo(2.5)
    }

    // ----------------------------------------------------------------------------------
    // Metric Units Converter Thresholds (< 1000m vs >= 1000m)
    // ----------------------------------------------------------------------------------

    @Test
    fun metricUnitsConverter_belowKilometer_usesMetersTemplate() {
        val distance = 450.meters
        val result = MetricUnitsConverter.toDistanceUnits(distance)

        assertThat(result.unitsTemplate).isEqualTo(R.string.in_meters)
        assertThat(result.value).isEqualTo(450.0)
    }

    @Test
    fun metricUnitsConverter_atOrAboveKilometer_usesKilometersTemplate() {
        val distance = 2500.meters
        val result = MetricUnitsConverter.toDistanceUnits(distance)

        assertThat(result.unitsTemplate).isEqualTo(R.string.in_kilometers)
        assertThat(result.value).isEqualTo(2.5)
    }

    @Test
    fun toDistanceString_withMockedResources_formatsCorrectly() {
        val mockResources = mockk<Resources>()
        every { mockResources.getString(R.string.in_feet, *anyVararg()) } returns "528 ft"
        every { mockResources.getString(R.string.in_kilometers, *anyVararg()) } returns "2.5 km"

        val imperialString = ImperialUnitsConverter.toDistanceString(mockResources, 0.1.miles)
        assertThat(imperialString).isEqualTo("528 ft")

        val metricString = MetricUnitsConverter.toDistanceString(mockResources, 2500.meters)
        assertThat(metricString).isEqualTo("2.5 km")
    }
}
