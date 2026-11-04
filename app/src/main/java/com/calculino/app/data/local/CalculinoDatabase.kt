package com.calculino.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CalculinoDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        fun build(context: Context): CalculinoDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                CalculinoDatabase::class.java,
                "calculino.db",
            ).build()
    }
}
