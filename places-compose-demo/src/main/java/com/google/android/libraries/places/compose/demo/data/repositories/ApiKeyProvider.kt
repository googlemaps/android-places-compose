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
package com.google.android.libraries.places.compose.demo.data.repositories

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.google.android.libraries.places.compose.demo.BuildConfig

/**
 * Provides the Google Maps API key from the AndroidManifest.xml file.
 *
 * @param context The application context.
 */
class ApiKeyProvider(private val context: Context) {
    val mapsApiKey: String by lazy {
        getMapsApiKeyFromManifest()
    }

    val placesApiKey: String = BuildConfig.PLACES_API_KEY

    private fun getMapsApiKeyFromManifest(): String {
        val mapsApiKey =
            try {
                val applicationInfo =
                    context.packageManager.getApplicationInfo(
                        context.packageName,
                        PackageManager.GET_META_DATA,
                    )
                applicationInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
            } catch (e: PackageManager.NameNotFoundException) {
                ""
            }
        if (mapsApiKey.isBlank()) {
            // TODO: get the right error message/behavior.
            error("MapsApiKey missing from AndroidManifest.")
        }
        return mapsApiKey
    }

    init {
        if (placesApiKey == "DEFAULT_API_KEY" || (getMapsApiKeyFromManifest() == "DEFAULT_API_KEY")) {
            Toast.makeText(
                context,
                "One or more API keys have not been set.  Please see the README.md file.",
                Toast.LENGTH_LONG
            ).show()
            error("One or more API keys have not been set.  Please see the README.md file.")
        }
    }
}
