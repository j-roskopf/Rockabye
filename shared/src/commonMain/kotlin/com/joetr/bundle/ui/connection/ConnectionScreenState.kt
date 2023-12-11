package com.joetr.bundle.ui.connection

import com.joetr.bundle.data.model.Connection

sealed interface ConnectionScreenState {
    data object Loading : ConnectionScreenState
    data object Error : ConnectionScreenState
    data class Content(
        val connection: Connection?,
        val lastName: String?,
    ) : ConnectionScreenState
}
