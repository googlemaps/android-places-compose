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
package com.google.android.libraries.places.compose.demo.di

import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.compose.autocomplete.repositories.AutocompleteRepository
import com.google.android.libraries.places.compose.demo.PlacesComposeDemoApplication
import com.google.android.libraries.places.compose.demo.data.repositories.AddressValidationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.ApiKeyProvider
import com.google.android.libraries.places.compose.demo.data.repositories.CountriesRepository
import com.google.android.libraries.places.compose.demo.data.repositories.GeocoderRepository
import com.google.android.libraries.places.compose.demo.data.repositories.LocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.MergedLocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.MockLocationRepository
import com.google.android.libraries.places.compose.demo.data.repositories.PlaceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Singleton

/** AppModule provides dependencies for the application. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePlacesClient(
        application: Application,
        apiKeyProvider: ApiKeyProvider
    ): PlacesClient {
        Places.initializeWithNewPlacesApiEnabled(
            application.applicationContext,
            apiKeyProvider.placesApiKey
        )

        return Places.createClient(application.applicationContext)
    }

    @Provides
    @Singleton
    fun provideAutocompleteRepository(placesClient: PlacesClient): AutocompleteRepository {
        return AutocompleteRepository(placesClient)
    }

    @Provides
    @Singleton
    fun providesPlaceRepository(placesClient: PlacesClient): PlaceRepository {
        return PlaceRepository(placesClient)
    }

    @Provides
    @Singleton
    fun provideGeocoderRepository(
        application: Application,
        apiKeyProvider: ApiKeyProvider
    ): GeocoderRepository {
        return GeocoderRepository(application.applicationContext, apiKeyProvider)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(application: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(application)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Provides
    @Singleton
    fun provideLocationRepository(application: Application): LocationRepository {
        return LocationRepository(
            application.applicationContext,
            (application as PlacesComposeDemoApplication).applicationScope
        )
    }

    @Provides
    @Singleton
    fun provideApiKeyProvider(application: Application): ApiKeyProvider {
        return ApiKeyProvider(application.applicationContext)
    }

    @Provides
    @Singleton
    fun provideRequestQueue(application: Application): RequestQueue {
        return Volley.newRequestQueue(application.applicationContext)
    }

    @Provides
    @Singleton
    fun provideAddressValidationRepository(
        apiKeyProvider: ApiKeyProvider,
        requestQueue: RequestQueue
    ): AddressValidationRepository {
        return AddressValidationRepository(
            apiKeyProvider = apiKeyProvider,
            requestQueue = requestQueue
        )
    }

    @Provides
    @Singleton
    fun provideCountriesRepository(): CountriesRepository {
        return CountriesRepository()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Provides
    @Singleton
    fun provideMockLocationRepository(application: Application): MockLocationRepository {
        return MockLocationRepository(
            (application as PlacesComposeDemoApplication).applicationScope
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Provides
    @Singleton
    fun provideApplication(
        application: Application,
        locationRepository: LocationRepository,
        mockLocationRepository: MockLocationRepository
    ): MergedLocationRepository {
        return MergedLocationRepository(
            scope = (application as PlacesComposeDemoApplication).applicationScope,
            locationRepository = locationRepository,
            mockLocationRepository = mockLocationRepository,
        )
    }
}
