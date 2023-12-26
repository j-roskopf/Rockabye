package com.joetr.bundle.ui.name

import com.joetr.bundle.ui.data.NameYearData

sealed interface NameScreenState {

    data object Loading : NameScreenState

    data class Empty(
        val personStatus: Pair<Boolean, Boolean>,
        val connectionCode: String?,
    ) : NameScreenState

    data object Error : NameScreenState

    data class Content(
        val data: List<Pair<String, List<NameYearData>>>,
        val personStatus: Pair<Boolean, Boolean>,
        val connectionCode: String?,
        val lastName: String?,
    ) : NameScreenState
}
