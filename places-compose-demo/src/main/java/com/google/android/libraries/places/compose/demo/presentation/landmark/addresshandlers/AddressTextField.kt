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

import androidx.annotation.StringRes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource

@Composable
fun AddressTextField(
    value: String,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable() (() -> Unit)? = null,
    onValueChange: ((String) -> Unit)? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
) {
    OutlinedTextField(
        modifier = modifier.then(
            if (onFocusChanged != null) {
                Modifier.onFocusChanged {
                    onFocusChanged(it.isFocused)
                }
            } else {
                Modifier
            }
        ),
        value = value,
        readOnly = onValueChange == null,
        label = { Text(stringResource(label)) },
        leadingIcon = leadingIcon,
        onValueChange = onValueChange ?: {},
    )
}
