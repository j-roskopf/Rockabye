package com.joetr.bundle.util

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()
