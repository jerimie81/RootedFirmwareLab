package com.redrum.rootedfirmwarelab.test

import android.content.Context
import androidx.room.Room
import com.redrum.rootedfirmwarelab.data.LogDatabase

object TestDatabaseHelper {
    fun createInMemoryDatabase(context: Context): LogDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            LogDatabase::class.java
        ).allowMainThreadQueries().build()
    }
}
