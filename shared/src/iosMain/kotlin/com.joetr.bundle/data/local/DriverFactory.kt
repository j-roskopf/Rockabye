package com.joetr.bundle.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.joetr.bundle.BundleDatabase
import com.joetr.bundle.data.NameConstants

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(BundleDatabase.Schema, NameConstants.DATABASE_NAME)
        BundleDatabase.Schema.create(driver)
        return driver
    }
}
