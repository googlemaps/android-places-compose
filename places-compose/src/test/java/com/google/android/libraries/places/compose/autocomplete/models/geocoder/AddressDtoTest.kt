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
package com.google.android.libraries.places.compose.autocomplete.models.geocoder

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Test

class AddressDtoTest {

    private val gson = Gson()

    @Test
    fun getCountryCode_returnsShortNameWhenCountryPresent() {
        val addressComponentCountry = AddressComponent(
            longName = "India",
            shortName = "IN",
            types = listOf("country", "political")
        )
        val addressComponentRoute = AddressComponent(
            longName = "Main Street",
            shortName = "Main St",
            types = listOf("route")
        )

        val addressDto = AddressDto(
            addressComponents = listOf(addressComponentRoute, addressComponentCountry),
            formattedAddress = "Main St, India",
            geometry = Geometry(
                location = Location(lat = 12.9716, lng = 77.5946),
                locationType = "ROOFTOP",
                viewport = Viewport(
                    northeast = Location(12.98, 77.60),
                    southwest = Location(12.96, 77.58)
                ),
                bounds = Bounds(
                    northeast = Location(12.98, 77.60),
                    southwest = Location(12.96, 77.58)
                )
            ),
            placeId = "ChIJbU60yXAWrjsR4E9-UejD3_g",
            plusCode = PlusCode(
                compoundCode = "XHQQ+34 Bengaluru, Karnataka",
                globalCode = "7J4VXHQQ+34"
            ),
            types = listOf("street_address")
        )

        assertThat(addressDto.getCountryCode()).isEqualTo("IN")
    }

    @Test
    fun getCountryCode_returnsNullWhenCountryAbsent() {
        val addressDto = AddressDto(
            addressComponents = listOf(
                AddressComponent(
                    longName = "Bengaluru",
                    shortName = "BLR",
                    types = listOf("locality")
                )
            ),
            formattedAddress = "Bengaluru",
            geometry = Geometry(
                location = Location(lat = 12.9716, lng = 77.5946),
                locationType = "APPROXIMATE",
                viewport = Viewport(
                    northeast = Location(12.98, 77.60),
                    southwest = Location(12.96, 77.58)
                ),
                bounds = null
            ),
            placeId = "ChIJbU60yXAWrjsR4E9-UejD3_g",
            plusCode = PlusCode(
                compoundCode = "XHQQ+34 Bengaluru",
                globalCode = "7J4VXHQQ+34"
            ),
            types = listOf("locality")
        )

        assertThat(addressDto.getCountryCode()).isNull()
    }

    @Test
    fun reverseGeocodingResponse_defaultValues() {
        val response = ReverseGeocodingResponse(
            status = "ZERO_RESULTS",
            errorMessage = "No results found",
            addressDescriptor = null,
            plusCode = null
        )

        assertThat(response.status).isEqualTo("ZERO_RESULTS")
        assertThat(response.errorMessage).isEqualTo("No results found")
        assertThat(response.addressDescriptor).isNull()
        assertThat(response.plusCode).isNull()
        assertThat(response.addresses).isEmpty()
    }

    @Test
    fun addressDescriptor_modelsCreationAndSerialization() {
        val area = Area(
            containment = "WITHIN",
            displayName = DisplayName(languageCode = "en", text = "Whitefield"),
            placeId = "area_place_1"
        )
        val landmark = Landmark(
            displayName = DisplayName(languageCode = "en", text = "KTPO Convention Centre"),
            placeId = "landmark_place_1",
            spatialRelationship = "NEAR",
            straightLineDistanceMeters = 150.0,
            travelDistanceMeters = 200.0,
            types = listOf("establishment", "point_of_interest")
        )
        val descriptor = AddressDescriptor(
            areas = listOf(area),
            landmarks = listOf(landmark)
        )

        val json = gson.toJson(descriptor)
        val deserialized = gson.fromJson(json, AddressDescriptor::class.java)

        assertThat(deserialized.areas).hasSize(1)
        assertThat(deserialized.areas[0].displayName.text).isEqualTo("Whitefield")
        assertThat(deserialized.areas[0].containment).isEqualTo("WITHIN")
        assertThat(deserialized.landmarks).hasSize(1)
        assertThat(deserialized.landmarks[0].displayName.text).isEqualTo("KTPO Convention Centre")
        assertThat(deserialized.landmarks[0].spatialRelationship).isEqualTo("NEAR")
        assertThat(deserialized.landmarks[0].straightLineDistanceMeters).isEqualTo(150.0)
        assertThat(deserialized.landmarks[0].travelDistanceMeters).isEqualTo(200.0)
        assertThat(deserialized.landmarks[0].types).contains("point_of_interest")
    }

    @Test
    fun reverseGeocodingResponse_fullJsonSerialization() {
        val jsonInput = """
            {
              "status": "OK",
              "results": [
                {
                  "address_components": [
                    {
                      "long_name": "Bengaluru",
                      "short_name": "BLR",
                      "types": ["locality", "political"]
                    },
                    {
                      "long_name": "India",
                      "short_name": "IN",
                      "types": ["country", "political"]
                    }
                  ],
                  "formatted_address": "Bengaluru, Karnataka, India",
                  "geometry": {
                    "location": { "lat": 12.9716, "lng": 77.5946 },
                    "location_type": "APPROXIMATE",
                    "viewport": {
                      "northeast": { "lat": 13.0, "lng": 77.7 },
                      "southwest": { "lat": 12.8, "lng": 77.4 }
                    },
                    "bounds": {
                      "northeast": { "lat": 13.0, "lng": 77.7 },
                      "southwest": { "lat": 12.8, "lng": 77.4 }
                    }
                  },
                  "place_id": "ChIJbU60yXAWrjsR4E9-UejD3_g",
                  "plus_code": {
                    "compound_code": "XHQQ+34 Bengaluru, Karnataka",
                    "global_code": "7J4VXHQQ+34"
                  },
                  "types": ["locality", "political"]
                }
              ],
              "address_descriptor": {
                "areas": [
                  {
                    "containment": "WITHIN",
                    "display_name": { "language_code": "en", "text": "Indiranagar" },
                    "place_id": "ChIJarea123"
                  }
                ],
                "landmarks": [
                  {
                    "display_name": { "language_code": "en", "text": "100 Feet Road" },
                    "place_id": "ChIJlandmark123",
                    "spatial_relationship": "NEAR",
                    "straight_line_distance_meters": 50.5,
                    "travel_distance_meters": 75.2,
                    "types": ["route"]
                  }
                ]
              },
              "plus_code": {
                "compound_code": "XHQQ+34 Bengaluru",
                "global_code": "7J4VXHQQ+34"
              }
            }
        """.trimIndent()

        val response = gson.fromJson(jsonInput, ReverseGeocodingResponse::class.java)

        assertThat(response.status).isEqualTo("OK")
        assertThat(response.errorMessage).isNull()
        assertThat(response.addresses).hasSize(1)
        assertThat(response.addresses[0].getCountryCode()).isEqualTo("IN")
        assertThat(response.addressDescriptor?.areas).hasSize(1)
        assertThat(response.addressDescriptor?.landmarks).hasSize(1)
        assertThat(response.plusCode?.globalCode).isEqualTo("7J4VXHQQ+34")
    }
}
