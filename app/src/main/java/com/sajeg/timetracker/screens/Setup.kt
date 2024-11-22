package com.sajeg.timetracker.screens

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.Setup
import com.sajeg.timetracker.classes.NameDownloader
import com.sajeg.timetracker.classes.UsageStatsFetcher
import com.sajeg.timetracker.composables.getInstalledVrGames
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Setup(navController: NavController) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        val usageAccessLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
                navController.navigate(Setup)
            }
        val intent = Intent("android.settings.USAGE_ACCESS_SETTINGS")
        intent.setPackage("com.android.settings")

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 3600

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        if (usageStatsList.isEmpty()) {
            LaunchedEffect(Unit) {
                usageAccessLauncher.launch(intent)
            }
        } else {
            ScanEvents(navController)
        }
    }
}

@Composable
fun ScanEvents(navController: NavController) {
    val context = LocalContext.current
    val scanning = remember { mutableStateOf(true) }
    val apps = getInstalledVrGames(context)
    val packageNames = mutableListOf<String>()
    apps.forEach { app ->
        packageNames.add(app.packageName)
    }

    LaunchedEffect(scanning) {
        NameDownloader().getStoreName(context, packageNames) { storeNames ->
            val dbManager = DatabaseManager(context)
            for (app in packageNames) {
                if (storeNames.find { it.packageName == app } == null) {
                    val packageManager = context.packageManager
                    val appInfo = packageManager.getApplicationInfo(app, 0)
                    val name = packageManager.getApplicationLabel(appInfo).toString()
                    dbManager.addAppNames(listOf(AppEntity(app, name)))
                }
            }
        }
        UsageStatsFetcher(context).updateDatabase {
            scanning.value = false
            CoroutineScope(Dispatchers.Main).launch {
                navController.navigate(AppOverview)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (scanning.value) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text("Processing your playtime...", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}