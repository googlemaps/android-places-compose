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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.libraries.places.compose.demo.R
import com.google.android.libraries.places.compose.demo.data.repositories.CountriesRepository

@Composable
fun CountryField(address: DisplayAddress) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = address.country,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.us_address_country)) },
        leadingIcon = CountriesRepository.countries[address.countryCode]?.flag?.let {
            {
                Text(it)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CountryFieldPreview() {
    val address = object : DisplayAddress {
        override val countryCode: String = "US"
        override val country: String = "United States"
        override fun toFormattedAddress(): String {
            TODO("Not yet implemented")
        }
    }

    CountryField(address)
}

@Preview(showBackground = true)
@Composable
fun CountryFieldPreview_India() {
    val address = object : DisplayAddress {
        override val countryCode: String = "IN"
        override val country: String = "India"
        override fun toFormattedAddress(): String {
            TODO("Not yet implemented")
        }
    }

    CountryField(address)
}

@Preview(showBackground = true)
@Composable
fun CountryFieldPreview_NoFlag() {
    val address = object : DisplayAddress {
        override val countryCode: String = "WA"
        override val country: String = "Wakanda"
        override fun toFormattedAddress(): String {
            TODO("Not yet implemented")
        }
    }

    CountryField(address)
}
