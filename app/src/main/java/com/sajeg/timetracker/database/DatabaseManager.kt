package com.sajeg.timetracker.database

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.toTypedArray

class DatabaseManager(context: Context) {
    private val migration1to2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE AppEntity ADD COLUMN landscape_image TEXT")
            db.execSQL("ALTER TABLE AppEntity ADD COLUMN icon TEXT")
        }
    }
    private val db = Room.databaseBuilder(context, Database::class.java, "events")
        .addMigrations(migration1to2)
        .build()
    private val dao = db.dao()

    fun getAppEvents(packageName: String, onResponse: (List<EventEntity>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            onResponse(dao.getEventsFromApp(packageName))
        }
    }

    fun getAppNames(onResponse: (List<AppEntity>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            onResponse(dao.getAppNames())
        }
    }

    fun addEvent(vararg event: EventEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.newEvent(*event)
        }
    }

    fun addAppNames(names: List<AppEntity>) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.addAppNames(*names.toTypedArray())
        }
    }

    fun getEvents(startTime: Long, endTime: Long, onResponse: (List<EventEntity>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            onResponse(dao.getEvents(startTime, endTime))
        }
    }

    fun getPlaytime(startTime: Long, endTime: Long, onResponse: (HashMap<String, Long>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val cursor = dao.getPlaytime(startTime, endTime)
            onResponse(cursor.toHashMap())
        }
    }

    fun deleteAppData(packageName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteEventByName(packageName)
        }
    }

    fun deleteAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteAllEvents()
        }
    }

    fun close() {
        db.close()
    }

    @SuppressLint("Range")
    fun Cursor.toHashMap(): HashMap<String, Long> {
        val hashMap = HashMap<String, Long>()
        if (this.moveToFirst()) {
            do {
                val packageName = this.getString(this.getColumnIndex("package_name"))
                val totalTimeDiff = this.getLong(this.getColumnIndex("total_time_diff"))
                hashMap[packageName] = totalTimeDiff
            } while (this.moveToNext())
        }
        return hashMap
    }
}