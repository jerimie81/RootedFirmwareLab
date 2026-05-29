package com.redrum.rootedfirmwarelab.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntry>>

    @Insert
    suspend fun insertLog(log: LogEntry)

    @Query("DELETE FROM logs")
    suspend fun deleteAllLogs()
}
