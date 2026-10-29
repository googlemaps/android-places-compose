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
package com.google.android.libraries.places.compose.autocomplete.models

import android.text.SpannableString
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.R
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Area
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.DisplayName
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Landmark
import org.junit.Test

/**
 * Unit tests verifying models and spatial relationship resolution for Address Descriptors.
 *
 * This test suite covers:
 * 1. [AutocompletePlace] construction and attributes.
 * 2. [Area.getSpatialRelationshipStringRes] mapping across all supported containment values.
 * 3. [Landmark.getSpatialRelationshipStringRes] mapping across all spatial relations.
 * 4. [Landmark.distanceMeters] selection logic between travel and straight-line distances.
 * 5. Polymorphic [NearbyObject] properties for landmarks and areas.
 */
class ModelsTest {

    // ----------------------------------------------------------------------------------
    // AutocompletePlace Construction
    // ----------------------------------------------------------------------------------

    @Test
    fun autocompletePlace_properties_matchInputs() {
        val primary = SpannableString("Googleplex")
        val secondary = SpannableString("Mountain View, CA")
        val latLng = LatLng(37.422, -122.084)
        val distance = 250.meters

        val place = AutocompletePlace(
            placeId = "place_123",
            primaryText = primary,
            secondaryText = secondary,
            distance = distance,
            latLng = latLng,
        )

        assert(place.placeId == "place_123")
        assert(place.primaryText === primary)
        assert(place.secondaryText === secondary)
        assert(place.distance == distance)
        assert(place.latLng == latLng)
    }

    // ----------------------------------------------------------------------------------
    // Area Spatial Relationships
    // ----------------------------------------------------------------------------------

    @Test
    fun area_spatialRelationship_resolvesExpectedResource() {
        val outskirtsArea = createArea(containment = "OUTSKIRTS", name = "City Limits")
        assert(outskirtsArea.getSpatialRelationshipStringRes() == R.string.spatial_relationship_outskirts_of)

        val withinArea = createArea(containment = "WITHIN", name = "Indiranagar")
        assert(withinArea.getSpatialRelationshipStringRes() == R.string.spatial_relationship_within)

        val defaultArea = createArea(containment = "NEAR", name = "Downtown")
        assert(defaultArea.getSpatialRelationshipStringRes() == R.string.spatial_relationship_near)

        val nearbyArea = NearbyObject.NearbyArea(withinArea)
        assert(nearbyArea.name == "Indiranagar")
        assert(nearbyArea.placeId == "area_id")
        assert(nearbyArea.spatialRelationshipStringRes == R.string.spatial_relationship_within)
    }

    // ----------------------------------------------------------------------------------
    // Landmark Spatial Relationships
    // ----------------------------------------------------------------------------------

    @Test
    fun landmark_spatialRelationship_resolvesAllCases() {
        val expectedMappings = mapOf(
            "WITHIN" to R.string.spatial_relationship_within,
            "BESIDE" to R.string.spatial_relationship_beside,
            "ACROSS_THE_ROAD" to R.string.spatial_relationship_across_the_road,
            "DOWN_THE_ROAD" to R.string.spatial_relationship_down_the_road,
            "AROUND_THE_CORNER" to R.string.spatial_relationship_around_the_corner,
            "BEHIND" to R.string.spatial_relationship_behind,
            "UNKNOWN" to R.string.spatial_relationship_near,
        )

        for ((relation, expectedResId) in expectedMappings) {
            val landmark = createLandmark(spatialRelationship = relation, name = "Target Landmark")
            assert(landmark.getSpatialRelationshipStringRes() == expectedResId)
        }
    }

    // ----------------------------------------------------------------------------------
    // Landmark Distance Computation
    // ----------------------------------------------------------------------------------

    @Test
    fun landmark_distanceMeters_selectsCorrectDistance() {
        val landmarkPositiveTravel = createLandmark(
            spatialRelationship = "NEAR",
            name = "Park",
            straightLine = 120.0,
            travel = 150.0,
        )
        // When travelDistanceMeters >= 0, returns travelDistanceMeters
        assert(landmarkPositiveTravel.distanceMeters() == 150.meters)

        val landmarkNegativeTravel = createLandmark(
            spatialRelationship = "NEAR",
            name = "Tower",
            straightLine = 200.0,
            travel = -1.0,
        )
        // When travelDistanceMeters < 0, falls back to straightLineDistanceMeters
        assert(landmarkNegativeTravel.distanceMeters() == 200.meters)

        val nearbyLandmark = NearbyObject.NearbyLandmark(landmarkPositiveTravel)
        assert(nearbyLandmark.name == "Park")
        assert(nearbyLandmark.placeId == "landmark_id")
        assert(nearbyLandmark.spatialRelationshipStringRes == R.string.spatial_relationship_near)
    }

    // ----------------------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------------------

    private fun createArea(containment: String, name: String) = Area(
        containment = containment,
        displayName = DisplayName(languageCode = "en", text = name),
        placeId = "area_id",
    )

    private fun createLandmark(
        spatialRelationship: String,
        name: String,
        straightLine: Double = 100.0,
        travel: Double = 150.0,
    ) = Landmark(
        displayName = DisplayName(languageCode = "en", text = name),
        placeId = "landmark_id",
        spatialRelationship = spatialRelationship,
        straightLineDistanceMeters = straightLine,
        travelDistanceMeters = travel,
        types = listOf("point_of_interest"),
    )
}
