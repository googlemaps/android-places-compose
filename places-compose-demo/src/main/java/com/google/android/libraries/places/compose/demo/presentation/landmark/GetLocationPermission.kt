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

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.libraries.places.compose.demo.R

/**
 * A composable that handles the logic for requesting location permissions, with an optional
 * fallback to preset mock locations if the user chooses not to grant device permissions.
 *
 * @param modifier The modifier to apply to the composable.
 * @param onFallbackToMock Callback invoked when the user elects to use mock location fallback.
 * @param content The content to display if permissions are granted or mock fallback is selected.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetLocationPermission(
    modifier: Modifier = Modifier,
    onFallbackToMock: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    var useMockFallback by rememberSaveable { mutableStateOf(false) }

    if (locationPermissionsState.allPermissionsGranted || useMockFallback) {
        content()
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val allPermissionsRevoked =
                        locationPermissionsState.permissions.size ==
                                locationPermissionsState.revokedPermissions.size

                    val titleText = if (!allPermissionsRevoked) {
                        "Precise Location Access"
                    } else {
                        "Location Access Required"
                    }

                    val textToShow = if (!allPermissionsRevoked) {
                        "Thank you for granting access to your approximate location. To provide you with " +
                                "the most accurate and relevant information, please grant permission " +
                                "to access your precise location."
                    } else if (locationPermissionsState.shouldShowRationale) {
                        "Getting your location is important for this demo to provide relevant places and autocomplete suggestions."
                    } else {
                        "This feature requires location permission to bias place suggestions and display relevant map markers."
                    }

                    val buttonText = if (!allPermissionsRevoked) {
                        "Allow Precise Location"
                    } else {
                        "Grant Location Permission"
                    }

                    Icon(
                        painter = painterResource(R.drawable.baseline_my_location_24),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = textToShow,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(buttonText)
                    }

                    OutlinedButton(
                        onClick = {
                            useMockFallback = true
                            onFallbackToMock?.invoke()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Use Mock Location Instead")
                    }
                }
            }
        }
    }
}