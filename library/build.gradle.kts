import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "com.google.android.libraries.places.compose.library"
  compileSdk {
    version = release(libs.versions.compileSdk.get().toInt())
  }

  defaultConfig {
    minSdk = libs.versions.minimumSdk.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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

}

dependencies {
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)
  implementation(libs.material)
  implementation(libs.play.services.maps)
  implementation(libs.startup.runtime)
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  testImplementation(libs.mockk)
  testImplementation(libs.core.ktx)
}

abstract class GenerateArtifactIdTask : DefaultTask() {
  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @get:Input
  abstract val version: Property<String>

  @TaskAction
  fun generate() {
    val dir = outputDir.get().asFile
    val packageName = "com.google.android.libraries.places.compose.library.utils.meta"
    val packagePath = packageName.replace('.', '/')
    val outputFile = File(dir, "$packagePath/ArtifactId.kt")
    outputFile.parentFile.mkdirs()
    val attributionId = "gmp_git_androidplacescompose_v${version.get()}"
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

val generateArtifactIdFile = tasks.register<GenerateArtifactIdTask>("generateArtifactIdFile") {
  outputDir.set(layout.buildDirectory.dir("generated/source/artifactId"))
  version.set(project.version.toString())
}

androidComponents {
  onVariants { variant ->
    variant.sources.java?.addGeneratedSourceDirectory(
      generateArtifactIdFile,
      GenerateArtifactIdTask::outputDir
    )
  }
}
