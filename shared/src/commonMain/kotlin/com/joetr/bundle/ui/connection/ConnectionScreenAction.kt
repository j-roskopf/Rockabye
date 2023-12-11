package com.joetr.bundle.ui.connection

sealed interface ConnectionScreenAction {
    data object DeleteConnection : ConnectionScreenAction
    data object ConnectionAlreadyHasPartner : ConnectionScreenAction
    data object ConnectionCodeDoesNotExist : ConnectionScreenAction
}
