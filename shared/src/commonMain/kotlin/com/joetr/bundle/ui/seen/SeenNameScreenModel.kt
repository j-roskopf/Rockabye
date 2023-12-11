package com.joetr.bundle.ui.seen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.bundle.SeenNames
import com.joetr.bundle.crash.CrashReporting
import com.joetr.bundle.data.ConnectionNotFoundException
import com.joetr.bundle.data.NameRepository
import com.joetr.bundle.data.model.Connection
import com.joetr.bundle.data.model.ConnectionLikedName
import com.joetr.bundle.data.model.Gender
import com.joetr.bundle.ui.seen.data.LocalSeenName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SeenNameScreenModel(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val nameRepository: NameRepository,
    private val crashReporting: CrashReporting,
) : ScreenModel {

    private val _state = MutableStateFlow<SeenNameScreenState>(SeenNameScreenState.Loading)
    val state: StateFlow<SeenNameScreenState> = _state

    private var getDataJob: Job? = null

    var alreadyMarkedAsRead = false

    fun getData(gender: Gender, connectionCode: String? = null) {
        getDataJob?.cancel()
        getDataJob = screenModelScope.launch(coroutineDispatcher) {
            if (connectionCode != null) {
                runCatching {
                    val seenNames = getLocalSeenNames()
                    val connectionFlow = nameRepository.connectionUpdates(connectionCode)
                    Pair(seenNames, connectionFlow)
                }.fold(
                    onSuccess = {
                        val seenNames = it.first
                        val connectionFlow = it.second
                        connectionFlow.collect { connection ->
                            _state.emit(
                                SeenNameScreenState.Content(
                                    gender = gender,
                                    seenNames = seenNames,
                                    connection = connection,
                                    remoteSeenNames = filterRemoteNames(connection),
                                ),
                            )
                        }
                    },
                    onFailure = {
                        if (it is ConnectionNotFoundException) {
                            screenModelScope.launch(coroutineDispatcher) {
                                nameRepository.deleteConnectionLocally()
                                getData(gender, null)
                            }
                        } else {
                            crashReporting.recordException(it)
                            getData(gender, null)
                        }
                    },
                )
            } else {
                val seenNames = getLocalSeenNames()
                _state.emit(
                    SeenNameScreenState.Content(
                        gender = gender,
                        seenNames = seenNames,
                        connection = null,
                        remoteSeenNames = emptyList(),
                    ),
                )
            }
        }
    }

    private fun filterRemoteNames(connection: Connection?): List<List<LocalSeenName>> {
        if (connection == null) return emptyList()

        val boyNames = mutableListOf<LocalSeenName>()
        val girlNames = mutableListOf<LocalSeenName>()
        val allNames = connection.matchedNames
            .sortedWith(
                compareByDescending<ConnectionLikedName> {
                    it.name
                },
            )
            .map {
                LocalSeenName(
                    name = it.name,
                    gender = when (it.genderAbbreviation) {
                        Gender.MALE.abbreviation -> Gender.MALE
                        Gender.FEMALE.abbreviation -> Gender.FEMALE
                        Gender.BOTH.abbreviation -> Gender.BOTH
                        else -> throw IllegalArgumentException("Unknown gender")
                    },
                    // all matches names are liked
                    liked = true,
                )
            }

        allNames.filterTo(boyNames) {
            it.gender == Gender.MALE
        }

        allNames.filterTo(girlNames) {
            it.gender == Gender.FEMALE
        }

        return listOf(boyNames, girlNames)
    }

    private suspend fun getLocalSeenNames(): List<List<LocalSeenName>> {
        val boyNames = mutableListOf<LocalSeenName>()
        val girlNames = mutableListOf<LocalSeenName>()
        val allNames = nameRepository.selectAllNames()
            .sortedWith(
                compareByDescending<SeenNames> {
                    it.liked
                }.thenBy {
                    it.name
                },
            )
            .map {
                LocalSeenName(
                    name = it.name,
                    gender = when (it.gender) {
                        Gender.MALE.abbreviation -> Gender.MALE
                        Gender.FEMALE.abbreviation -> Gender.FEMALE
                        Gender.BOTH.abbreviation -> Gender.BOTH
                        else -> throw IllegalArgumentException("Unknown gender")
                    },
                    liked = it.liked > 0,
                )
            }

        allNames.filterTo(boyNames) {
            it.gender == Gender.MALE
        }

        allNames.filterTo(girlNames) {
            it.gender == Gender.FEMALE
        }

        return listOf(boyNames, girlNames)
    }

    fun reverseLike(seenName: LocalSeenName, connectionCode: String?) {
        screenModelScope.launch(coroutineDispatcher) {
            val newLikedStatus = seenName.liked.not()
            nameRepository.updateLikeStatus(
                newLikeStatus = if (newLikedStatus) {
                    1L
                } else {
                    0L
                },
                seenName.gender.abbreviation,
                seenName.name,
            )
            getData(seenName.gender, connectionCode)
        }
    }

    fun markRemoteAsReadIfNeeded(connectionCode: String?) {
        screenModelScope.launch(coroutineDispatcher) {
            if (alreadyMarkedAsRead.not() && connectionCode != null) {
                alreadyMarkedAsRead = true
                nameRepository.markMatchedAsReadForUser(connectionCode)
            }
        }
    }
}
