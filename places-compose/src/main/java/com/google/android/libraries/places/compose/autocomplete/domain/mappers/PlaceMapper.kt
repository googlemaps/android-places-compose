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

package com.google.android.libraries.places.compose.autocomplete.domain.mappers

import com.google.android.libraries.places.compose.autocomplete.models.Address
import com.google.android.libraries.places.compose.autocomplete.models.geocoder.AddressComponent

private fun com.google.android.libraries.places.api.model.AddressComponent.toAddressComponent() =
    AddressComponent(
        longName = this.name,
        shortName = this.shortName ?: "",
        types = this.types
    )

fun List<com.google.android.libraries.places.api.model.AddressComponent>.toAddress(): Address =
    map { it.toAddressComponent() }.toAddressComponentMultiMap().toAddress()
