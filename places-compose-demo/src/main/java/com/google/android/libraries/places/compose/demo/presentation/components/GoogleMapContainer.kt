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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.demo.R
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

/**
 * A composable function that displays a Google Map with a marker at the location if provided.
 *
 * @param modifier The modifier to be applied to the container.
 * @param markerLatLng The [LatLng] object representing the location to display.
 * @param markerTitle The title of the marker.
 * @param markerSnippet The snippet of the marker.
 * @param header A composable function that displays a header for the card.
 */
@Composable
fun GoogleMapContainer(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    markerLatLng: LatLng? = null,
    markerTitle: String? = null,
    markerSnippet: String? = null,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onMapClick: (LatLng) -> Unit = {},
    onMapCloseClick: (() -> Unit)? = null,
) {
    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    header()
                }

                if (onMapCloseClick != null) {
                    IconButton(onClick = onMapCloseClick) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.close_the_map)
                        )
                    }
                }
            }


            if (markerLatLng != null) {
                val markerState = rememberMarkerState(position = markerLatLng)

                LaunchedEffect(markerLatLng) {
                    markerState.position = markerLatLng
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            markerLatLng,
                            15f
                        ), 1500
                    )
                }

                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    onMapClick = onMapClick,
                ) {
                    AdvancedMarker(
                        state = markerState,
                        title = markerTitle,
                        snippet = markerSnippet,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun GoogleMapContainerPreview() {
    GoogleMapContainer(
        markerLatLng = LatLng(0.0, 0.0),
        markerTitle = "Marker Title",
        markerSnippet = "Marker Snippet",
        header = {
            Column {
                Text(
                    text = "Preview Header Line1",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = "Preview Header Line2",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
        onMapCloseClick = {}
    )
}