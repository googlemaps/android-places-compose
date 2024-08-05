# Android Places Compose Library

## Description

This is a Jetpack Compose library for the Google Maps Platform Places SDK for Android. It provides a
reusable Places Autocomplete Compose widget.  

Additionally, there is a sample app showing how to use the widget as well as demonstrating 
[Address Descriptors](https://developers.google.com/maps/documentation/geocoding/address-descriptors/requests-address-descriptors)
and address entry.

## Requirements

* Android API 24 (or newer) (Android 7.0 Nougat)  
* Android Studio (Koala or newer recommended)

## Installation

Maven artifacts are coming soon...

Add the dependency below to your module's `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.google.maps.android:places-compose:0.1.0")
}
```

## Sample App

This repository includes a full-featured demo app that demonstrates how to use the library. To run the demo app, follow these steps:

1. Clone the repository
2. [Get a Places API key][api-key]
3. Copy `local.defaults.properties` to `secrets.properties` and replace `DEFAULT_API_KEY` with your API key(s). (Note: this file should *NOT* be
   under version control to protect your API key)
4. Build and run

## Usage

### Place Autocomplete

The `PlacesAutocompleteTextField` composable provides a text field for place autocomplete. As the
user types, the composable will show a list of 
[Places Autocomplete suggestions](https://developers.google.com/maps/documentation/places/android-sdk/place-autocomplete)
that the user can select from.

See PlacesAutocompleteMinimalActivity for a very minimal working example.

```Kotlin
class PlacesAutocompleteMinimalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(this, BuildConfig.PLACES_API_KEY)
        val placesClient = Places.createClient(this)

        val bias: LocationBias = RectangularBounds.newInstance(
            LatLng(39.9, -105.5), // SW lat, lng
            LatLng(40.1, -105.0) // NE lat, lng
        )

        setContent {
            val searchTextFlow = MutableStateFlow("")
            val searchText by searchTextFlow.collectAsState()
            var predictions by remember { mutableStateOf(emptyList<AutocompletePrediction>()) }

            LaunchedEffect(Unit) {
                searchTextFlow.debounce(500.milliseconds).collect { query : String ->
                    val response = placesClient.awaitFindAutocompletePredictions {
                        locationBias = bias
                        typesFilter = listOf(PlaceTypes.ESTABLISHMENT)
                        this.query = query
                        countries = listOf("US")
                    }
                    predictions = response.autocompletePredictions
                }
            }

            AndroidPlacesComposeDemoTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Places Autocomplete") }
                        )
                    }
                ) { paddingValues: PaddingValues ->
                    PlacesAutocompleteTextField(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        searchText = searchText,
                        predictions = predictions.map { it.toPlaceDetails() },
                        onQueryChanged = { searchTextFlow.value = it },
                        onSelected = { autocompletePlace : AutocompletePlace ->
                            // Handle the selected place
                            Toast.makeText(
                                this@PlacesAutocompleteMinimalActivity,
                                "Selected: ${autocompletePlace.primaryText}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    )
                }
            }
        }
    }
}
```

> **_NOTE:_**  This sample is for demonstration purposes.  Please follow Android best practices when
> building production ready apps. 

### Landmark Selection

The demo code shows how to use [address descriptors](https://developers.google.com/maps/documentation/geocoding/address-descriptors/requests-address-descriptors)
to assist in locating an address.  Note that address descriptors are not available in all locations.

## Contributing

Contributions are welcome and encouraged\! See [contributing] for more info.

## Terms of Service

This library uses Google Maps Platform services. Use of Google Maps Platform services through this library is subject to the Google Maps Platform [Terms of Service](https://cloud.google.com/maps-platform/terms).

This library is not a Google Maps Platform Core Service. Therefore, the Google Maps Platform Terms of Service (e.g. Technical Support Services, Service Level Agreements, and Deprecation Policy) do not apply to the code in this library.

## Support

This library is offered via an open source license. It is not governed by the Google Maps Platform Support [Technical Support Services Guidelines](https://cloud.google.com/maps-platform/terms/tssg), the [SLA](https://cloud.google.com/maps-platform/terms/sla), or the [Deprecation Policy](https://cloud.google.com/maps-platform/terms) (however, any Google Maps Platform services used by the library remain subject to the Google Maps Platform Terms of Service).

This library adheres to [semantic versioning](https://semver.org/) to indicate when backwards-incompatible changes are introduced. Accordingly, while the library is in version 0.x, backwards-incompatible changes may be introduced at any time.

If you find a bug or have a feature request, please [file an issue].
Or, if you'd like to contribute, send us a [pull request] and refer to our [code of conduct] and [contributing] guide.

You can also reach us on our [Discord channel].

[api-key]: https://developers.google.com/places/android-sdk/get-api-key
[Discord channel]: https://discord.gg/hYsWbmk
[code of conduct]: CODE_OF_CONDUCT.md
[file an issue]: https://github.com/googlemaps/android-places-compose/issues/new/choose
[code of conduct]: CODE_OF_CONDUCT.md
[contributing]: CONTRIBUTING.md
