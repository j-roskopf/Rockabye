plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.joetr.bundle.root")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("co.touchlab.crashkios.crashlyticslink") version libs.versions.crashlytics.get()
    id("app.cash.sqldelight") version libs.versions.sqlDelight.get()
}

kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries {
            framework {
                baseName = "shared"
                isStatic = true
            }
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.voyager.navigator)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.koin)
                implementation(libs.voyager.tabNavigator)
                implementation(libs.koin)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.core)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx)
                implementation(libs.ktor.client.logging)

                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.calendar.compose.datepicker)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.sqldelight.common)

                implementation(libs.compose.swipebox.multiplatform)

                implementation(libs.firebase.firestore)
                implementation(libs.firebase.auth)
                implementation(libs.firebase.crashlytics)
                implementation(libs.crashlytics)
                implementation(libs.firebase.common)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
                implementation(libs.coroutinesTest)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.activity.compose)
                api(libs.appcompat)
                api(libs.core.ktx)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqldelight.driver.android)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
                implementation(libs.coroutinesTest)
                dependsOn(androidMain)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.driver.ios)
            }
        }

        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
            val main by compilations.getting
            binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.Framework> {
                linkerOpts += "-lsqlite3"
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.joetr.bundle.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
    }
    kotlin {
        jvmToolchain(libs.versions.jdk.get().toInt())
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
}

sqldelight {
    databases {
        create("BundleDatabase") {
            packageName.set("com.joetr.bundle")
        }
        linkSqlite.set(true)
    }
}
