package com.github.crayonxiaoxin.alarmclock.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.crayonxiaoxin.alarmclock.data.db.dao.AlarmDao
import com.github.crayonxiaoxin.alarmclock.model.Alarm
import java.util.concurrent.Executors

@Database(
    entities = [Alarm::class],
    version = 1,
    exportSchema = true,
    autoMigrations = [],
)
@TypeConverters(Converters::class)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "hera_db"
                )
                    .setQueryCallback(object : QueryCallback {
                        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
//                        _e("SQL", sqlQuery)
//                        _e("SQL Args", bindArgs.toString())
                        }
                    }, Executors.newSingleThreadExecutor())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}