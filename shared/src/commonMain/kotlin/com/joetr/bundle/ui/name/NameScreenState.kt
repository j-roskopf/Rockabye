package com.joetr.bundle.ui.name

import com.joetr.bundle.ui.data.NameYearData

sealed interface NameScreenState {
    val animationKey: Int

    data object Loading : NameScreenState {
        override val animationKey: Int
            get() = 1
    }

    data class Empty(
        val personStatus: Pair<Boolean, Boolean>,
        val connectionCode: String?,
        override val animationKey: Int = 2,
    ) : NameScreenState
    data object Error : NameScreenState {
        override val animationKey: Int
            get() = 3
    }

    data class Content(
        val data: List<Pair<String, List<NameYearData>>>,
        val personStatus: Pair<Boolean, Boolean>,
        val connectionCode: String?,
        val lastName: String?,
        override val animationKey: Int = 4,
    ) : NameScreenState
}
