package com.google.android.libraries.places.compose.demo.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.libraries.places.compose.autocomplete.data.LocalUnitsConverter
import com.google.android.libraries.places.compose.autocomplete.data.getUnitsConverter
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.presentation.components.NextLocationButton
import com.google.android.libraries.places.compose.demo.presentation.components.SelectableButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonScreen(
    viewModel: CommonViewModel,
    onNavigateUp: () -> Unit,
) {
    val viewState by viewModel.commonViewState.collectAsState()
    val country =
        viewState.countryCode ?: LocalContext.current.resources.configuration.locales.get(0).country

    // Determine which units converter to use based on the country.
    val unitsConverter = remember(country) {
        getUnitsConverter(country)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    fun onEvent(event: CommonEvent) {
        viewModel.onEvent(event)
    }

    CompositionLocalProvider(LocalUnitsConverter provides unitsConverter) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = { Text(stringResource(R.string.cart)) },
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
                            buttonState = viewState.buttonStates.currentLocation,
                            onClick = { onEvent(CommonEvent.OnUseSystemLocation) },
                            iconId = R.drawable.baseline_my_location_24,
                            contentDescription = R.string.fill_address_from_current_location
                        )

                        NextLocationButton(
                            isSelected = viewState.buttonStates.mockLocation == ButtonState.SELECTED
                        ) {
                            onEvent(CommonEvent.OnNextMockLocation)
                        }

                        SelectableButton(
                            buttonState = viewState.buttonStates.map,
                            iconId = R.drawable.baseline_map_24,
                            contentDescription = R.string.toggle_map,
                            onClick = { onEvent(CommonEvent.OnToggleMap) }
                        )
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text("Common Screen")
            }
        }
    }
}