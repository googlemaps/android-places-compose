// Copyright 2026 Google LLC
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

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Unit tests verifying [CountriesRepository] country catalog indexing and Unicode flag generation.
 */
internal class CountriesRepositoryTest {

    @Test
    fun countries_containsExpectedEntriesWithUnicodeFlags() {
        val countries = CountriesRepository.countries

        assert(countries.isNotEmpty())

        val us = assertNotNull(countries["US"])
        assert(us.name == "United States")
        assert(us.code == "US")
        assert(us.flag == "\uD83C\uDDFA\uD83C\uDDF8")

        val india = assertNotNull(countries["IN"])
        assert(india.name == "India")
        assert(india.code == "IN")
        assert(india.flag == "\uD83C\uDDEE\uD83C\uDDF3")

        val uk = assertNotNull(countries["GB"])
        assert(uk.name == "United Kingdom")
        assert(uk.code == "GB")
        assert(uk.flag == "\uD83C\uDDEC\uD83C\uDDE7")
    }

    @Test
    fun countries_allEntriesHaveTwoCharacterCodeAndRegionalIndicatorFlag() {
        for ((key, country) in CountriesRepository.countries) {
            assert(key == country.code)
            assert(country.code.length == 2)
            assert(country.name.isNotBlank())
            assert(country.flag.codePointCount(0, country.flag.length) == 2)
        }
    }
}
