package com.joetr.bundle.data

private const val DEBUG_NAME_COLLECTION = "ConnectionsTesting"
private const val NAME_COLLECTION = "Connections"

class NameConstants(private val buildConfig: BuildConfig) {

    companion object {
        const val MAX_RANDOM_NUMBERS = 5

        const val DATABASE_NAME = "bundle.db"
    }

    fun nameCollection(): String {
        return if (buildConfig.isDebug()) {
            DEBUG_NAME_COLLECTION
        } else {
            NAME_COLLECTION
        }
    }
}

interface BuildConfig {
    fun isDebug(): Boolean
}

expect class BuildConfigImpl : BuildConfig
