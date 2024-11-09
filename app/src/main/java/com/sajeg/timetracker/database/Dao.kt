package com.sajeg.timetracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface Dao {
    @Query("SELECT * FROM evententity")
    fun getAll(): List<EventEntity>

    @Query("SELECT * FROM evententity WHERE package_name = :packageName")
    fun getEventsFromApp(packageName: String): List<EventEntity>

    @Insert
    fun newEvent(vararg entity: EventEntity)

    @Insert
    fun newApp(entity: AppEntity)

    @Update
    fun updateAppName(entity: AppEntity)

    @Query("SELECT * FROM appentity")
    fun getAppNames(): List<AppEntity>

    @Insert
    fun addAppNames(vararg entity: AppEntity)
}