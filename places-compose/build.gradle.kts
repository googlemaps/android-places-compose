import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("android.places.compose.PublishingConventionPlugin")
}

android {
    lint {
        sarifOutput = layout.buildDirectory.file("reports/lint-results.sarif").get().asFile
    }

    namespace = "com.google.android.libraries.places.compose"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minimumSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        getByName("main") {
            java.directories.add("build/generated/source/artifactId")
            kotlin.directories.add("build/generated/source/artifactId")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

// Artifact ID logic for usage attribution
val attributionId = "gmp_git_androidplacescompose_v$version"

val generateArtifactIdFile = tasks.register("generateArtifactIdFile") {
    val outputDir = layout.buildDirectory.dir("generated/source/artifactId")
    val packageName = "com.google.android.libraries.places.compose.autocomplete.utils.meta"
    val packagePath = packageName.replace('.', '/')
    val outputFile = outputDir.get().file("$packagePath/ArtifactId.kt").asFile

    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package $packageName

            /**
             * Automatically generated object containing the library's attribution ID.
             * This is used to track library usage for analytics.
             */
            public object AttributionId {
                public const val VALUE: String = "$attributionId"
            }
            """.trimIndent()
        )
    }
}

tasks.named("preBuild") {
    dependsOn(generateArtifactIdFile)
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.places)
    implementation(libs.startup.runtime)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.ui.test.android)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.android)

    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)

    testImplementation(libs.google.truth)
    testImplementation(kotlin("test"))
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)

    testImplementation(libs.ui.test.junit4)
    testImplementation(libs.ui.test.manifest)

    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.ui.test.junit4)
}