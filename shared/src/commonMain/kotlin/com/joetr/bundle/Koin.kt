package com.joetr.bundle

import com.joetr.bundle.constants.Dictionary
import com.joetr.bundle.constants.DictionaryImpl
import com.joetr.bundle.coroutineextensions.IoDispatcher
import com.joetr.bundle.coroutineextensions.dispatcherModule
import com.joetr.bundle.crash.CrashReporting
import com.joetr.bundle.crash.CrashReportingImpl
import com.joetr.bundle.data.NameConstants
import com.joetr.bundle.data.NameRepository
import com.joetr.bundle.data.NameRepositoryImpl
import com.joetr.bundle.network.BehindTheNameApi
import com.joetr.bundle.network.NationalizeApi
import com.joetr.bundle.ui.connection.ConnectionScreenModel
import com.joetr.bundle.ui.detail.NameDetailScreenModel
import com.joetr.bundle.ui.filter.FilterScreenModel
import com.joetr.bundle.ui.name.NameScreenModel
import com.joetr.bundle.ui.seen.SeenNameScreenModel
import com.russhwolf.settings.Settings
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule = module {
    factory { Settings() }
    factory { NameScreenModel(get(IoDispatcher), get(), get()) }
    factory { FilterScreenModel(get(IoDispatcher), get()) }
    factory { NameDetailScreenModel(get(IoDispatcher), get(), get()) }
    factory { ConnectionScreenModel(get(IoDispatcher), get(), get()) }
    factory { SeenNameScreenModel(get(IoDispatcher), get(), get()) }
    single<Dictionary> { DictionaryImpl }
    single<CrashReporting> { CrashReportingImpl() }
    single<NameRepository> { NameRepositoryImpl(get(), get(), get(), get(), get(), get()) }
    single { NameConstants(get()) }
    single { BundleDatabase(get()) }
    single { NationalizeApi() }
    single { BehindTheNameApi() }
}

@Suppress("SpreadOperator")
fun initKoin(
    block: KoinApplication.() -> Unit = {
        // no-op by default
    },
    modules: List<Module>,
) = startKoin {
    this.block()
    modules(appModule, dispatcherModule, *modules.toTypedArray())
}
