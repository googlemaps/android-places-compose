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
package com.google.android.libraries.places.compose.demo.presentation.autocomplete

import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace

/**
 * Represents events that can be emitted by the autocomplete view.
 */
sealed class AutocompleteEvent {
    data class OnPlaceSelected(val autocompletePlace: AutocompletePlace) : AutocompleteEvent()
    data class OnQueryChanged(
        val query: String,
        val actions: FindAutocompletePredictionsRequest.Builder.() -> Unit = {}
    ) : AutocompleteEvent()

    data object OnNextMockLocation: AutocompleteEvent()
    data object OnToggleMap: AutocompleteEvent()
    data class OnSetMapVisible(val visible: Boolean): AutocompleteEvent()
    data object OnUseDeviceLocation: AutocompleteEvent()
}
