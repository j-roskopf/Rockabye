package com.joetr.bundle.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.joetr.bundle.BundleDatabase
import com.joetr.bundle.data.NameConstants.Companion.DATABASE_NAME

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val driver = AndroidSqliteDriver(BundleDatabase.Schema, context, DATABASE_NAME)
        BundleDatabase.Schema.create(driver)
        return driver
    }
}
