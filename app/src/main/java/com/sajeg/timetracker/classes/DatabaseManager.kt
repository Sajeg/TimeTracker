package com.sajeg.timetracker.classes

import android.content.Context
import androidx.room.Room
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.Database
import com.sajeg.timetracker.database.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseManager(context: Context) {
    val db = Room.databaseBuilder(context, Database::class.java, "events").build()
    val dao = db.dao()

    fun getAppEvents(packageName: String, onResponse: (List<EventEntity>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            onResponse(dao.getEventsFromApp(packageName))
        }
    }

    fun addEvent(packageName: String, startTime: Long, endTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.newEvent(EventEntity(0, packageName, startTime, endTime, endTime - startTime))
        }
    }

    fun addEvent(vararg event: EventEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.newEvent(*event)
        }
    }

    fun addApp(packageName: String, displayName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.newApp(AppEntity(packageName, displayName))
        }
    }

    fun updateName(packageName: String, newName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.updateAppName(AppEntity(packageName, newName))
        }
    }
}