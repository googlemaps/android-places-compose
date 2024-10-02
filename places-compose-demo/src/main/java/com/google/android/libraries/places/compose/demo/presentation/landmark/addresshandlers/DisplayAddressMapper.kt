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
package com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers

import com.google.android.libraries.places.compose.autocomplete.models.Address
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.`in`.toIndiaDisplayAddress
import com.google.android.libraries.places.compose.demo.presentation.landmark.addresshandlers.us.toUsDisplayAddress

// TODO: Make this dynamic and pluggable
fun Address.toDisplayAddress(): DisplayAddress {
    return when(countryCode) {
        "US" -> this.toUsDisplayAddress()
        "IN" -> this.toIndiaDisplayAddress()
        else -> {
            // Fallback to US address format
            this.toUsDisplayAddress()
        }
    }
}