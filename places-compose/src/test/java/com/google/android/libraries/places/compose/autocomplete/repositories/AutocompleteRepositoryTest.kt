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
package com.google.android.libraries.places.compose.autocomplete.repositories

import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests verifying [AutocompleteRepository] delegation to [PlacesClient].
 */
class AutocompleteRepositoryTest {

    @Test
    fun getAutocompletePlaces_delegatesToPlacesClient() = runTest {
        val placesClient = mockk<PlacesClient>()
        val request = mockk<FindAutocompletePredictionsRequest>()
        val prediction1 = mockk<AutocompletePrediction>()
        val prediction2 = mockk<AutocompletePrediction>()
        val response = mockk<FindAutocompletePredictionsResponse> {
            every { autocompletePredictions } returns listOf(prediction1, prediction2)
        }

        every { placesClient.findAutocompletePredictions(request) } returns Tasks.forResult(response)

        val repository = AutocompleteRepository(placesClient)
        val result = repository.getAutocompletePlaces(request)

        assertThat(result).hasSize(2)
        assertThat(result).containsExactly(prediction1, prediction2).inOrder()
    }
}
