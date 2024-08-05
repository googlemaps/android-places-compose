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
package com.google.android.libraries.places.compose.demo.presentation

/**
 * Represents an event that can be triggered by a ViewModel.
 *
 * Sealed classes are used to represent a closed set of possible types.
 * In this case, the possible types are [UserMessage] and any other types that may be added in the future.
 */
sealed class ViewModelEvent {

    /**
     *  Data class representing a user message event.
     *
     *  @property message The message content.
     */
    data class UserMessage(val message: String) : ViewModelEvent()
}
