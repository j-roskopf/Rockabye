package com.joetr.bundle.ui.connection

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.bundle.crash.CrashReporting
import com.joetr.bundle.data.ConnectionNotFoundException
import com.joetr.bundle.data.NameRepository
import com.joetr.bundle.ui.connection.data.ConnectionStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConnectionScreenModel(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val nameRepository: NameRepository,
    private val crashReporting: CrashReporting,
) : ScreenModel {

    private val _state = MutableStateFlow<ConnectionScreenState>(ConnectionScreenState.Loading)
    val state: StateFlow<ConnectionScreenState> = _state

    private val _action = MutableSharedFlow<ConnectionScreenAction>()
    val action: SharedFlow<ConnectionScreenAction> = _action

    fun init() {
        screenModelScope.launch(coroutineDispatcher) {
            val lastKnownConnectionId = nameRepository.getLastKnownConnectionId()
            val lastName = nameRepository.getLastName()
            if (lastKnownConnectionId != null) {
                connectionUpdates(lastKnownConnectionId, lastName)
            } else {
                _state.value = ConnectionScreenState.Content(
                    connection = null,
                    lastName = lastName,
                )
            }
        }
    }

    fun createConnection(lastName: String) {
        screenModelScope.launch(coroutineDispatcher) {
            _state.value = ConnectionScreenState.Loading
            connectionUpdates(nameRepository.createConnection().id, lastName)
        }
    }

    fun connectWithPartner(connectionCode: String, lastName: String) {
        screenModelScope.launch(coroutineDispatcher) {
            _state.value = ConnectionScreenState.Loading
            when (nameRepository.connectWithPartner(connectionCode)) {
                ConnectionStatus.ConnectionAlreadyHasPartner -> {
                    _action.emit(ConnectionScreenAction.ConnectionAlreadyHasPartner)
                }
                ConnectionStatus.ConnectionCodeDoesNotExist -> {
                    _action.emit(ConnectionScreenAction.ConnectionCodeDoesNotExist)
                }
                ConnectionStatus.Success -> {
                    connectionUpdates(connectionCode, lastName)
                }
            }
        }
    }

    private suspend fun connectionUpdates(connectionCode: String, lastName: String?) {
        runCatching {
            nameRepository.connectionUpdates(
                connectionCode = connectionCode,
            )
        }.fold(
            onSuccess = {
                it.collect { connection ->
                    _state.value = ConnectionScreenState.Content(
                        connection = connection,
                        lastName = lastName,
                    )
                }
            },
            onFailure = {
                if (it is ConnectionNotFoundException) {
                    screenModelScope.launch(coroutineDispatcher) {
                        nameRepository.deleteConnectionLocally()
                        init()
                    }
                } else {
                    crashReporting.recordException(it)
                    _state.value = ConnectionScreenState.Error
                }
            },
        )
    }

    fun deleteConnection() {
        screenModelScope.launch(coroutineDispatcher) {
            runCatching {
                val lastKnownConnection = nameRepository.getLastKnownConnectionId()!!
                nameRepository.deleteConnection(lastKnownConnection)
            }.fold(
                onSuccess = {
                    init()
                },
                onFailure = {
                    crashReporting.recordException(it)
                    _state.value = ConnectionScreenState.Error
                },
            )
        }
    }

    fun disconnectFromPartner() {
        screenModelScope.launch(coroutineDispatcher) {
            _action.emit(ConnectionScreenAction.DeleteConnection)
        }
    }

    fun saveLastName(lastName: String) {
        screenModelScope.launch(coroutineDispatcher) {
            nameRepository.saveLastNameLocally(lastName)
        }
    }
}
