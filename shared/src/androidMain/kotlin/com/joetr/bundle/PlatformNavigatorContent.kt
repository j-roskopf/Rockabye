package com.joetr.bundle

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition

@Composable
actual fun PlatformNavigatorContent(navigator: Navigator) {
    FadeTransition(navigator)
}
