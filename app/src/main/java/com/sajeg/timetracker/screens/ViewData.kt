package com.sajeg.timetracker.screens

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun ViewData(navController: NavController) {
    val context = LocalContext.current
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 1000 * 3600 * 24 * 4  // last 24 hours

    val usageStatsList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY, startTime, endTime
    )

    Log.d("AppUsage", usageStatsList.toString())

    usageStatsList?.forEach { usageStats ->
        Log.d("AppUsage", "Package: ${usageStats.packageName}, Time in foreground: ${usageStats.totalTimeInForeground}")
    }
}