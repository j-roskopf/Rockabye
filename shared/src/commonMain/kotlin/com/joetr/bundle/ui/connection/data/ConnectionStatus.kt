package com.joetr.bundle.ui.connection.data

sealed interface ConnectionStatus {
    data object ConnectionCodeDoesNotExist : ConnectionStatus
    data object ConnectionAlreadyHasPartner : ConnectionStatus
    data object Success : ConnectionStatus
}
