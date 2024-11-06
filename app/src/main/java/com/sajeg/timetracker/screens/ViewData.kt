package com.sajeg.timetracker.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.sajeg.timetracker.DetailScreen
import com.sajeg.timetracker.classes.UsageStatsFetcher

@Composable
fun ViewData(navController: NavController) {
    val context = LocalContext.current
    val usageList = UsageStatsFetcher(context).getUsedApps()

    Column {
        usageList.forEach { app ->
            if (app.totalTimeInForeground == 0L) {
                return@forEach
            }
            Card(
                modifier = Modifier.clickable { navController.navigate(DetailScreen(app.packageName)) }
            ) {
                Text(app.packageName)
                Text(app.totalTimeInForeground.toString())
            }
        }
    }
}