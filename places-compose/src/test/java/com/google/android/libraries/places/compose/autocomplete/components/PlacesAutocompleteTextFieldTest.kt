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
package com.google.android.libraries.places.compose.autocomplete.components

import android.text.SpannableString
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.compose.autocomplete.data.LocalUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.getUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Area
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.DisplayName
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.Landmark
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull

/**
 * UI unit tests verifying the [PlacesAutocompleteTextField] and [AutocompletePlaceRow] composables.
 *
 * This test suite covers:
 * 1. Search text field rendering with query text and placeholder.
 * 2. Entering text and triggering [onQueryChanged].
 * 3. Rendering place predictions with primary and secondary texts.
 * 4. Selecting a place prediction item and triggering [onSelected].
 * 5. Clearing search text via the clear trailing icon button.
 * 6. Clicking back navigation button when [onBackClicked] is provided.
 */
@RunWith(RobolectricTestRunner::class)
class PlacesAutocompleteTextFieldTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val samplePlaces = listOf(
        AutocompletePlace(
            placeId = "place_1",
            primaryText = SpannableString("Googleplex"),
            secondaryText = SpannableString("1600 Amphitheatre Pkwy, Mountain View, CA"),
            distance = 350.meters,
        ),
        AutocompletePlace(
            placeId = "place_2",
            primaryText = SpannableString("Golden Gate Bridge"),
            secondaryText = SpannableString("San Francisco, CA"),
            distance = 15000.meters,
        ),
    )

    @Test
    fun placesAutocompleteTextField_displaysPlaceholderAndInitialPredictions() {
        composeTestRule.setContent {
            MaterialTheme {
                PlacesAutocompleteTextField(
                    searchText = "",
                    predictions = samplePlaces,
                    onQueryChanged = {},
                    placeHolderText = "Search Google Maps",
                )
            }
        }

        // Verify search field and placeholder
        composeTestRule.onNodeWithTag("placesAutocompleteSearchField").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search Google Maps").assertIsDisplayed()

        // Verify prediction rows
        composeTestRule.onNodeWithText("Googleplex").assertIsDisplayed()
        composeTestRule.onNodeWithText("1600 Amphitheatre Pkwy, Mountain View, CA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Golden Gate Bridge").assertIsDisplayed()
        composeTestRule.onNodeWithText("San Francisco, CA").assertIsDisplayed()
    }

    @Test
    fun placesAutocompleteTextField_typingUpdatesQuery() {
        var query by mutableStateOf("")

        composeTestRule.setContent {
            MaterialTheme {
                PlacesAutocompleteTextField(
                    searchText = query,
                    predictions = emptyList(),
                    onQueryChanged = { query = it },
                    placeHolderText = "Search here",
                )
            }
        }

        composeTestRule.onNodeWithTag("placesAutocompleteSearchField")
            .performTextInput("Mountain View")

        assert(query == "Mountain View")
    }

    @Test
    fun placesAutocompleteTextField_selectingPlaceInvokesCallback() {
        var selectedPlace: AutocompletePlace? = null

        composeTestRule.setContent {
            MaterialTheme {
                PlacesAutocompleteTextField(
                    searchText = "Google",
                    predictions = samplePlaces,
                    onQueryChanged = {},
                    onSelected = { selectedPlace = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Googleplex").performClick()

        val place = assertNotNull(selectedPlace)
        assert(place.placeId == "place_1")
        assert(place.primaryText.toString() == "Googleplex")
    }

    @Test
    fun placesAutocompleteTextField_clearButtonClearsQuery() {
        var query by mutableStateOf("Initial query")

        composeTestRule.setContent {
            MaterialTheme {
                PlacesAutocompleteTextField(
                    searchText = query,
                    predictions = emptyList(),
                    onQueryChanged = { query = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        assert(query.isEmpty())
    }

    @Test
    fun placesAutocompleteTextField_backButtonClickedInvokesCallback() {
        var backClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                PlacesAutocompleteTextField(
                    searchText = "",
                    predictions = emptyList(),
                    onQueryChanged = {},
                    onBackClicked = { backClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(backClicked)
    }

    @Test
    fun placesAutocompleteTextField_previewsRenderCleanly() {
        composeTestRule.setContent {
            AutocompleteFieldPreview()
            AutocompletePlaceRowPreview()
            AutocompletePlaceRowPreviewShortText()
            AutocompletePlaceRowPreviewLongRows()
        }

        composeTestRule.onNodeWithText("463 km").assertIsDisplayed()
        composeTestRule.onNodeWithText("this is a primary test").assertIsDisplayed()
        composeTestRule.onNodeWithText("REI").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "night")
    fun placesAutocompleteTextField_darkThemeAndNotScrollable() {
        composeTestRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                PlacesAutocompleteTextField(
                    searchText = "Dark test",
                    predictions = samplePlaces,
                    onQueryChanged = {},
                    scrollable = false,
                    selectedPlace = samplePlaces.first(),
                    textFieldMaxLines = 1,
                )
            }
        }

        composeTestRule.onNodeWithText("Googleplex").assertIsDisplayed()
    }

    @Test
    fun autocompletePlaceRow_expandedStateRendersAndInvokesCallbacks() {
        var placeSelected = false
        var expandedClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                AutocompletePlaceRow(
                    autocompletePlace = samplePlaces.first(),
                    isSelected = true,
                    onPlaceSelected = { placeSelected = true },
                    onExpandClick = { expandedClicked = true },
                    isExpanded = true,
                    primaryTextMaxLines = 1,
                    secondaryTextMaxLines = 1,
                )
            }
        }

        composeTestRule.onNodeWithText("Googleplex").performClick()
        assert(placeSelected)
        assert(!expandedClicked)
    }

    @Test
    fun placesAutocompleteTextField_clippablePrediction_expandsCollapsesAndSelects() {
        val longPlace = AutocompletePlace(
            placeId = "long_place_1",
            primaryText = SpannableString(
                "Primary line 1\nPrimary line 2\nPrimary line 3",
            ),
            secondaryText = SpannableString(
                "Secondary line 1\nSecondary line 2\nSecondary line 3",
            ),
            distance = null,
        )
        var selectedPlace: AutocompletePlace? = null

        composeTestRule.setContent {
            MaterialTheme {
                PlacesAutocompleteTextField(
                    searchText = "Long",
                    predictions = listOf(longPlace),
                    onQueryChanged = {},
                    onSelected = { selectedPlace = it },
                    primaryTextMaxLines = 1,
                    secondaryTextMaxLines = 1,
                    modifier = Modifier.width(240.dp),
                )
            }
        }

        composeTestRule.onAllNodesWithContentDescription("Expand entry").assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription("Expand entry").performClick()

        composeTestRule.onAllNodesWithContentDescription("Collapse entry").assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription("Collapse entry").performClick()

        composeTestRule.onAllNodesWithContentDescription("Expand entry").assertCountEquals(1)

        // Clicking the text column while clippable selects the place
        composeTestRule.onNodeWithText(longPlace.primaryText.toString()).performClick()
        assert(selectedPlace?.placeId == "long_place_1")
    }

    @Test
    fun nearbyObject_composableFormatting_returnsExpectedStrings() {
        val landmark = NearbyObject.NearbyLandmark(
            Landmark(
                displayName = DisplayName(languageCode = "en", text = "Ferry Building"),
                placeId = "landmark_ferry",
                spatialRelationship = "BESIDE",
                straightLineDistanceMeters = 100.0,
                travelDistanceMeters = 250.0,
                types = listOf("point_of_interest"),
            ),
        )
        val area = NearbyObject.NearbyArea(
            Area(
                containment = "WITHIN",
                displayName = DisplayName(languageCode = "en", text = "Embarcadero"),
                placeId = "area_embarcadero",
            ),
        )

        var landmarkRelation = ""
        var landmarkDistance = ""
        var areaRelation = ""
        var areaDistance = "non-empty"

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnitsConverter provides getUnitsConverter("CA")) {
                landmarkRelation = landmark.spatialRelationship()
                landmarkDistance = landmark.getDistanceString()
                areaRelation = area.spatialRelationship()
                areaDistance = area.getDistanceString()
                Text(text = "$landmarkRelation|$landmarkDistance|$areaRelation|$areaDistance")
            }
        }

        assert(landmarkRelation == "Beside Ferry Building")
        assert(landmarkDistance == "250 m")
        assert(areaRelation == "Within Embarcadero")
        assert(areaDistance.isEmpty())
    }
}
