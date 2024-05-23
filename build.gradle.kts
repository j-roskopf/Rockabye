plugins {
    id("com.joetr.bundle.root")
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    id("com.android.application").version(libs.versions.agp).apply(false)
    id("com.android.library").version(libs.versions.agp).apply(false)
    id("org.jetbrains.compose").version(libs.versions.compose).apply(false)
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("com.google.gms.google-services").version("4.3.14").apply(false)
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        allWarningsAsErrors.set(true)

        if (project.hasProperty("enableComposeCompilerReports")) {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                    layout.buildDirectory.asFile.get().absolutePath + "/compose_metrics",
            )
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    layout.buildDirectory.asFile.get().absolutePath + "/compose_metrics",
            )
        }
    }
}
