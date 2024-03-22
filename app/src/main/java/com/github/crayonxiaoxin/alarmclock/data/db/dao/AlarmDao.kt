package com.github.crayonxiaoxin.alarmclock.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.crayonxiaoxin.alarmclock.model.Alarm

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarm")
    fun getAll(): List<Alarm>

    @Query("SELECT * FROM alarm")
    fun getAllObx(): LiveData<List<Alarm>>

    @Query("SELECT * FROM alarm WHERE id = :id")
    fun get(id: Int): Alarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg alarm: Alarm): List<Long>

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(vararg alarm: Alarm): Int
}