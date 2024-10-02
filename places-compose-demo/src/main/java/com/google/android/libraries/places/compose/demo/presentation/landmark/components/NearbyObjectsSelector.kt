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

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.compose.autocomplete.models.NearbyObject
import com.google.android.libraries.places.compose.demo.R

@Composable
fun NearbyObjectsSelector(
    nearbyObjects: List<NearbyObject>,
    onNearbyLandmarkSelected: (String?) -> Unit,
    selectedPlaceId: String?
) {
    var showLandmarks by rememberSaveable { mutableStateOf(true) }
    var showAreas by rememberSaveable { mutableStateOf(false) }
    var showDialog by rememberSaveable { mutableStateOf(false) } // State to control dialog visibility

    if (showDialog) {
        NearbyObjectsFilterDialog(
            showLandmarks,
            showAreas,
            onDismissRequest = { showLandmarksNew, showAreasNew ->
                showDialog = false
                showLandmarks = showLandmarksNew
                showAreas = showAreasNew
            }
        )
    }

    val landmarks by rememberSaveable(nearbyObjects) { mutableStateOf(nearbyObjects.filterIsInstance<NearbyObject.NearbyLandmark>()) }
    val areas by rememberSaveable(nearbyObjects) { mutableStateOf(nearbyObjects.filterIsInstance<NearbyObject.NearbyArea>()) }

    if (landmarks.isNotEmpty()) {
        NearbyObjectsMenu(
            modifier = Modifier,
            titleId = R.string.nearby_landmarks,
            nearbyObjects = landmarks,
            onNearbyObjectSelected = {
                onNearbyLandmarkSelected(it.placeId)
            },
            selectedPlaceId = selectedPlaceId,
        )
    }

    if (areas.isNotEmpty()) {
        NearbyObjectsMenu(
            titleId = R.string.nearby_areas,
            nearbyObjects = areas,
            modifier = Modifier,
            selectedPlaceId = selectedPlaceId
        )
    }
}

@Composable
private fun NearbyObjectsFilterDialog(
    showLandmarksInitial: Boolean,
    showAreasInitial: Boolean,
    onDismissRequest: (Boolean, Boolean) -> Unit
) {
    var showLandmarks by rememberSaveable { mutableStateOf(showLandmarksInitial) }
    var showAreas by rememberSaveable { mutableStateOf(showAreasInitial) }

    AlertDialog(
        onDismissRequest = { onDismissRequest(showLandmarksInitial, showAreasInitial) },
        title = { Text("Filter nearby objects types") },
        confirmButton = {
            Button(onClick = { onDismissRequest(showLandmarks, showAreas) }) {
                Text("Apply")
            }
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showLandmarks,
                        onCheckedChange = { showLandmarks = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Landmarks")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showAreas,
                        onCheckedChange = { showAreas = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Areas")
                }
            }
        }
    )
}