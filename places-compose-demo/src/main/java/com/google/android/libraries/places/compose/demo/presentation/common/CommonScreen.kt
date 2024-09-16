package com.google.android.libraries.places.compose.demo.presentation.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.places.compose.autocomplete.data.LocalUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.getUnitsConverter
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.components.NextLocationButton
import com.google.android.libraries.places.compose.demo.presentation.components.SelectableButton
import com.google.android.libraries.places.compose.demo.presentation.landmark.GetLocationPermission
import com.google.android.libraries.places.compose.demo.ui.theme.AndroidPlacesComposeDemoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonScreen(
    @StringRes titleId: Int,
    commonViewModel: CommonViewModel,
    onNavigateUp: () -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable (PaddingValues) -> Unit,
) {
    val commonViewState by commonViewModel.commonViewState.collectAsStateWithLifecycle()
    val country = commonViewState.countryCode
        ?: LocalContext.current.resources.configuration.locales.get(0).country

    // Determine which units converter to use based on the country.
    val unitsConverter = remember(country) {
        getUnitsConverter(country)
    }

    fun onEvent(event: CommonEvent) {
        commonViewModel.onEvent(event)
    }

    AndroidPlacesComposeDemoTheme {
        GetLocationPermission {
            CompositionLocalProvider(LocalUnitsConverter provides unitsConverter) {
                Scaffold(
                    modifier = Modifier.fillMaxSize().systemBarsPadding(),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                                actionIconContentColor = MaterialTheme.colorScheme.primary,
                                navigationIconContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = { Text(stringResource(titleId)) },
                            navigationIcon = {
                                IconButton(onClick = { onNavigateUp() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            },
                            actions = {
                                SelectableButton(
                                    buttonState = commonViewState.buttonStates.currentLocation,
                                    onClick = { onEvent(CommonEvent.OnUseSystemLocation) },
                                    iconId = R.drawable.baseline_my_location_24,
                                    contentDescription = R.string.fill_address_from_current_location
                                )

                                NextLocationButton(
                                    isSelected = commonViewState.buttonStates.mockLocation == ButtonState.SELECTED
                                ) {
                                    onEvent(CommonEvent.OnNextMockLocation)
                                }

                                SelectableButton(
                                    buttonState = commonViewState.buttonStates.map,
                                    iconId = R.drawable.baseline_map_24,
                                    contentDescription = R.string.toggle_map,
                                    onClick = { onEvent(CommonEvent.OnToggleMap) }
                                )
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) { paddingValues ->
                    content(paddingValues)
                }
            }
        }
    }
}