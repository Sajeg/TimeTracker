package com.sajeg.timetracker.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.R
import com.sajeg.timetracker.ViewData

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
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {

    }
}

//@Composable
//fun LeftPart() {
//
//}
//
//@Composable
//fun RightPart() {
//
//}