// ktlint-disable filename

import androidx.compose.material3.Surface
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import com.joetr.bundle.Main
import com.joetr.bundle.data.BuildConfig
import com.joetr.bundle.data.BuildConfigImpl
import com.joetr.bundle.data.file.File
import com.joetr.bundle.data.file.FileImpl
import com.joetr.bundle.data.local.DriverFactory
import com.joetr.bundle.design.theme.AppTheme
import com.joetr.bundle.initKoin
import org.koin.dsl.module

@Suppress("Unused", "FunctionName")
fun MainViewController() = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    },
) {
    initCrashlyticsApple()
    initKoin(modules = listOf(buildConfigModule, sqlDriverModule, fileModule))
    AppTheme {
        Surface {
            Main()
        }
    }
}

private fun initCrashlyticsApple() {
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}

private val buildConfigModule = module {
    single<BuildConfig> { BuildConfigImpl() }
}

private val sqlDriverModule = module {
    single { DriverFactory().createDriver() }
}

private val fileModule = module {
    single<File> { FileImpl() }
}
