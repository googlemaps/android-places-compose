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

import com.google.android.libraries.places.compose.demo.data.models.Country

class CountriesRepository {
    companion object {
        private fun getFlagEmoji(countryCode: String): String {
            val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
            val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
        }

        val countries = mapOf(
            "Afghanistan" to "AF",
            "Algeria" to "DZ",
            "Angola" to "AO",
            "Argentina" to "AR",
            "Bangladesh" to "BD",
            "Brazil" to "BR",
            "Cameroon" to "CM",
            "China" to "CN",
            "Colombia" to "CO",
            "Egypt" to "EG",
            "Ethiopia" to "ET",
            "France" to "FR",
            "Germany" to "DE",
            "Ghana" to "GH",
            "India" to "IN",
            "Indonesia" to "ID",
            "Iran" to "IR",
            "Italy" to "IT",
            "Ivory Coast" to "CI",
            "Japan" to "JP",
            "Kenya" to "KE",
            "Madagascar" to "MG",
            "Malaysia" to "MY",
            "Mexico" to "MX",
            "Morocco" to "MA",
            "Mozambique" to "MZ",
            "Myanmar" to "MM",
            "Nepal" to "NP",
            "Nigeria" to "NG",
            "Pakistan" to "PK",
            "Peru" to "PE",
            "Philippines" to "PH",
            "Poland" to "PL",
            "Russia" to "RU",
            "Saudi Arabia" to "SA",
            "South Africa" to "ZA",
            "South Korea" to "KR",
            "Spain" to "ES",
            "Sri Lanka" to "LK",
            "Sudan" to "SD",
            "Tanzania" to "TZ",
            "Thailand" to "TH",
            "Turkey" to "TR",
            "Ukraine" to "UA",
            "United Kingdom" to "GB",
            "United States" to "US",
            "Uzbekistan" to "UZ",
            "Venezuela" to "VE",
            "Vietnam" to "VN",
        ).map { (name, code) ->
            Country(name, getFlagEmoji(code), code)
        }.associateBy { it.code }
    }
}