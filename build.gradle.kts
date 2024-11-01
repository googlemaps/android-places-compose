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
}

allprojects {
    group = "com.google.maps.android"
    version = "0.1.1"
    val projectArtifactId by extra { project.name }
}