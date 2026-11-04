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
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.compose.autocomplete.data.meters
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertNull

/**
 * Unit tests verifying conversion from [AutocompletePrediction] to [AutocompletePlace].
 */
class AutocompleteMapperTest {

    @Test
    fun toPlaceDetails_mapsPredictionFieldsCorrectly() {
        val primarySpannable = mockk<SpannableString>()
        val secondarySpannable = mockk<SpannableString>()
        val prediction = mockk<AutocompletePrediction> {
            every { placeId } returns "place_xyz"
            every { getPrimaryText(any()) } returns primarySpannable
            every { getSecondaryText(any()) } returns secondarySpannable
            every { distanceMeters } returns 1200
        }

        val placeDetails = prediction.toPlaceDetails()

        assert(placeDetails.placeId == "place_xyz")
        assert(placeDetails.primaryText === primarySpannable)
        assert(placeDetails.secondaryText === secondarySpannable)
        assert(placeDetails.distance == 1200.meters)
    }

    @Test
    fun toPlaceDetails_withNullDistanceMeters_setsNullDistance() {
        val primarySpannable = mockk<SpannableString>()
        val secondarySpannable = mockk<SpannableString>()
        val prediction = mockk<AutocompletePrediction> {
            every { placeId } returns "place_no_dist"
            every { getPrimaryText(any()) } returns primarySpannable
            every { getSecondaryText(any()) } returns secondarySpannable
            every { distanceMeters } returns null
        }

        val placeDetails = prediction.toPlaceDetails()

        assert(placeDetails.placeId == "place_no_dist")
        assertNull(placeDetails.distance)
    }
}
