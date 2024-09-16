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
package com.google.android.libraries.places.compose.demo.presentation.landmark.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.demo.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NearbyObjectsMenu(
    @StringRes titleId: Int,
    nearbyObjects: List<NearbyObject>,
    modifier: Modifier = Modifier,
    selectedObject: NearbyObject?,
    onNearbyObjectSelected: (NearbyObject) -> Unit = {},
) {
    require(nearbyObjects.isNotEmpty()) { "Nearby objects list cannot be empty" }

    var expanded by remember { mutableStateOf(false) }
    var selectedOption = selectedObject ?: nearbyObjects.first()

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOption.spatialRelationship(),
            onValueChange = { },
            label = { Text(stringResource(titleId)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            nearbyObjects.forEachIndexed { index, option ->
                DropdownMenuItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Place,
                                    contentDescription = stringResource(R.string.distance)
                                )
                                if (option.getDistanceString().isNotBlank()) {
                                    Text(
                                        text = option.getDistanceString(),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = option.spatialRelationship())
                                }
                            }
                        }
                    },
                    onClick = {
                        selectedOption = option
                        expanded = false
                        onNearbyObjectSelected(selectedOption)
                    }
                )
                if (index < nearbyObjects.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}