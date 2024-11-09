package com.sajeg.timetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppEntity::class, EventEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun dao(): Dao
}