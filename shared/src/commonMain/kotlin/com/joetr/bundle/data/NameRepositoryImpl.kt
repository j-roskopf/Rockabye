package com.joetr.bundle.data

import com.joetr.bundle.BundleDatabase
import com.joetr.bundle.SeenNames
import com.joetr.bundle.constants.Dictionary
import com.joetr.bundle.crash.CrashReporting
import com.joetr.bundle.data.file.File
import com.joetr.bundle.data.model.Connection
import com.joetr.bundle.data.model.ConnectionLikedName
import com.joetr.bundle.data.model.ConnectionPerson
import com.joetr.bundle.data.model.Gender
import com.joetr.bundle.ui.connection.data.ConnectionStatus
import com.joetr.bundle.ui.data.TimePeriodFilters
import com.joetr.bundle.ui.filter.textFileRange
import com.joetr.bundle.ui.name.data.NameSort
import com.joetr.bundle.util.randomUUID
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock

private const val DEFAULT_TIMEOUT_MILLIS = 10_000L
internal val textFileRange = 1880..2022
private val defaultGender = Gender.MALE
private const val GENDER_KEY = "gender"
private const val YEAR_KEY = "year"
private const val LAST_NAME_KEY = "last_name"
private const val STARTS_WITH_KEY = "starts_with"
private const val TIME_PERIOD_KEY = "time_period"
private const val MAX_LENGTH_KEY = "max_length"
private const val USER_ID_KEY = "user_id"
private const val SORTING_KEY = "sorting"
private const val LAST_KNOWN_CONNECTION_CODE = "last_known_connection_code"

class NameRepositoryImpl(
    private val settings: Settings,
    private val database: BundleDatabase,
    private val file: File,
    private val crashReporting: CrashReporting,
    private val nameConstants: NameConstants,
    private val dictionary: Dictionary,
) : NameRepository {

    private val firestore = Firebase.firestore

    private val _cacheClear = MutableSharedFlow<Boolean>()
    val cacheClear: SharedFlow<Boolean> = _cacheClear

    init {
        firestore.setSettings(
            persistenceEnabled = true,
        )
    }

    private val auth = Firebase.auth

    override suspend fun signInAnonymouslyIfNeeded() {
        if (auth.currentUser == null) {
            val user = auth.signInAnonymously().user
            if (user == null) {
                crashReporting.recordException(
                    Throwable("Error - after signing in anonymously, there was no user present"),
                )
            }
        }
    }

    override suspend fun selectAllNames(): List<SeenNames> {
        return database.seenNamesQueries.SelectAll().executeAsList()
    }

    override suspend fun insertName(name: String, genderAbbreviation: String, liked: Boolean) {
        // insert into DB
        database.seenNamesQueries.InsertName(name, genderAbbreviation, if (liked) 1 else 0)

        // insert remotely
        val lastKnownConnectionCode = getLastKnownConnectionId()
        if (lastKnownConnectionCode != null && liked) {
            val connection = getConnection(lastKnownConnectionCode)
            if (connection != null) {
                val userId = getUserId()
                val isPersonOne = connection.personOne.id == userId
                val isPersonTwo = connection.personTwo?.id == userId
                val copy = if (isPersonOne) {
                    updateIntersections(
                        connection.copy(
                            personOneLikedNames = connection.personOneLikedNames + ConnectionLikedName(
                                name = name,
                                genderAbbreviation = genderAbbreviation,
                                personOneAlerted = false,
                                personTwoAlerted = false,
                            ),
                        ),
                    )
                } else if (isPersonTwo) {
                    updateIntersections(
                        connection.copy(
                            personTwoLikedNames = connection.personTwoLikedNames + ConnectionLikedName(
                                name = name,
                                genderAbbreviation = genderAbbreviation,
                                personOneAlerted = false,
                                personTwoAlerted = false,
                            ),
                        ),
                    )
                } else {
                    null
                }

                if (copy != null) {
                    // update remote
                    updateConnection(copy)
                }
            }
        }
    }

    private fun updateIntersections(connection: Connection): Connection {
        val intersections =
            connection.personTwoLikedNames
                .intersect(connection.personOneLikedNames.toSet())

        val personOneLikedNames = connection.personOneLikedNames - intersections
        val personTwoLikedNames = connection.personTwoLikedNames - intersections

        return connection.copy(
            personTwoLikedNames = personTwoLikedNames,
            personOneLikedNames = personOneLikedNames,
            matchedNames = connection.matchedNames + intersections.toList(),
        )
    }

    override suspend fun getGenderOrDefault(): Gender {
        return settings.getGenderOrDefault()
    }

    override suspend fun getMaxLengthOrDefault(): Int {
        return settings.getMaxLengthOrDefault()
    }

    override suspend fun getStartsWithOrDefault(): String {
        return settings.getStartsWithOrDefault()
    }

    override suspend fun getTimePeriodOrDefault(): TimePeriodFilters {
        return settings.getTimePeriodOrDefault()
    }

    override fun setGender(gender: Gender) {
        settings[GENDER_KEY] = gender.abbreviation
    }

    override fun setMaxLength(maxLength: Int) {
        settings[MAX_LENGTH_KEY] = maxLength
    }

    override fun setStartsWith(startsWith: String) {
        settings[STARTS_WITH_KEY] = startsWith
    }

    override fun setTimePeriod(timePeriodFilters: TimePeriodFilters) {
        settings[TIME_PERIOD_KEY] = timePeriodFilters::class.simpleName
    }

    override fun setYear(year: Int) {
        val filter = TimePeriodFilters.SpecificYear(
            range = year..year,
            display = year.toString(),
        )
        settings[TIME_PERIOD_KEY] = filter::class.simpleName
        settings[YEAR_KEY] = year
    }

    override suspend fun readFileInChunks(
        range: IntRange,
        gender: Gender,
        startsWith: String,
        maxLength: Int,
    ): List<String> {
        return file.readFileInChunks(
            range = range,
            gender = gender,
            startsWith = startsWith,
            maxLength = maxLength,
        )
    }

    override suspend fun createConnection(): Connection {
        val userId = getUserId()

        var connectionId = createConnectionId()
        if (connectionCodeExists(connectionId)) {
            while (connectionCodeExists(connectionId)) {
                connectionId = createConnectionId()
            }
        }

        val userLikedNames = selectAllNames().filter {
            it.liked > 0
        }
        val connection = Connection(
            id = connectionId,
            personOne = ConnectionPerson(id = userId),
            personTwo = null,
            personOneLikedNames = userLikedNames.map {
                ConnectionLikedName(
                    name = it.name,
                    genderAbbreviation = it.gender,
                    personOneAlerted = false,
                    personTwoAlerted = false,
                )
            },
            personTwoLikedNames = emptyList(),
            matchedNames = emptyList(),
        )

        // save connection locally
        settings[LAST_KNOWN_CONNECTION_CODE] = connectionId

        updateConnection(connection)

        return connection
    }

    override suspend fun connectWithPartner(
        connectionCode: String,
    ): ConnectionStatus {
        // check if connection exists
        return if (connectionCodeExists(connectionCode)) {
            val connection: Connection =
                firestore.collection(nameConstants.nameCollection()).document(connectionCode).get()
                    .data()
            if (connection.personTwo != null) {
                ConnectionStatus.ConnectionAlreadyHasPartner
            } else {
                val personTwo = ConnectionPerson(getUserId())

                val personTwoLikedNames = selectAllNames().filter {
                    it.liked > 0
                }.map {
                    ConnectionLikedName(
                        name = it.name,
                        genderAbbreviation = it.gender,
                        personOneAlerted = false,
                        personTwoAlerted = false,
                    )
                }

                val intersectionConnection = updateIntersections(
                    connection.copy(
                        personTwo = personTwo,
                        personTwoLikedNames = personTwoLikedNames,
                    ),
                )

                updateConnection(
                    intersectionConnection,
                )

                // save locally
                settings[LAST_KNOWN_CONNECTION_CODE] = connection.id

                ConnectionStatus.Success
            }
        } else {
            ConnectionStatus.ConnectionCodeDoesNotExist
        }
    }

    override suspend fun getConnection(connectionCode: String): Connection? {
        return if (connectionCodeExists(connectionCode)) {
            firestore.collection(nameConstants.nameCollection()).document(connectionCode)
                .get()
                .data()
        } else {
            null
        }
    }

    override suspend fun updateConnection(connection: Connection) {
        firestore.collection(nameConstants.nameCollection()).document(connection.id).set(connection)
    }

    override suspend fun deleteConnection(connectionCode: String) {
        settings[LAST_KNOWN_CONNECTION_CODE] = null
        firestore.collection(nameConstants.nameCollection()).document(connectionCode).delete()
    }

    private suspend fun connectionCodeExists(connectionCode: String): Boolean {
        return runCatching {
            withTimeout(
                DEFAULT_TIMEOUT_MILLIS,
            ) {
                val roomCollection = firestore.collection(nameConstants.nameCollection()).get()
                roomCollection.documents.any {
                    it.id == connectionCode
                }
            }
        }.fold(
            onSuccess = {
                it
            },
            onFailure = {
                crashReporting.recordException(it)
                false
            },
        )
    }

    override fun getLastKnownConnectionId(): String? {
        return settings[LAST_KNOWN_CONNECTION_CODE]
    }

    override suspend fun connectionUpdates(connectionCode: String): Flow<Connection?> {
        return if (connectionCodeExists(connectionCode)) {
            firestore
                .collection(nameConstants.nameCollection())
                .document(connectionCode)
                .snapshots
                .map {
                    if (it.exists) {
                        it.data() as Connection
                    } else {
                        null
                    }
                }
        } else {
            throw ConnectionNotFoundException()
        }
    }

    override suspend fun clearCache(): Flow<Boolean> {
        return cacheClear
    }

    override suspend fun emitClearCacheSignal() {
        _cacheClear.emit(true)
    }

    override suspend fun updateLikeStatus(
        newLikeStatus: Long,
        genderAbbreviation: String,
        name: String,
    ) {
        // update locally
        database.seenNamesQueries.UpdateLikeStatus(
            newLikeStatus,
            genderAbbreviation,
            name,
        )

        val lastKnownConnectionCode = getLastKnownConnectionId()
        if (lastKnownConnectionCode != null && connectionCodeExists(lastKnownConnectionCode)) {
            getConnection(lastKnownConnectionCode)?.let { connection ->
                val userId = getUserId()
                val copy = if (userId == connection.personTwo?.id) {
                    val index = connection.personTwoLikedNames.indexOfFirst {
                        it.name == name && genderAbbreviation == it.genderAbbreviation
                    }
                    if (index == -1) {
                        // they didn't previously like it, add it
                        connection.copy(
                            personTwoLikedNames = connection.personTwoLikedNames + ConnectionLikedName(
                                name = name,
                                genderAbbreviation = genderAbbreviation,
                                personOneAlerted = false,
                                personTwoAlerted = false,
                            ),
                        )
                    } else {
                        // they did previously like it, remove it
                        val mutableList = connection.personTwoLikedNames.toMutableList()
                        mutableList.removeAt(index)
                        connection.copy(
                            personTwoLikedNames = mutableList,
                        )
                    }
                } else if (userId == connection.personOne.id) {
                    val index = connection.personOneLikedNames.indexOfFirst {
                        it.name == name && genderAbbreviation == it.genderAbbreviation
                    }
                    if (index == -1) {
                        // they didn't previously like it, add it
                        connection.copy(
                            personOneLikedNames = connection.personOneLikedNames + ConnectionLikedName(
                                name = name,
                                genderAbbreviation = genderAbbreviation,
                                personOneAlerted = false,
                                personTwoAlerted = false,
                            ),
                        )
                    } else {
                        // they did previously like it, remove it
                        val mutableList = connection.personOneLikedNames.toMutableList()
                        mutableList.removeAt(index)
                        connection.copy(
                            personOneLikedNames = mutableList,
                        )
                    }
                } else {
                    null
                }

                if (copy != null) {
                    // update remote
                    updateConnection(updateIntersections(copy))
                }
            }
        }
    }

    override fun deleteConnectionLocally() {
        settings[LAST_KNOWN_CONNECTION_CODE] = null
    }

    override fun getUserId(): String {
        val userIdFromSettings: String? = settings[USER_ID_KEY]
        val userId = if (userIdFromSettings == null) {
            val randomId = randomUUID()
            settings[USER_ID_KEY] = randomId
            randomId
        } else {
            userIdFromSettings
        }
        return userId
    }

    private fun createConnectionId(): String {
        val numberOfWords = 1
        return dictionary.numberOfRandomWords(numberOfWords).first().plus(
            Clock.System.now().toEpochMilliseconds().toString().takeLast(
                NameConstants.MAX_RANDOM_NUMBERS,
            ),
        )
    }

    override suspend fun markMatchedAsReadForUser(connectionCode: String) {
        val connection = getConnection(connectionCode)
        if (connection != null) {
            val userId = getUserId()
            val copy = if (connection.personOne.id == userId) {
                connection.copy(
                    matchedNames = connection.matchedNames.map {
                        it.copy(
                            personOneAlerted = true,
                        )
                    },
                )
            } else if (connection.personTwo?.id == userId) {
                connection.copy(
                    matchedNames = connection.matchedNames.map {
                        it.copy(
                            personTwoAlerted = true,
                        )
                    },
                )
            } else {
                null
            }

            if (copy != null) {
                updateConnection(copy)
            }
        }
    }

    override suspend fun saveLastNameLocally(lastName: String) {
        settings[LAST_NAME_KEY] = lastName
    }

    override fun getLastName(): String? {
        return settings.getLastName()
    }

    override suspend fun saveSortingLocally(sorting: NameSort) {
        settings[SORTING_KEY] = sorting.key
    }

    override suspend fun getSorting(): NameSort {
        return settings.getSortingOrDefault()
    }
}

internal fun Settings.getTimePeriodOrDefault(): TimePeriodFilters {
    val timePeriodSetting = this.getStringOrNull(TIME_PERIOD_KEY)
    val yearSetting = this.getYearOrDefault()
    return when (timePeriodSetting) {
        null -> TimePeriodFilters.Default()
        TimePeriodFilters.Default::class.simpleName -> TimePeriodFilters.Default()
        TimePeriodFilters.EighteenHundreds::class.simpleName -> TimePeriodFilters.EighteenHundreds()
        TimePeriodFilters.PreviousMillennium::class.simpleName -> TimePeriodFilters.PreviousMillennium()
        TimePeriodFilters.CurrentMillennium::class.simpleName -> TimePeriodFilters.CurrentMillennium()
        TimePeriodFilters.SpecificYear::class.simpleName -> TimePeriodFilters.SpecificYear(
            range = yearSetting..yearSetting,
            display = yearSetting.toString(),
        )

        else -> TimePeriodFilters.Default()
    }
}

fun Settings.getGenderOrDefault(): Gender {
    val genderSetting = this.getStringOrNull(GENDER_KEY)

    return Gender.entries.firstOrNull {
        it.abbreviation == genderSetting
    } ?: defaultGender
}

fun Settings.getYearOrDefault(): Int {
    val yearSetting = this.getIntOrNull(YEAR_KEY)
    return yearSetting ?: textFileRange.last
}

fun Settings.getMaxLengthOrDefault(): Int {
    val maxLengthSetting = this.getIntOrNull(MAX_LENGTH_KEY)
    return maxLengthSetting ?: Int.MAX_VALUE
}

fun Settings.getStartsWithOrDefault(): String {
    val startsWith = this.getStringOrNull(STARTS_WITH_KEY)
    return startsWith ?: ""
}

fun Settings.getLastName(): String? {
    return getStringOrNull(LAST_NAME_KEY)
}

fun Settings.getSortingOrDefault(): NameSort {
    val sorting = this.getIntOrNull(SORTING_KEY)
    return NameSort.entries.firstOrNull {
        it.key == sorting
    } ?: NameSort.POPULAR
}
