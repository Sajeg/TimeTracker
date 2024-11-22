package com.sajeg.timetracker.classes

import android.content.Context
import android.util.JsonReader
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.net.URL

class MetaDataManager {
    private val baseUrl =
        "https://raw.githubusercontent.com/threethan/MetaMetadata/refs/heads/main/data/common/"
    private val oldBaseUrl = "https://files.cocaine.trade/LauncherIcons/"
    private val listUrl = "https://files.cocaine.trade/LauncherIcons/oculus_apps.json"

    fun updateData(context: Context, packages: List<String>, onDone: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            SettingsManager(context).readInt("new_meta_data") { newData ->
                if (newData == 1) {
                    newDataSource(packages, context)
                } else {
                    oldDataSource(packages, context)
                }
                onDone()
            }
        }
    }

    private fun newDataSource(
        packageNames: List<String>,
        context: Context,
    ) {
        val appMetaDataList = mutableListOf<AppEntity>()
        for (packageName in packageNames) {
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

        saveToDB(context, appMetaDataList, packageNames)
    }

    private fun oldDataSource(packageNames: List<String>, context: Context) {
        val appMetaDataList = mutableListOf<AppEntity>()
        val inputStream = URL(listUrl).openStream()
        JsonReader(InputStreamReader(inputStream)).use { reader ->
            reader.beginArray()
            while (reader.hasNext()) {
                reader.beginObject()
                var name: String? = null
                var packageName: String?
                if (reader.nextName() == "appName") {
                    name = reader.nextString()
                }
                if (reader.nextName() == "packageName") {
                    packageName = reader.nextString()
                    if (packageNames.contains(packageName) && name != null) {
                        appMetaDataList.add(
                            AppEntity(
                                packageName,
                                name,
                                "${oldBaseUrl}oculus_landscape/${packageName}.jpg",
                                "${oldBaseUrl}oculus_icon/${packageName}.jpg"
                            )
                        )
                    }
                }
                reader.nextName()
                reader.nextString()
                reader.endObject()
            }
            reader.endArray()
        }
        saveToDB(context, appMetaDataList, packageNames)
    }

    private fun saveToDB(context: Context, metadata: List<AppEntity>, packages: List<String>) {
        val allAppsWithMetaData = metadata.toMutableList()
        for (app in packages) {
            if (metadata.find { it.packageName == app } == null) {
                val packageManager = context.packageManager
                val appInfo = packageManager.getApplicationInfo(app, 0)
                val name = packageManager.getApplicationLabel(appInfo).toString()
                allAppsWithMetaData.add(AppEntity(
                    packageName = app,
                    displayName = name,
                    landscapeImage = null,
                    icon = null
                ))
            }
        }

        val dbManager = DatabaseManager(context)
        dbManager.addAppNames(allAppsWithMetaData)
        dbManager.close()
    }
}