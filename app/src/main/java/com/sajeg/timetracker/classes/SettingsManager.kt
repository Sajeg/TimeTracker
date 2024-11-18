package com.sajeg.timetracker.classes

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.sajeg.timetracker.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsManager(val context: Context) {

    fun saveLong(id: String, value: Long) {
        val idPreferenceKey = longPreferencesKey(id)
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[idPreferenceKey] = value
            }
        }
    }

    fun readLong(id: String, onResponse: (value: Long) -> Unit) {
        val idPreferenceKey = longPreferencesKey(id)
        CoroutineScope(Dispatchers.IO).launch {
            val preferences = context.dataStore.data.first()
            val data = preferences[idPreferenceKey]
            onResponse(data?.toLong() ?: 0L)
        }
    }

    fun saveInt(id: String, value: Int) {
        val idPreferenceKey = intPreferencesKey(id)
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[idPreferenceKey] = value
            }
        }
    }

    fun readInt(id: String, onResponse: (value: Int) -> Unit) {
        val idPreferenceKey = intPreferencesKey(id)
        CoroutineScope(Dispatchers.IO).launch {
            val preferences = context.dataStore.data.first()
            val data = preferences[idPreferenceKey]
            onResponse(data?.toInt() ?: 0)
        }
    }
}