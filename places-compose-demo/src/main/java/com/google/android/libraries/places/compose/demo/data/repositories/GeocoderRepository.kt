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

import android.content.Context
import android.net.Uri
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.ReverseGeocodingResponse
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

inline fun buildUrl(baseUrl: String, block: Uri.Builder.() -> Unit): String {
    val builder = Uri.parse(baseUrl).buildUpon()
    block(builder)
    return builder.build().toString()
}

/**
 * A repository class that handles fetching address descriptors.
 *
 * @param context The application context.
 */
class GeocoderRepository(
    private val context: Context,
    private val apiKeyProvider: ApiKeyProvider
) {
    private val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private val _nearbyObjects = MutableStateFlow<List<NearbyObject>>(emptyList())
    val nearbyObjects: StateFlow<List<NearbyObject>> = _nearbyObjects.asStateFlow()

    private fun buildRequestUrl(latLng: LatLng, includeAddressDescriptors: Boolean = true): String {
        return buildUrl(BASE_URL) {
            appendQueryParameter("latlng", "${latLng.latitude},${latLng.longitude}")
            appendQueryParameter("key", apiKeyProvider.mapsApiKey)
            if (includeAddressDescriptors) {
                appendQueryParameter("enable_address_descriptor", "true")
            }
        }
    }

    suspend fun reverseGeocode(
        latLng: LatLng,
        includeAddressDescriptors: Boolean = true
    ): ReverseGeocodingResponse {
        val url = buildRequestUrl(latLng, includeAddressDescriptors)

        return suspendCancellableCoroutine { cont ->
            val queue = Volley.newRequestQueue(context)
            val stringRequest =
                StringRequest(
                    Request.Method.GET,
                    url,
                    { response -> cont.resume(parseFullGeocodeResponse(response)) },
                    { error -> cont.resumeWithException(RuntimeException(error.localizedMessage)) },
                )
            queue.add(stringRequest)
        }
    }

    private fun parseFullGeocodeResponse(response: String): ReverseGeocodingResponse {
        val fullResult = gson.fromJson(response, ReverseGeocodingResponse::class.java)
        // TODO: handle the status -- consider a monad pattern (Result?)

        return fullResult
    }

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json"
    }
}