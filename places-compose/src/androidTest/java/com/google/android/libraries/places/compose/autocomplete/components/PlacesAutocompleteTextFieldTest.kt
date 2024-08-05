package com.google.android.libraries.places.compose.autocomplete.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.text.SpannableString
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import com.google.android.libraries.places.compose.autocomplete.data.LocalUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.getUnitsConverter

class PlacesAutocompleteTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testPlace = AutocompletePlace(
        placeId = "test_id",
        primaryText = SpannableString("Primary Text"),
        secondaryText = SpannableString("Secondary Text"),
        distance = 1000.0.meters,
    )

    @Test
    fun autoCompleteTextField_emptyQuery_displaysPlaceholder() {
        composeTestRule.setContent {
            PlacesAutocompleteTextField(
                searchText = "",
                predictions = emptyList(),
                onQueryChanged = {},
                modifier = Modifier.testTag("autocompleteTextField")
            )
        }
        composeTestRule.onNodeWithTag("placesAutocompleteSearchField")
            .assertIsDisplayed()
            .assert(hasText("")) // Assuming empty query shows placeholder
    }

    @Test
    fun autoCompleteTextField_queryText_updatesOnValueChange() {
        composeTestRule.setContent {
            var query by remember { mutableStateOf("") }
            PlacesAutocompleteTextField(
                searchText = query,
                predictions = emptyList(),
                onQueryChanged = { query = it },
                modifier = Modifier.testTag("autocompleteTextField")
            )
        }
        composeTestRule.onNodeWithTag("placesAutocompleteSearchField")
            .performTextInput("test")
        composeTestRule.onNodeWithTag("placesAutocompleteSearchField")
            .assert(hasText("test"))
    }

    @Test
    fun autocompletePlaceRow_displaysCorrectInfo() {
        composeTestRule.setContent {
            val unitsConverter = getUnitsConverter("CA")
            CompositionLocalProvider(LocalUnitsConverter provides unitsConverter) {
                AutocompletePlaceRow(
                    autocompletePlace = testPlace,
                    isSelected = false,
                    onPlaceSelected = {},
                    primaryTextMaxLines = 2,
                    secondaryTextMaxLines = 3,
                )
            }
        }

        composeTestRule.onNodeWithText("Primary Text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Secondary Text").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Distance").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.0 km").assertIsDisplayed() // Distance converted and displayed
    }

    @Test
    fun autocompletePlaceRow_displaysCorrectInfo_inUSUnits() {
        composeTestRule.setContent {
            val unitsConverter = getUnitsConverter("US")
            CompositionLocalProvider(LocalUnitsConverter provides unitsConverter) {
                AutocompletePlaceRow(
                    autocompletePlace = testPlace,
                    isSelected = false,
                    onPlaceSelected = {},
                    primaryTextMaxLines = 2,
                    secondaryTextMaxLines = 3,
                )
            }
        }

        composeTestRule.onNodeWithText("Primary Text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Secondary Text").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Distance").assertIsDisplayed()
        composeTestRule.onRoot().printToLog("AutocompletePlaceRowTest")
        composeTestRule.onNodeWithText("0.6 miles").assertIsDisplayed() // Distance converted and displayed
    }

    @Test
    fun autocompletePlaceRow_expandable() {
        composeTestRule.setContent {
            var isExpanded by remember { mutableStateOf(false) }
            AutocompletePlaceRow(
                autocompletePlace = testPlace.copy(
                    primaryText = SpannableString("Very long primary text that should be truncated"),
                    secondaryText = SpannableString("Very long secondary text that should be truncated")
                ),
                isSelected = false,
                onPlaceSelected = {},
                primaryTextMaxLines = 1,
                secondaryTextMaxLines = 1,
                isExpanded = isExpanded,
                onExpandClick = { isExpanded = !isExpanded }
            )
        }

        composeTestRule.onAllNodesWithContentDescription("Expand entry").assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription("Expand entry").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Expand entry").performClick() // Toggle expansion

        composeTestRule.onAllNodesWithContentDescription("Collapse entry").assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription("Collapse entry").assertIsDisplayed()
    }
}
