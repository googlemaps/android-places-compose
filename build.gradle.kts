// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.power.assert) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.secrets.gradle.plugin) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                "max_line_length" to "120",
                // Renaming files would change JVM facade class names (e.g. ModelsKt) in the published library.
                "ktlint_standard_filename" to "disabled",
                // The demo uses an `in` (India) package for address handlers; renaming it is out of scope.
                "ktlint_standard_package-name" to "disabled",
                // Keep the existing `_foo` backing properties in the demo; renaming them is out of scope.
                "ktlint_standard_backing-property-naming" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}

allprojects {
    group = "com.google.maps.android"
    // {x-release-please-start-version}
    version = "0.3.0"
    // {x-release-please-end}
    val projectArtifactId by extra { project.name }
}