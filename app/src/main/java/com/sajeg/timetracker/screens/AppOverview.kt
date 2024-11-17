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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.bumptech.glide.integration.compose.GlidePreloadingData
import com.bumptech.glide.integration.compose.placeholder
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.DetailScreen
import com.sajeg.timetracker.R
import com.sajeg.timetracker.ViewData
import com.sajeg.timetracker.composables.millisecondsToTimeString
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager

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
    var storeNames = remember { mutableStateListOf<AppEntity>() }
    val playtimeMap = remember { mutableStateMapOf<String, Long>() }
    val state = rememberLazyGridState()

    if (storeNames.isEmpty()) {
        LaunchedEffect(storeNames) {
            val dbManager = DatabaseManager(context)
            dbManager.getAppNames { names ->
                names.forEach { storeNames.add(it) }
            }
            dbManager.close()
        }
    }
    if (playtimeMap.isEmpty()) {
        LaunchedEffect(playtimeMap) {
            val dbManager = DatabaseManager(context)
            dbManager.getPlaytime(0L, System.currentTimeMillis()) { events ->
                events.forEach {
                    playtimeMap.put(it.key, it.value)
                }
            }
            dbManager.close()
        }
    }
    storeNames.sortBy { it.displayName }
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 15.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 15.dp),
        state = state,
        columns = GridCells.Adaptive(250.dp),
        verticalArrangement = Arrangement.spacedBy(29.dp, Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(29.dp, Alignment.Start)
    ) {
        items(storeNames) { app ->
            val playtime = playtimeMap[app.packageName] ?: 0L
            val name = storeNames.find { it.packageName == app.packageName }?.displayName ?: ""
            AppCard(onClick, app, name, playtime)
        }
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
private fun AppCard(
    onClick: (String) -> Unit,
    app: AppEntity,
    name: String,
    playtime: Long
) {
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
                    .background(Color(0xC9FFFFFF))
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