package com.sajeg.timetracker.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.DetailScreen
import com.sajeg.timetracker.R
import com.sajeg.timetracker.ViewData
import com.sajeg.timetracker.classes.UsageStatsFetcher

@Composable
fun AppOverview(navController: NavController) {
    val context = LocalContext.current
    val usageList = UsageStatsFetcher(context).getUsedApps()
    val currentDestination = navController.currentDestination?.route
    Log.d("Navigation", currentDestination.toString())
    Row {

        NavigationRail {
            NavigationRailItem(
                selected = currentDestination == "com.sajeg.timetracker.ViewData",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.hourglass),
                        contentDescription = ""
                    )
                },
                onClick = { navController.navigate(ViewData) }
            )
            NavigationRailItem(
                selected = currentDestination == "com.sajeg.timetracker.AppOverview",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.apps),
                        contentDescription = ""
                    )
                },
                onClick = { navController.navigate(AppOverview) }
            )
        }

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
}