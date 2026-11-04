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
package com.google.android.libraries.places.compose.demo.mappers

import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.AddressDescriptor
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Area
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.DisplayName
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Landmark
import kotlin.test.Test

/**
 * Unit tests verifying conversion of [AddressDescriptor] into polymorphic [NearbyObject] lists.
 */
internal class NearbyObjectsMapperTest {

    @Test
    fun toNearbyObjects_mapsLandmarksThenAreasInOrder() {
        val landmark = Landmark(
            displayName = DisplayName(languageCode = "en", text = "KTPO Convention Centre"),
            placeId = "landmark_1",
            spatialRelationship = "NEAR",
            straightLineDistanceMeters = 120.0,
            travelDistanceMeters = 180.0,
            types = listOf("point_of_interest"),
        )
        val area = Area(
            containment = "WITHIN",
            displayName = DisplayName(languageCode = "en", text = "Whitefield"),
            placeId = "area_1",
        )
        val descriptor = AddressDescriptor(
            areas = listOf(area),
            landmarks = listOf(landmark),
        )

        val nearbyObjects = descriptor.toNearbyObjects()

        assert(nearbyObjects == listOf(NearbyObject.NearbyLandmark(landmark), NearbyObject.NearbyArea(area)))
        assert(nearbyObjects[0].name == "KTPO Convention Centre")
        assert(nearbyObjects[1].name == "Whitefield")
    }

    @Test
    fun toNearbyObjects_emptyDescriptor_returnsEmptyList() {
        val descriptor = AddressDescriptor(areas = emptyList(), landmarks = emptyList())

        assert(descriptor.toNearbyObjects().isEmpty())
    }
}
