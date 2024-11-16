package com.sajeg.timetracker.classes

import android.content.Context
import android.util.JsonReader
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.URL

data class AppName(val packageName: String, var name: String) {
    override fun toString(): String {
        return "$packageName,$name"
    }
}


class NameDownloader {
    val listURL = URL("https://files.cocaine.trade/LauncherIcons/oculus_apps.json")

    fun getStoreName(
        context: Context,
        packageNames: List<String>,
        onFinished: (List<AppEntity>) -> Unit
    ) {
        val dbManager = DatabaseManager(context)
        dbManager.getAppNames() { names ->
            updateStoreNames(packageNames) { newNames ->
                dbManager.addAppNames(newNames)
                dbManager.close()
                onFinished(newNames)
            }
        }
    }

    fun updateStoreNames(packageNames: List<String>, onFinished: (List<AppEntity>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val customNames = mutableListOf<AppEntity>()
            val inputStream = withContext(Dispatchers.IO) {
                listURL.openStream()
            }
            JsonReader(InputStreamReader(inputStream)).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    reader.beginObject()
                    var name: String? = null
                    var packageName: String? = null
                    if (reader.nextName() == "appName") {
                        name = reader.nextString()
                    }
                    if (reader.nextName() == "packageName") {
                        packageName = reader.nextString()
                        if (packageNames.contains(packageName) && name != null) {
                            customNames.add(
                                AppEntity(packageName, name)
                            )
                        }
                    }
                    reader.nextName()
                    reader.nextString()
                    reader.endObject()
                }
                reader.endArray()
            }
            onFinished(customNames)
        }
    }
}