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

import androidx.compose.runtime.mutableStateMapOf
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing places data.
 *
 * @param placesClient The client for interacting with the Places API.
 */
class PlaceRepository(
    private val placesClient: PlacesClient,
) {
    private val placesIdsWithLocations = mutableStateMapOf<String, Place>()

    suspend fun getPlaceLatLng(placeId: String): Pair<String, Place> {
        return withContext(Dispatchers.Main) {
            placeId to placesIdsWithLocations.getOrElse(placeId) {
                withContext(Dispatchers.IO) {
                    placesClient.awaitFetchPlace(
                        placeId = placeId,
                        placeFields = listOf(Place.Field.LOCATION)
                    ).place.also { place ->
                        withContext(Dispatchers.Main) {
                            placesIdsWithLocations[placeId] = place
                        }
                    }
                }
            }
        }
    }

    private val placesIdsWithAddresses = mutableStateMapOf<String, Place>()

    suspend fun getPlaceAddress(placeId: String): Place {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION,
        )

        return withContext(Dispatchers.Main) {
            placesIdsWithAddresses.getOrElse(placeId) {
                withContext(Dispatchers.IO) {
                    placesClient.awaitFetchPlace(
                        placeId = placeId,
                        placeFields = placeFields
                    ).place.also { place ->
                        withContext(Dispatchers.Main) {
                            placesIdsWithAddresses[placeId] = place
                        }
                    }
                }
            }
        }
    }
}
