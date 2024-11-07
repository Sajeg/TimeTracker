package com.sajeg.timetracker.screens

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.sajeg.timetracker.ViewData

@Composable
fun Setup(navController: NavController) {
    val context = LocalContext.current
    val intent = Intent("android.settings.USAGE_ACCESS_SETTINGS")
    intent.setPackage("com.android.settings")

    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 1000 * 3600

    val usageStatsList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY, startTime, endTime
    )
    if (usageStatsList.isEmpty()) {
        context.startActivity(intent, null)
        navController.navigate(ViewData)
    } else {
        navController.navigate(ViewData)
    }
}