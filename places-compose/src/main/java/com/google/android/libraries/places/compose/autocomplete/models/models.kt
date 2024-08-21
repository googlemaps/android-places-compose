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

import android.text.Spannable
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.R
import com.google.android.libraries.places.compose.autocomplete.data.Meters
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.autocomplete.data.toDistanceString
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Area
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Landmark

data class AutocompletePlace(
  val placeId: String,
  val primaryText: Spannable,
  val secondaryText: Spannable,
  val distance: Meters? = null,
  val latLng: LatLng? = null
)

sealed class NearbyObject {
  abstract val placeId: String
  abstract val spatialRelationshipStringRes: Int
  abstract val name: String

  @Composable
  abstract fun getDistanceString(): String

  @Composable
  fun spatialRelationship(): String {
    return buildString {
      append(stringResource(spatialRelationshipStringRes, name))
    }
  }

  data class NearbyLandmark(val landmark: Landmark) : NearbyObject() {
    override val name: String
      get() = landmark.displayName.text

    override val spatialRelationshipStringRes: Int
      @StringRes
      get() = landmark.getSpatialRelationshipStringRes()

    override val placeId: String
      get() = landmark.placeId

    @Composable
    override fun getDistanceString(): String {
      return landmark.distanceMeters().toDistanceString()
    }
  }

  data class NearbyArea(val area: Area) : NearbyObject() {
    override val name: String
      get() = area.displayName.text

    override val spatialRelationshipStringRes: Int
      @StringRes
      get() = area.getSpatialRelationshipStringRes()

    override val placeId: String
      get() = area.placeId

    @Composable
    override fun getDistanceString(): String {
      return ""
    }
  }
}

@StringRes
fun Area.getSpatialRelationshipStringRes(): Int {
  return when (containment) {
    "OUTSKIRTS" -> R.string.spatial_relationship_outskirts_of
    "WITHIN" -> R.string.spatial_relationship_within
    else -> R.string.spatial_relationship_near
  }
}

@StringRes
fun Landmark.getSpatialRelationshipStringRes(): Int {
  return when (spatialRelationship) {
    "WITHIN" -> R.string.spatial_relationship_within
    "BESIDE" -> R.string.spatial_relationship_beside
    "ACROSS_THE_ROAD" -> R.string.spatial_relationship_across_the_road
    "DOWN_THE_ROAD" -> R.string.spatial_relationship_down_the_road
    "AROUND_THE_CORNER" -> R.string.spatial_relationship_around_the_corner
    "BEHIND" -> R.string.spatial_relationship_behind
    else -> R.string.spatial_relationship_near
  }
}

fun Landmark.distanceMeters(): Meters =
  (if (travelDistanceMeters < 0) travelDistanceMeters else straightLineDistanceMeters).meters
