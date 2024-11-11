package com.sajeg.timetracker.classes

import android.content.Context
import androidx.room.Room
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.Database
import com.sajeg.timetracker.database.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.toTypedArray

class DatabaseManager(context: Context) {
    val db = Room.databaseBuilder(context, Database::class.java, "events").build()
    val dao = db.dao()

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
}