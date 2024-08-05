package com.google.android.libraries.places.compose.demo.domain.usecases

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.compose.autocomplete.repositories.AutocompleteRepository
import javax.inject.Inject

class GetAutocompletionPredictionsUseCase
@Inject
constructor(
    private val autocompleteRepository: AutocompleteRepository
) {
    suspend operator fun invoke(
        query: String,
        actions: FindAutocompletePredictionsRequest.Builder.() -> Unit = {}
    ): List<AutocompletePrediction> {
        return autocompleteRepository.getAutocompletePlaces(
            FindAutocompletePredictionsRequest.builder().apply(
                actions
            ).setQuery(query).build()
        )
    }
}