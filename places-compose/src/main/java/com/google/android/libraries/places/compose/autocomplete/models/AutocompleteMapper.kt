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

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.compose.autocomplete.data.meters

/**
 * Converts an AutocompletePrediction object to a PlaceDetails object.
 *
 * @return A PlaceDetails object containing the extracted information from the
 *   AutocompletePrediction.
 */
fun AutocompletePrediction.toPlaceDetails() =
  AutocompletePlace(
    placeId = placeId,
    primaryText = getPrimaryText(predictionStyleSpan),
    secondaryText = getSecondaryText(predictionStyleSpan),
    distance = distanceMeters?.meters,
  )

/** StyleSpan applied by the to the [AutocompletePrediction]s to highlight the matches. */
private val predictionStyleSpan = android.text.style.StyleSpan(android.graphics.Typeface.BOLD)
