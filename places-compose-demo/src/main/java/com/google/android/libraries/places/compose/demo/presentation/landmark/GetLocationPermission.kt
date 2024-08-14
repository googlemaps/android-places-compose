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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * A composable that handles the logic for requesting location permissions.
 *
 * @param modifier The modifier to apply to the composable.
 * @param content The content to display if the location permissions are granted.
 *
 * This composable uses the `rememberMultiplePermissionsState` API to manage the state of the location permissions.
 * It displays a different message and button based on the current state of the permissions.
 *
 * If the user has granted all the permissions, the `content` composable is displayed.
 * If the user has denied all the permissions, a message explaining why the permissions are needed is displayed,
 * along with a button to request the permissions.
 * If the user has only granted the coarse location permission, a message asking the user to grant the fine location permission is displayed,
 * along with a button to request the fine location permission.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetLocationPermission(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    if (locationPermissionsState.allPermissionsGranted) {
        content()
    } else {
        Column(modifier = modifier.padding(16.dp)) {
            val allPermissionsRevoked =
                locationPermissionsState.permissions.size ==
                        locationPermissionsState.revokedPermissions.size

            val textToShow = if (!allPermissionsRevoked) {
                // If not all the permissions are revoked, it's because the user accepted the COARSE
                // location permission, but not the FINE one.
                "Thank you for granting access to your approximate location. To provide you with " +
                        "the most accurate and relevant information, please grant permission " +
                        "to access your precise location. This will enable the app to tailor " +
                        "responses to your specific needs and circumstances, ensuring a more " +
                        "relevant and effective experience."
            } else if (locationPermissionsState.shouldShowRationale) {
                // Both location permissions have been denied
                "Getting your exact location is important for this app. " +
                        "Please grant us fine location. Thank you :D"
            } else {
                // First time the user sees this feature or the user doesn't want to be asked again
                "This feature requires location permission"
            }

            val buttonText = if (!allPermissionsRevoked) {
                "Allow precise location"
            } else {
                "Request permissions"
            }

            Text(text = textToShow)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                Text(buttonText)
            }
        }
    }
}