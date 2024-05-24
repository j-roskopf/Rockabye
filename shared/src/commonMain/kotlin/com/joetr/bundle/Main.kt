package com.joetr.bundle

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.joetr.bundle.ui.name.NameScreen

@Composable
fun Main() {
    Navigator(
        screen = NameScreen(),
        disposeBehavior = NavigatorDisposeBehavior(),
        onBackPressed = { true },
    ) { navigator ->
        PlatformNavigatorContent(navigator)
    }
}

@Composable
expect fun PlatformNavigatorContent(navigator: Navigator)
