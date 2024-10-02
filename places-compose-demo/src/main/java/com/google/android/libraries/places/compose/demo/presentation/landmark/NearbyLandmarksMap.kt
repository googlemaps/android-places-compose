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
package com.google.android.libraries.places.compose.demo.presentation.landmark

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.ui.theme.AndroidPlacesComposeDemoTheme
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState

@Composable
fun NearbyLandmarksMap(
    cameraPositionState: CameraPositionState,
    userMarker: MarkerState,
    modifier: Modifier = Modifier,
    onMapClick: (LatLng) -> Unit = {},
    selectedPlaceId: String?,
    onLandmarkSelected: (String?) -> Unit,
    landmarkMarkers: List<LandmarkMarker>,
) {
    val mapId = stringResource(id = R.string.map_id)

    var showAsMarkers by remember { mutableStateOf(true) }

    LaunchedEffect(cameraPositionState.position.zoom) {
        showAsMarkers = cameraPositionState.position.zoom < 17f
    }

    val droppedPinPinConfig = with(PinConfig.builder()) {
        setBackgroundColor(MaterialTheme.colorScheme.tertiaryContainer.toArgb())
        setBorderColor(MaterialTheme.colorScheme.onTertiaryContainer.toArgb())
        build()
    }

    val selectedPinPinConfig = with(PinConfig.builder()) {
        setBackgroundColor(MaterialTheme.colorScheme.secondaryContainer.toArgb())
        setBorderColor(MaterialTheme.colorScheme.onSecondaryContainer.toArgb())
        setGlyph(PinConfig.Glyph(MaterialTheme.colorScheme.onSecondaryContainer.toArgb()))
        build()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = onMapClick,
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId(mapId)
            },
        ) {
            AdvancedMarker(
                state = userMarker,
                zIndex = 2f
            )
            key(landmarkMarkers) {
                if (showAsMarkers) {
                    landmarkMarkers.forEach { landmarkMarker ->
                        val (pinConfig, zIndex) = if (landmarkMarker.landmark.placeId == selectedPlaceId) {
                            selectedPinPinConfig to 5f
                        } else {
                            droppedPinPinConfig to 1f
                        }

                        AdvancedMarker(
                            state = landmarkMarker.marker,
                            title = landmarkMarker.landmark.name,
                            snippet = landmarkMarker.landmark.spatialRelationship(),
                            zIndex = zIndex,
                            pinConfig = pinConfig,
                            onClick = {
                                onLandmarkSelected(landmarkMarker.landmark.placeId)
                                false
                            }
                        )
                    }
                } else {
                    landmarkMarkers.forEach { landmarkMarker ->
                        val nearbyObject = landmarkMarker.landmark
                        val placeId = landmarkMarker.landmark.placeId

                        if (placeId == selectedPlaceId) {
                            MarkerComposable(
                                state = landmarkMarker.marker,
                                zIndex = 5f,
                                onClick = {
                                    onLandmarkSelected(placeId)
                                    false
                                }
                            ) {
                                LandmarkMarker(
                                    nearbyObject.name,
                                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    borderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            }
                        } else {
                            MarkerComposable(
                                state = landmarkMarker.marker,
                                zIndex = 1f,
                                onClick = {
                                    onLandmarkSelected(placeId)
                                    false
                                }
                            ) {
                                LandmarkMarker(nearbyObject.name)
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(selectedPlaceId, landmarkMarkers) {
            landmarkMarkers.forEach { landmarkMarker ->
                if (landmarkMarker.landmark.placeId == selectedPlaceId) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLng(landmarkMarker.marker.position)
                    )
                    landmarkMarker.marker.showInfoWindow()
                } else {
                    landmarkMarker.marker.hideInfoWindow()
                }
            }
        }
    }
}

// TODO: better outline for the landmark marker
@Composable
fun LandmarkMarker(
    label: String,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    val dipSize = 16.dp
    val dipHeight = 12.dp
    val cornerRadius = 14.dp // Adjust corner radius as needed
    val density = LocalDensity.current

    val borderShape = GenericShape { size, _ ->
        with(density) {
            reset()

            addRoundRect(
                roundRect = androidx.compose.ui.geometry.RoundRect(
                    rect = Rect(
                        offset = Offset(
                            x = 0f,
                            y = 0f
                        ),
                        size = Size(
                            width = size.width,
                            height = size.height
                        )
                    ),
                    topLeft = CornerRadius(cornerRadius.toPx()),
                    topRight = CornerRadius(cornerRadius.toPx()),
                    bottomRight = CornerRadius(cornerRadius.toPx()), // No corner radius at the dip
                    bottomLeft = CornerRadius(cornerRadius.toPx()) // No corner radius at the dip
                )
            )

            moveTo(size.width / 2f, size.height + dipHeight.toPx())
            lineTo(size.width / 2f + (dipSize.toPx() / 2), size.height)
            lineTo(size.width / 2f - (dipSize.toPx() / 2), size.height)
        }
    }

    Row(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = borderShape
            )
            .background(
                color = backgroundColor,
                shape = borderShape
            )
            .padding(
                top = 2.dp,
                bottom = 2.dp,
                end = 6.dp,
                start = 2.dp
            ), // Add padding around the text

    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    shape = CircleShape,
                    color = borderColor,
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = R.drawable.outline_pin_drop_24),
                contentDescription = null,
                tint = backgroundColor
            )
        }
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = label,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LandmarkMarkerPreview() {
    AndroidPlacesComposeDemoTheme {
        LandmarkMarker(label = "Landmark")
    }
}

@Preview(showBackground = true)
@Composable
fun LandmarkMarkerSelectedPreview() {
    AndroidPlacesComposeDemoTheme {
        LandmarkMarker(
            label = "Landmark",
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            borderColor = MaterialTheme.colorScheme.onSecondaryContainer,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}
