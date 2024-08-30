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

package com.google.android.libraries.places.compose.demo.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.common.ButtonState


@Composable
fun NextLocationButton(
    buttonState: ButtonState,
    contentDescriptionId: Int = R.string.next_mock_location,
    onClick: () -> Unit,
) {
   NextLocationButton(
       isSelected = buttonState == ButtonState.SELECTED,
       contentDescriptionId = contentDescriptionId,
       onClick = onClick
   )
}

/**
 * Composable function that creates a button to change the mock location to the next location in the list.
 *
 * @param onClick Callback function that is invoked when the button is clicked.
 */
@Composable
fun NextLocationButton(
    isSelected: Boolean,
    contentDescriptionId: Int = R.string.next_mock_location,
    onClick: () -> Unit,
) {
    SelectableButton(
        buttonState = if (isSelected) ButtonState.SELECTED else ButtonState.NORMAL,
        onClick = onClick,
    ) { tint ->
        val description = stringResource(contentDescriptionId)

        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(0.dp)
                .semantics {
                    contentDescription = description
                }
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center).size(24.dp).offset(x = (-6).dp),
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = tint
            )
            Icon(
                modifier = Modifier.align(Alignment.Center).size(24.dp).offset(x = 6.dp),
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = tint
            )
        }
    }
}