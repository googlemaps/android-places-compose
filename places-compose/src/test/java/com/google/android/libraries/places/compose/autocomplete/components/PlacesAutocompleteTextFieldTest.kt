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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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
            distance = 350.meters
        ),
        AutocompletePlace(
            placeId = "place_2",
            primaryText = SpannableString("Golden Gate Bridge"),
            secondaryText = SpannableString("San Francisco, CA"),
            distance = 15000.meters
        )
    )

    @Test
    fun placesAutocompleteTextField_displaysPlaceholderAndInitialPredictions() {
        composeTestRule.setContent {
            MaterialTheme {
                PlacesAutocompleteTextField(
                    searchText = "",
                    predictions = samplePlaces,
                    onQueryChanged = {},
                    placeHolderText = "Search Google Maps"
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
                    placeHolderText = "Search here"
                )
            }
        }

        composeTestRule.onNodeWithTag("placesAutocompleteSearchField")
            .performTextInput("Mountain View")

        assertThat(query).isEqualTo("Mountain View")
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
                    onSelected = { selectedPlace = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Googleplex").performClick()

        assertThat(selectedPlace).isNotNull()
        assertThat(selectedPlace?.placeId).isEqualTo("place_1")
        assertThat(selectedPlace?.primaryText.toString()).isEqualTo("Googleplex")
    }

    @Test
    fun placesAutocompleteTextField_clearButtonClearsQuery() {
        var query by mutableStateOf("Initial query")

        composeTestRule.setContent {
            MaterialTheme {
                PlacesAutocompleteTextField(
                    searchText = query,
                    predictions = emptyList(),
                    onQueryChanged = { query = it }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        assertThat(query).isEmpty()
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
                    onBackClicked = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertThat(backClicked).isTrue()
    }
}
