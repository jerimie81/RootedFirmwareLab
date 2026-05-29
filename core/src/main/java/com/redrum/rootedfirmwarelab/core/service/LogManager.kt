package com.redrum.rootedfirmwarelab.core.service

import android.content.Context
import com.redrum.rootedfirmwarelab.data.LogDatabase
import com.redrum.rootedfirmwarelab.data.LogEntry
import com.redrum.rootedfirmwarelab.data.LogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class LogManager(private val logDao: com.redrum.rootedfirmwarelab.data.LogDao) {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    fun log(message: String, type: LogType = LogType.INFO) {
        applicationScope.launch {
            logDao.insertLog(LogEntry(timestamp = Date(), message = message, type = type))
        }
    }

    fun getAllLogs() = logDao.getAllLogs()

    fun clearAllLogs() {
        applicationScope.launch {
            logDao.deleteAllLogs()
        }
    }
}
