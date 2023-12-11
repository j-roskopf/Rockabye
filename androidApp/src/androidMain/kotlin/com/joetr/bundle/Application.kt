package com.joetr.bundle

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.joetr.bundle.data.BuildConfig
import com.joetr.bundle.data.BuildConfigImpl
import com.joetr.bundle.data.file.File
import com.joetr.bundle.data.file.FileImpl
import com.joetr.bundle.data.local.DriverFactory
import initCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class Application : Application() {

    private val buildConfigModule = module {
        single<BuildConfig> { BuildConfigImpl(get()) }
    }

    private val sqlDriverModule = module {
        single { DriverFactory(get()).createDriver() }
    }

    private val fileModule = module {
        single<File> { FileImpl() }
    }

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)

        initKoin(
            block = {
                androidContext(this@Application)
            },
            modules = listOf(buildConfigModule, sqlDriverModule, fileModule),
        )
        initCrashlytics()
    }
}
