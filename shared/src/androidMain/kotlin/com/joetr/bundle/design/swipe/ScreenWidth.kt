package com.joetr.bundle.design.swipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun screenWidth(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp
}
