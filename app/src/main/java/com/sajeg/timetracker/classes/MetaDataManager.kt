package com.sajeg.timetracker.classes

import android.content.Context
import android.util.JsonReader
import android.util.Log
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStreamReader
import java.net.URL

class MetaDataManager {
    private val baseUrl = "https://raw.githubusercontent.com/threethan/MetaMetadata/refs/heads/main/data/common/"

    fun updateData(context: Context, packages: List<String>) {
        val appMetaDataList = mutableListOf<AppEntity>()
        CoroutineScope(Dispatchers.IO).launch {
            for (packageName in packages) {
                val url = URL("$baseUrl$packageName.json")
                var displayName: String? = null
                var landscapeImage: String? = null
                var icon: String? = null

                try {
                    val inputStream = url.openStream()
                    JsonReader(InputStreamReader(inputStream)).use { reader ->
                        if (reader.hasNext()) {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "name" -> displayName = reader.nextString()
                                    "landscape" -> landscapeImage = reader.nextString()
                                    "icon" -> icon = reader.nextString()
                                    "versioncode" -> reader.nextInt()
                                    else -> reader.nextString()
                                }
                            }
                            reader.close()
                        }
                    }
                } catch (e: Exception) {
                    if (displayName == null) {
                        val packageManager = context.packageManager
                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        displayName = packageManager.getApplicationLabel(appInfo).toString()
                    }
                } finally {
                    appMetaDataList.add(
                        AppEntity(
                            packageName = packageName,
                            displayName = displayName ?: "Error fetching name",
                            landscapeImage = landscapeImage,
                            icon = icon
                        )
                    )
                }
            }
            val dbManager = DatabaseManager(context)
            dbManager.addAppNames(appMetaDataList)
            dbManager.close()
        }
    }
}