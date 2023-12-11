plugins {
    `kotlin-dsl`
    alias(libs.plugins.spotless)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(libs.versions.ktlint.get())
    }

    kotlinGradle {
        target("*.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("root") {
            id = "com.joetr.bundle.root"
            implementationClass = "com.joetr.bundle.gradle.RootConventionPlugin"
        }

        register("precommit") {
            id = "com.joetr.bundle.precommit"
            implementationClass = "com.joetr.bundle.gradle.PreCommitPlugin"
        }
    }
}
