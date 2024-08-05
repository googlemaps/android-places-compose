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

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.Gson
import org.json.JSONObject
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AddressValidationRepository(
    private val apiKeyProvider: ApiKeyProvider,
    private val requestQueue: RequestQueue
) {
    private val gson = Gson()

    suspend fun validateAddress(
        request: AddressValidationRequest
    ): AddressValidationResponse {
        val url = buildString {
            append(BASE_URL)
            append("?key=${apiKeyProvider.mapsApiKey}")
        }

        return suspendCoroutine { continuation ->
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, JSONObject(gson.toJson(request)),
                { response ->
                    val addressValidationResponse = gson.fromJson(response.toString(), AddressValidationResponse::class.java)
                    continuation.resume(addressValidationResponse)
                },
                { error ->
                    val errorMessage = if (error.networkResponse != null) {
                        String(error.networkResponse.data, StandardCharsets.UTF_8)
                    } else {
                        error.message ?: "Unknown error"
                    }
                    continuation.resumeWithException(AddressValidationException(error.networkResponse?.statusCode ?: 0, errorMessage))
                }
            )

            requestQueue.add(jsonObjectRequest)
        }
    }

    companion object {
        const val BASE_URL = "https://addressvalidation.googleapis.com/v1:validateAddress"
    }
}

data class AddressValidationResponse(
    val result: ValidationResult,
    val responseId: String? = null // Optional unique identifier for the request
) {

    data class ValidationResult(
        val verdict: Verdict,
        val address: Address? = null, // Corrected/standardized address
        val validationGranularity: Granularity? = null
    ) {

        enum class Verdict {
            UNSPECIFIED_VERDICT, // Default value, indicates an unknown status
            VALIDATION_SUCCESS,
            PARTIAL_MATCH,
            NO_MATCH, // Address could not be matched
            AMBIGUOUS_VERDICT // Address is too ambiguous to validate
        }

        enum class Granularity {
            SUB_PREMISE, // Most granular level (e.g., apartment number)
            PREMISE,
            BLOCK,
            ROUTE,
            LOCALITY,
            POSTAL_CODE,
            ADMINISTRATIVE_AREA,
            COUNTRY,
            UNKNOWN_GRANULARITY
        }

        data class Address(
            val revisedAddress: String? = null, // Fully revised address (if available)
            val addressComponents: List<AddressComponent> = emptyList()
        ) {

            data class AddressComponent(
                val componentName: String,
                val componentType: String
            )
        }
    }
}

interface AddressValidationRequestInterface {
    val regionCode: String
    val addressLines: List<String>
    val locality: String
}

data class AddressValidationRequest(
    override val regionCode: String,
    override val locality: String,
    override val addressLines: List<String>
): AddressValidationRequestInterface

data class UsAddressValidationRequest(
    override val regionCode: String,
    override val addressLines: List<String>,
    override val locality: String,
    val postalCode: String? = null,
    val enableUspsCass: Boolean = false,
) : AddressValidationRequestInterface

class AddressValidationException(
    val responseCode: Int,
    val errorMessage: String,
    val errorType: ErrorType? = null
) : Exception("Address Validation Error: $responseCode - $errorMessage") {

    enum class ErrorType {
        API_KEY_INVALID,
        REQUEST_INVALID,
        ADDRESS_NOT_FOUND,
        QUOTA_EXCEEDED,
        SERVER_ERROR,
        UNKNOWN_ERROR
    }

    companion object {
        fun fromVolleyError(error: VolleyError): AddressValidationException {
            val responseCode = error.networkResponse?.statusCode ?: 0
            val errorMessage = String(error.networkResponse?.data ?: "Unknown error".toByteArray(), StandardCharsets.UTF_8)

            val errorType = when (responseCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> ErrorType.API_KEY_INVALID
                HttpURLConnection.HTTP_BAD_REQUEST -> ErrorType.REQUEST_INVALID
                HttpURLConnection.HTTP_NOT_FOUND -> ErrorType.ADDRESS_NOT_FOUND
                HttpURLConnection.HTTP_FORBIDDEN -> ErrorType.QUOTA_EXCEEDED
                in 500..599 -> ErrorType.SERVER_ERROR
                else -> ErrorType.UNKNOWN_ERROR
            }

            return AddressValidationException(responseCode, errorMessage, errorType)
        }
    }
}

