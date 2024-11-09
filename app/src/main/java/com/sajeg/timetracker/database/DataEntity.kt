package com.sajeg.timetracker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppEntity (
    @PrimaryKey val packageName: String,
    @ColumnInfo(name = "display_name") val displayName: String
)

@Entity
data class EventEntity (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    @ColumnInfo(name = "time_diff") val timeDiff: Long
)