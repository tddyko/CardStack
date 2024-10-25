import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.layout.buildDirectory)
    }
    withType(KotlinCompile::class) {
        compilerOptions.allWarningsAsErrors = true
    }
}