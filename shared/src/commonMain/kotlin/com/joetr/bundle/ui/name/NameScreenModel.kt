package com.joetr.bundle.ui.name

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.joetr.bundle.crash.CrashReporting
import com.joetr.bundle.data.ConnectionNotFoundException
import com.joetr.bundle.data.NameRepository
import com.joetr.bundle.data.model.Connection
import com.joetr.bundle.data.model.Gender
import com.joetr.bundle.ui.data.NameYearData
import com.joetr.bundle.ui.name.data.NameSort
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class NameScreenModel(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val nameRepository: NameRepository,
    private val crashReporting: CrashReporting,
) : ScreenModel {

    private val _state = MutableStateFlow<NameScreenState>(NameScreenState.Loading)
    val state: StateFlow<NameScreenState> = _state

    private var updatesJob: Job? = null
    private var cacheJob: Job? = null

    var names: MutableList<Pair<String, List<NameYearData>>>? = null

    init {
        screenModelScope.launch(coroutineDispatcher) {
            nameRepository.signInAnonymouslyIfNeeded()
        }
    }

    fun startCacheCollection() {
        cacheJob?.cancel()
        cacheJob = screenModelScope.launch(coroutineDispatcher) {
            nameRepository.clearCache().collect {
                if (it) {
                    clearCache()
                    readData()
                }
            }
        }
    }

    fun readData(useCacheIfAvailable: Boolean = true) {
        _state.value = NameScreenState.Loading
        updatesJob?.cancel()
        updatesJob = screenModelScope.launch(coroutineDispatcher) {
            runCatching {
                val connectionFlow = getConnectionFlow()
                val filteredListOfNames = if (names != null && useCacheIfAvailable) {
                    names!!
                } else {
                    getFilteredListOfNames()
                }
                combine(
                    flowOf(filteredListOfNames),
                    connectionFlow,
                    flowOf(nameRepository.getLastName()),
                ) { names: List<Pair<String, List<NameYearData>>>, connection: Connection?, lastName: String? ->
                    Triple(names, connection, lastName)
                }
            }.fold(
                onSuccess = {
                    it.collect { data ->
                        val connection = data.second
                        names = data.first.toMutableList()
                        val lastName = data.third

                        val userId = nameRepository.getUserId()

                        // if the current user is person 1 / 2 and needs to be alerted
                        val personStatus = Pair(
                            userId == connection?.personOne?.id && connection.matchedNames.any { connectionLikedName ->
                                connectionLikedName.personOneAlerted.not()
                            },
                            userId == connection?.personTwo?.id && connection.matchedNames.any { connectionLikedName ->
                                connectionLikedName.personTwoAlerted.not()
                            },
                        )

                        if (names?.isEmpty() == true) {
                            _state.emit(
                                NameScreenState.Empty(
                                    personStatus = personStatus,
                                    connectionCode = connection?.id,
                                ),
                            )
                        } else {
                            _state.emit(
                                NameScreenState.Content(
                                    data = names!!,
                                    personStatus = personStatus,
                                    connectionCode = connection?.id,
                                    lastName = lastName,
                                ),
                            )
                        }
                    }
                },
                onFailure = {
                    if (it is ConnectionNotFoundException) {
                        screenModelScope.launch(coroutineDispatcher) {
                            nameRepository.deleteConnectionLocally()
                            readData()
                        }
                    } else {
                        crashReporting.recordException(it)
                        _state.value = NameScreenState.Error
                    }
                },
            )
        }
    }

    private suspend fun getFilteredListOfNames(): List<Pair<String, List<NameYearData>>> {
        val gender = nameRepository.getGenderOrDefault()
        val maxLength = nameRepository.getMaxLengthOrDefault()
        val startsWith = nameRepository.getStartsWithOrDefault()
        val timePeriodFilters = nameRepository.getTimePeriodOrDefault()
        val names =
            nameRepository.readFileInChunks(timePeriodFilters.range, gender, startsWith, maxLength)
        val set = processNames(
            names = names,
            startsWith = startsWith,
            maxLength = maxLength,
            genderFilter = gender,
        )
        val seenNames = nameRepository.selectAllNames().map {
            it.name
        }

        val sorting = nameRepository.getSorting()

        val filteredList = set.filter {
            seenNames.contains(it.key).not()
        }.toList()

        return when (sorting) {
            NameSort.RANDOM -> {
                filteredList.shuffled()
            }

            NameSort.POPULAR -> {
                filteredList.sortedByDescending {
                    (it.second[0].popularity)
                }
            }
        }
    }

    private suspend fun getConnectionFlow(): Flow<Connection?> {
        val lastKnownConnectionId = nameRepository.getLastKnownConnectionId()
        return if (lastKnownConnectionId == null) {
            return flowOf(null)
        } else {
            nameRepository.connectionUpdates(lastKnownConnectionId)
        }
    }

    fun nameRemoved(name: String, gender: String, liked: Boolean) {
        screenModelScope.launch(coroutineDispatcher) {
            nameRepository.insertName(name, gender, liked)

            // remove from cache
            if (names?.isNotEmpty() == true) {
                names?.removeFirst()
            }
        }
    }

    private fun clearCache() {
        names = null
    }

    private fun processNames(
        names: List<String>,
        startsWith: String,
        maxLength: Int,
        genderFilter: Gender,
    ): Map<String, List<NameYearData>> {
        val set = mutableMapOf<String, MutableList<NameYearData>>()
        names.forEach { line ->
            // Process each line (e.g., split by comma and handle data)
            val parts = line.split(',')
            if (parts.size == 5) {
                val name = parts[0]
                val gender = parts[1]
                val popularity = parts[2]
                val sum = parts[3]
                val year = parts[4]

                if (name.startsWith(startsWith, ignoreCase = true).not()) return@forEach
                if (name.length > maxLength) return@forEach

                if (genderFilter.abbreviation == Gender.BOTH.abbreviation || gender == genderFilter.abbreviation) {
                    // Handle the data as needed
                    val entry = set.getOrPut(name) {
                        mutableListOf()
                    }
                    val popularityLong = popularity.toLong()
                    val nameYearData = NameYearData(
                        year = year.toInt(),
                        popularity = popularityLong,
                        popularityTotal = sum.toLong(),
                        genderAbbreviation = gender,
                    )
                    if (entry.isEmpty()) {
                        entry.add(
                            nameYearData,
                        )
                    } else {
                        val index = entry.indexOfFirst {
                            it.popularity < popularityLong
                        }
                        if (index == -1) {
                            entry.add(nameYearData)
                        } else {
                            entry.add(index, nameYearData)
                        }
                    }
                }
            }
        }

        return set
    }

    fun setSorting(sorting: NameSort) {
        screenModelScope.launch(coroutineDispatcher) {
            nameRepository.saveSortingLocally(sorting)
            readData(
                useCacheIfAvailable = false,
            )
        }
    }
}
