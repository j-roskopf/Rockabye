package com.joetr.bundle.data

import com.joetr.bundle.SeenNames
import com.joetr.bundle.data.model.Connection
import com.joetr.bundle.data.model.Gender
import com.joetr.bundle.ui.connection.data.ConnectionStatus
import com.joetr.bundle.ui.data.TimePeriodFilters
import com.joetr.bundle.ui.name.data.NameSort
import kotlinx.coroutines.flow.Flow

interface NameRepository {
    suspend fun signInAnonymouslyIfNeeded()

    suspend fun selectAllNames(): List<SeenNames>
    suspend fun insertName(name: String, genderAbbreviation: String, liked: Boolean)
    suspend fun getGenderOrDefault(): Gender
    suspend fun getMaxLengthOrDefault(): Int
    suspend fun getStartsWithOrDefault(): String
    suspend fun getTimePeriodOrDefault(): TimePeriodFilters

    fun setGender(gender: Gender)
    fun setMaxLength(maxLength: Int)
    fun setStartsWith(startsWith: String)
    fun setTimePeriod(timePeriodFilters: TimePeriodFilters)
    fun setYear(year: Int)

    suspend fun readFileInChunks(range: IntRange, gender: Gender, startsWith: String, maxLength: Int): List<String>

    suspend fun createConnection(): Connection

    suspend fun connectWithPartner(connectionCode: String): ConnectionStatus

    suspend fun getConnection(connectionCode: String): Connection?

    suspend fun updateConnection(connection: Connection)

    suspend fun deleteConnection(connectionCode: String)

    fun getLastKnownConnectionId(): String?

    fun getUserId(): String

    suspend fun connectionUpdates(connectionCode: String): Flow<Connection?>

    suspend fun clearCache(): Flow<Boolean>

    suspend fun emitClearCacheSignal()

    suspend fun updateLikeStatus(
        newLikeStatus: Long,
        genderAbbreviation: String,
        name: String,
    )

    fun deleteConnectionLocally()

    suspend fun markMatchedAsReadForUser(connectionCode: String)

    suspend fun saveLastNameLocally(lastName: String)
    fun getLastName(): String?

    suspend fun saveSortingLocally(sorting: NameSort)
    suspend fun getSorting(): NameSort
}
