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

package com.google.android.libraries.places.compose.demo.presentation.addresscompletion

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.DisplayAddress

/**
 * Events handled by the [AddressCompletionViewModel].
 */
sealed class AddressCompletionEvent {
    data class OnAddressSelected(val autocompletePlace: AutocompletePlace) : AddressCompletionEvent()
    data class OnAddressChanged(val displayAddress: DisplayAddress) : AddressCompletionEvent()
    data class OnMapClicked(val latLng: LatLng) : AddressCompletionEvent()
    data object OnNavigateUp: AddressCompletionEvent()
}