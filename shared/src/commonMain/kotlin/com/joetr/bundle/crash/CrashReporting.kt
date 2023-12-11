package com.joetr.bundle.crash

interface CrashReporting {
    fun recordException(
        throwable: Throwable,
    )
}
