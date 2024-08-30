package com.google.android.libraries.places.compose.demo.presentation.common

sealed class CommonEvent {
    data object OnNextMockLocation: CommonEvent()
    data object OnToggleMap: CommonEvent()
    data object OnUseSystemLocation: CommonEvent()
    data object OnMapCloseClicked: CommonEvent()
    data object OnNavigateUp: CommonEvent()
}
