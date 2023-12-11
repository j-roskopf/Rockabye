package com.joetr.bundle.data

import kotlin.experimental.ExperimentalNativeApi

actual class BuildConfigImpl() : BuildConfig {

    @OptIn(ExperimentalNativeApi::class)
    override fun isDebug(): Boolean {
        return Platform.isDebugBinary
    }
}
