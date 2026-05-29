package com.redrum.rootedfirmwarelab.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Date,
    val message: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO, WARNING, ERROR, TOOL_OUTPUT, TERMINAL_COMMAND
}
