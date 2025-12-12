// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(libs.jacoco.android.plugin)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.secrets.gradle.plugin) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp)
    id("org.jetbrains.dokka") version "2.1.0"
}

allprojects {
    group = "com.google.maps.android"
    // {x-release-please-start-version}
    version = "0.3.0"
    // {x-release-please-end}
    val projectArtifactId by extra { project.name }
}