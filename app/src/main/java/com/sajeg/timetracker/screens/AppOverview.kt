package com.sajeg.timetracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.DetailScreen
import com.sajeg.timetracker.R
import com.sajeg.timetracker.ViewData
import com.sajeg.timetracker.classes.AppName
import com.sajeg.timetracker.classes.NameDownloader
import com.sajeg.timetracker.classes.UsageStatsFetcher
import com.sajeg.timetracker.composables.getInstalledVrGames
import com.sajeg.timetracker.composables.millisecondsToTimeString

@Composable
fun AppOverview(navController: NavController) {
    val currentDestination = navController.currentDestination?.route
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
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
        AppGrid(Modifier) {
            navController.navigate(DetailScreen(it))
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AppGrid(modifier: Modifier, onClick: (packageName: String) -> Unit) {
    val context = LocalContext.current
    val usageList = UsageStatsFetcher(context).getUsedApps(0L, System.currentTimeMillis())
    val packageManager = context.packageManager
    val apps = getInstalledVrGames(context)
    var storeNames = remember { mutableStateListOf<AppName?>(null) }
    val packageNames = mutableListOf<String>()
    apps.forEach { app ->
        packageNames.add(app.packageName)
    }
    LaunchedEffect(storeNames) {
        NameDownloader().getAppsName(packageNames) {
            storeNames = it.toMutableStateList()
        }
    }

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 15.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 15.dp),
        columns = GridCells.Adaptive(250.dp),
        verticalArrangement = Arrangement.spacedBy(29.dp, Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(29.dp, Alignment.Start)
    ) {
        items(apps) { app ->
            val playtime =
                usageList.find { it.packageName == app.packageName }?.totalTimeInForeground ?: 0
            val appInfo = packageManager.getApplicationInfo(app.packageName, 0)
            val name = storeNames.find { it?.packageName == app.packageName }?.name ?: packageManager.getApplicationLabel(appInfo).toString()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(160.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable { onClick(app.packageName) }
            ) {
                GlideImage(
                    model = "https://files.cocaine.trade/LauncherIcons/oculus_landscape/${app.packageName}.jpg",
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    failure = placeholder(R.drawable.apps),
                )
                Column {
                    Spacer(Modifier.height(110.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x5BFFFFFF))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(name, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                            Text(millisecondsToTimeString(playtime))
                        }
                    }
                }
            }
        }
    }
}