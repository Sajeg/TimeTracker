package com.sajeg.timetracker.screens

import android.util.Log
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.R
import com.sajeg.timetracker.ViewData
import com.sajeg.timetracker.classes.UsageStatsFetcher

@Composable
fun ViewData(navController: NavController) {
    val currentDestination = navController.currentDestination?.route
    Log.d("Navigation", currentDestination.toString())
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
            icon = { Icon(painter = painterResource(R.drawable.apps), contentDescription = "") },
            onClick = { navController.navigate(AppOverview) }
        )
    }
}