import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import tddy.ko.cardstack.build_logic.extension.configureComposeExtension
import tddy.ko.cardstack.build_logic.extension.library
import tddy.ko.cardstack.build_logic.extension.libraryExtension
import tddy.ko.cardstack.build_logic.extension.libs
import tddy.ko.cardstack.build_logic.extension.plugin

class ComposeLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(libs.plugin("android-library").pluginId)
                apply(libs.plugin("compose-compiler").pluginId)
            }

            dependencies {
                val bom = libs.library("androidx-compose-bom")
                add("implementation", platform(bom))
                add("implementation", libs.library("androidx-compose-ui"))
                add("implementation", libs.library("androidx-compose-ui-graphics"))
                add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
                add("implementation", libs.library("androidx-compose-material3"))
                add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
            }

            configureComposeExtension(commonExtension = libraryExtension)
        }
    }
}