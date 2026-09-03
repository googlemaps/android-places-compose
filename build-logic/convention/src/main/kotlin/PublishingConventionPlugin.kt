// buildSrc/src/main/kotlin/PublishingConventionPlugin.kt
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {

            applyPlugins()
            configureKover()
            configureDokka()
            configureVanniktechPublishing()
        }
    }

    private fun Project.applyPlugins() {
        apply(plugin = "com.android.library")
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "org.jetbrains.kotlinx.kover")
        apply(plugin = "com.vanniktech.maven.publish")
    }

    private fun Project.configureKover() {
        configure<KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        androidGeneratedClasses()
                    }
                }
            }
        }
    }

    private fun Project.configureDokka() {
        extensions.configure<org.jetbrains.dokka.gradle.DokkaExtension> {
            dokkaSourceSets.configureEach {
                suppress.set(name != "androidJvm")
            }
        }
    }

    private fun Project.configureVanniktechPublishing() {
        extensions.configure<MavenPublishBaseExtension> {
            configure(
                AndroidSingleVariantLibrary(
                    variant = "release",
                    sourcesJar = true,
                    publishJavadocJar = true
                )
            )

            publishToMavenCentral()
            signAllPublications()

            pom {
                name.set(project.name)
                description.set("Jetpack Compose components for the Places SDK for Android")
                url.set("https://github.com/googlemaps/android-places-compose")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:googlemaps/android-places-compose.git")
                    developerConnection.set("scm:git@github.com:googlemaps/android-places-compose.git")
                    url.set("https://github.com/googlemaps/android-places-compose")
                }
                developers {
                    developer {
                        id.set("google")
                        name.set("Google Inc.")
                    }
                }
                organization {
                    name.set("Google Inc")
                    url.set("http://developers.google.com/maps")
                }
            }
        }
    }
}