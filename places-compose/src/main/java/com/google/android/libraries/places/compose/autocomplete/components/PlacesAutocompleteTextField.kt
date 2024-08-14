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

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.compose.R
import com.google.android.libraries.places.compose.autocomplete.data.meters
import com.google.android.libraries.places.compose.autocomplete.data.toDistanceString
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import kotlinx.coroutines.flow.filter


@Composable
private fun AutoCompleteTextField(
    modifier: Modifier = Modifier,
    queryText: String = "",
    onQueryChanged: (String) -> Unit,
    onBackClicked: (() -> Unit)? = null,
    maxLines: Int,
    placeHolderText: String = "",
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 20.dp)
            .testTag("placesAutocompleteSearchField"),
        shape = RoundedCornerShape(25.dp),
        value = queryText,
        onValueChange = { onQueryChanged(it) },
        leadingIcon = onBackClicked?.let {
            {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        trailingIcon = {
            if (queryText.isNotBlank())
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                }
        },
        singleLine = maxLines == 1,
        maxLines = maxLines,
        placeholder = {
            Text(
                text = placeHolderText,
            )
        }
    )
}

internal val predictionsHighlightStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold)

@Composable
internal fun AutocompletePlaceRow(
    autocompletePlace: AutocompletePlace,
    isSelected: Boolean,
    onPlaceSelected: (AutocompletePlace) -> Unit,
    primaryTextMaxLines: Int,
    secondaryTextMaxLines: Int,
    isExpanded: Boolean = false,
    onExpandClick: (String) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
            )
            .clickable { onPlaceSelected(autocompletePlace) }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AutocompleteIcon(
            caption = autocompletePlace.distance?.toDistanceString()
        )
        Spacer(modifier = Modifier.width(16.dp))

        val primaryText = autocompletePlace.primaryText.toAnnotatedString(predictionsHighlightStyle)
        val secondaryText = autocompletePlace.secondaryText.toAnnotatedString(predictionsHighlightStyle)

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {

            Column(
                modifier = Modifier.weight(1f, true),
            ) {
                val (primaryTextLines, secondaryTextLines) = if (isExpanded) {
                    Int.MAX_VALUE to Int.MAX_VALUE
                } else {
                    primaryTextMaxLines to secondaryTextMaxLines
                }

                var primaryTextLayoutResult by remember(autocompletePlace) { mutableStateOf<TextLayoutResult?>(null) }
                var secondaryTextLayoutResult by remember(autocompletePlace) { mutableStateOf<TextLayoutResult?>(null) }

                // This indicates whether the text is currently clipped.
                val isClipped by remember(autocompletePlace) {
                    derivedStateOf {
                        primaryTextLayoutResult?.hasVisualOverflow == true ||
                                secondaryTextLayoutResult?.hasVisualOverflow == true
                    }
                }

                // This remembers if the text can be clipped even if it is not currently clipped.
                // It acts like a latch in that once it becomes true, it will stay true.
                var isClippable by remember(autocompletePlace) { mutableStateOf(false) }
                LaunchedEffect(autocompletePlace) {
                    snapshotFlow { isClipped }
                        .filter { it }
                        .collect { isClippable = true }
                }

                val targetSizeDp: Dp = 44.dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isClippable) {
                                Modifier.clickable { onExpandClick(autocompletePlace.placeId) }
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f, true)
                            .animateContentSize()
                            .then(
                                if (isClippable) {
                                    Modifier.clickable { onPlaceSelected(autocompletePlace) }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = primaryTextLines,
                            onTextLayout = { primaryTextLayoutResult = it },
                            overflow = TextOverflow.Ellipsis,
                            text = primaryText,
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            onTextLayout = { secondaryTextLayoutResult = it },
                            maxLines = secondaryTextLines,
                            overflow = TextOverflow.Ellipsis,
                            text = secondaryText,
                        )
                    }

                    if (isClippable) {
                        Icon(
                            imageVector = if (isClipped) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(if (isExpanded) R.string.collapse_entry else R.string.expand_entry),
                            modifier = Modifier
                                .size(targetSizeDp)
                                .padding(top = 16.dp)
                                .clickable { onExpandClick(autocompletePlace.placeId) }
                                .align(Alignment.Bottom)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutocompleteIcon(caption: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier =
            Modifier
                .background(color = Color.LightGray.copy(0.5f), shape = CircleShape)
                .size(40.dp)
        ) {
            Icon(
                modifier = Modifier.padding(8.dp),
                imageVector = Icons.Outlined.Place,
                contentDescription = stringResource(R.string.distance),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        caption?.let { distanceString ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = distanceString,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Composable function to display an autocomplete list of places.
 *
 * @param onSelected Callback function to be invoked when a place is selected.
 * @param searchText The current search text entered by the user.
 * @param onQueryChanged Callback function to be invoked when the search text changes.
 * @param predictions List of place details to be displayed in the autocomplete list.
 * @param modifier Modifier to be applied to the composable.
 */
@Composable
fun PlacesAutocompleteTextField(
    searchText: String,
    predictions: List<AutocompletePlace>,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSelected: (AutocompletePlace) -> Unit = {},
    selectedPlace: AutocompletePlace? = null,
    textFieldMaxLines: Int = 2,
    primaryTextMaxLines: Int = 2,
    secondaryTextMaxLines: Int = 2,
    onBackClicked: (() -> Unit)? = null,
    scrollable: Boolean = true,
    placeHolderText: String = "",
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scrollState = rememberScrollState()

    // Forget which place is expanded when we get a new set of predictions.
    var expandedPlaceId by remember(predictions) { mutableStateOf<String?>(null) }

    Column(modifier = modifier) {
        AutoCompleteTextField(
            queryText = searchText,
            onQueryChanged = { onQueryChanged(it) },
            onBackClicked = onBackClicked,
            maxLines = textFieldMaxLines,
            placeHolderText = placeHolderText,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .then(if (scrollable) Modifier.verticalScroll(scrollState) else Modifier)
        ) {
            for (prediction in predictions) {
                AutocompletePlaceRow(
                    autocompletePlace = prediction,
                    isSelected = prediction.placeId == selectedPlace?.placeId,
                    onPlaceSelected = {
                        keyboardController?.hide()
                        onSelected(it)
                    },
                    isExpanded = expandedPlaceId == prediction.placeId,
                    onExpandClick = {
                        keyboardController?.hide()
                        expandedPlaceId =
                            if (expandedPlaceId == prediction.placeId) null else prediction.placeId
                    },
                    primaryTextMaxLines = primaryTextMaxLines,
                    secondaryTextMaxLines = secondaryTextMaxLines,
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }

        val logoAsset = if (isSystemInDarkTheme()) {
            R.drawable.google_on_non_white
        } else {
            R.drawable.google_on_white
        }

        if (predictions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Image(
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp),
            painter = painterResource(logoAsset),
            contentDescription = stringResource(R.string.google)
        )
    }
}

/**
 * Attempts to convert a [SpannableString] to an [AnnotatedString]. This is not intended to be a
 * general purpose solution. Instead, all of the spans are styled using the given [spanStyle] in the
 * resulting AnnotatedString.
 */
internal fun Spannable.toAnnotatedString(spanStyle: SpanStyle?): AnnotatedString {
    return buildAnnotatedString {
        if (spanStyle == null) {
            append(this@toAnnotatedString.toString())
        } else {
            var last = 0
            for (span in getSpans(0, this@toAnnotatedString.length, Any::class.java)) {
                val start = this@toAnnotatedString.getSpanStart(span)
                val end = this@toAnnotatedString.getSpanEnd(span)
                if (last < start) {
                    append(this@toAnnotatedString.substring(last, start))
                }
                withStyle(spanStyle) {
                    append(this@toAnnotatedString.substring(start, end))
                }
                last = end
            }
            if (last < this@toAnnotatedString.length) {
                append(this@toAnnotatedString.substring(last))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AutocompleteFieldPreview() {
    MaterialTheme {
        AutocompleteIcon("463 km")
    }
}

@Preview(showBackground = true)
@Composable
fun AutocompletePlaceRowPreview() {
    MaterialTheme {
        AutocompletePlaceRow(
            AutocompletePlace(
                placeId = "test_id",
                primaryText = SpannableString("this is a primary test"),
                secondaryText = SpannableString("this is a secondary test"),
                distance = 150.0.meters,
            ),
            isSelected = false,
            onPlaceSelected = {},
            isExpanded = false,
            primaryTextMaxLines = 1,
            secondaryTextMaxLines = 3,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AutocompletePlaceRowPreviewLongRows() {
    val longPrimaryText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed et magna " +
            "porta erat luctus semper. In non erat egestas, elementum lacus eget, pretium purus"

    val longSecondaryText = "Nulla consequat libero et felis convallis, vel aliquam sem " +
            "pellentesque. Praesent ac eros id tellus ornare rutrum ac sed magna. Ut a massa velit."

    val primaryText = SpannableStringBuilder(longPrimaryText).apply {
        setSpan(
            /* what = */ StyleSpan(Typeface.BOLD),
            /* start = */ 0,
            /* end = */ 8,
            /* flags = */ SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    val secondaryText = SpannableStringBuilder(longSecondaryText).apply {
        setSpan(
            /* what = */ StyleSpan(Typeface.BOLD),
            /* start = */ 8,
            /* end = */ 13,
            /* flags = */ SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    MaterialTheme {
        AutocompletePlaceRow(
            AutocompletePlace(
                placeId = "test_id",
                primaryText = primaryText,
                secondaryText = secondaryText,
                distance = 150.0.meters,
            ),
            isSelected = false,
            onPlaceSelected = {},
            isExpanded = false,
            primaryTextMaxLines = 1,
            secondaryTextMaxLines = 3,
        )
    }
}
