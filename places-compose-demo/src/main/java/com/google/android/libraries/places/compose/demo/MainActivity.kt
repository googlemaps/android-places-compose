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
package com.google.android.libraries.places.compose.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.libraries.places.compose.demo.ui.theme.AndroidPlacesComposeDemoTheme
import android.content.Intent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.compose.demo.data.repositories.ApiKeyProvider
import com.google.android.libraries.places.compose.demo.presentation.autocomplete.AutocompleteActivity
import com.google.android.libraries.places.compose.demo.presentation.landmark.LandmarkSelectionActivity
import com.google.android.libraries.places.compose.demo.presentation.addresscompletion.AddressCompletionActivity
import com.google.android.libraries.places.compose.demo.presentation.minimal.PlacesAutocompleteMinimalActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var apiKeyProvider: ApiKeyProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidPlacesComposeDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityButtons()
                }
            }
        }
    }
}

data class ActivityItem(
    val activityClass: Class<*>,
    val titleRes: Int,
    val descRes: Int,
)

val activities = listOf(
    ActivityItem(AutocompleteActivity::class.java, R.string.autocomplete_button, R.string.autocomplete_button_desc),
    ActivityItem(LandmarkSelectionActivity::class.java, R.string.landmark_selection_button, R.string.landmark_selection_button_desc),
    ActivityItem(AddressCompletionActivity::class.java, R.string.address_validation_button, R.string.address_validation_button_desc),
    ActivityItem(PlacesAutocompleteMinimalActivity::class.java, R.string.minimal_autocomplete_button, R.string.minimal_autocomplete_button_desc),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityButtons() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(stringResource(R.string.main_activity_title)) }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            for (item in activities) {
                Button(
                    onClick = { context.startActivity(Intent(context, item.activityClass)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(item.titleRes),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(item.descRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}
