/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.places.compose.autocomplete.utils.attribution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.compose.autocomplete.utils.meta.AttributionId
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AttributionIdInitializerTest {
    @Before
    fun setUp() {
        mockkStatic(Places::class)
        every { Places.addInternalUsageAttributionId(any()) } just runs
    }

    @After
    fun tearDown() {
        unmockkStatic(Places::class)
    }

    @Test
    fun create_addsInternalUsageAttributionIdToPlaces() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val initializer = AttributionIdInitializer()

        initializer.create(context)

        verify {
            Places.addInternalUsageAttributionId(AttributionId.VALUE)
        }
    }

    @Test
    fun dependencies_returnsEmptyList() {
        val initializer = AttributionIdInitializer()
        assertThat(initializer.dependencies()).isEmpty()
    }
}
