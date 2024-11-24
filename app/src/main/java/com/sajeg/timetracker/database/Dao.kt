package com.sajeg.timetracker.database

import android.annotation.SuppressLint
import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface Dao {
    @Query("SELECT * FROM evententity")
    fun getAll(): List<EventEntity>

    @Query("SELECT * FROM evententity WHERE package_name = :packageName")
    fun getEventsFromApp(packageName: String): List<EventEntity>

    @Query("SELECT * FROM appentity")
    fun getAppNames(): List<AppEntity>

    @Query("SELECT * FROM evententity WHERE (start_time > :startTime AND start_time < :endTime) OR (end_time < :endTime AND end_time > :startTime)")
    fun getEvents(startTime: Long, endTime: Long): List<EventEntity>

    @Query("SELECT package_name, SUM(time_diff) AS total_time_diff " +
            "FROM evententity " +
            "WHERE (start_time > :startTime " +
            "AND start_time < :endTime) " +
            "OR (end_time < :endTime " +
            "AND end_time > :startTime) " +
            "GROUP BY package_name")
    fun getPlaytime(startTime: Long, endTime: Long): Cursor

    @Insert
    fun newEvent(vararg entity: EventEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun newApp(entity: AppEntity)

    @Update
    fun updateAppName(entity: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAppNames(vararg entity: AppEntity)

    @Query("DELETE FROM evententity WHERE package_name = :packageName")
    fun deleteEventByName(packageName: String)

    @Query("DELETE FROM EventEntity")
    fun deleteAllEvents()
}