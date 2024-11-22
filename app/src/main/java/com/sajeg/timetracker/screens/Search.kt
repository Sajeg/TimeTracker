package com.sajeg.timetracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sajeg.timetracker.DetailScreen
import com.sajeg.timetracker.R
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager

@Composable
fun Search(navController: NavController) {
    val currentDestination = navController.currentDestination?.route
    val context = LocalContext.current
    val appList = remember { mutableStateListOf<AppEntity>() }
    val filteredAppList = remember { mutableStateListOf<AppEntity>() }
    val playtimeList = remember { mutableMapOf<String, Long>() }
    LaunchedEffect(Unit) {
        val dbManager = DatabaseManager(context)
        dbManager.getAppNames { names ->
            appList.addAll(names)
            filteredAppList.addAll(names)
            filteredAppList.sortBy { it.displayName }
            dbManager.getPlaytime(0L, System.currentTimeMillis()) { playtime ->
                playtimeList.putAll(playtime)
                dbManager.close()
            }
        }
    }
    Row {
        NavigationRail {
            NavigationRailItem(
                selected = currentDestination == "com.sajeg.timetracker.Search",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = ""
                    )
                },
                onClick = { navController.navigate(com.sajeg.timetracker.Search) }
            )
            NavigationRailItem(
                selected = currentDestination == "com.sajeg.timetracker.AppOverview",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.apps),
                        contentDescription = ""
                    )
                },
                onClick = { navController.navigate(com.sajeg.timetracker.AppOverview) }
            )
            NavigationRailItem(
                selected = currentDestination == "com.sajeg.timetracker.Settings",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = ""
                    )
                },
                onClick = { navController.navigate(com.sajeg.timetracker.Settings) }
            )
        }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .padding(top = 5.dp)
        ) {
            SearchBar { query ->
                val filteredApps = appList.filter { it.displayName.contains(query) }
                filteredAppList.clear()
                filteredAppList.addAll(filteredApps)
            }
            if (appList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.padding(vertical = 15.dp)
                ) {
                    items(filteredAppList) { app ->
                        ListItem(
                            modifier = Modifier,
                            packageName = app.packageName,
                            displayName = app.displayName,
                            icon = app.icon,
                            usage = playtimeList[app.packageName] ?: 0L
                        ) { navController.navigate(DetailScreen(app.packageName)) }
                    }
                }
            } else {
                Text("LOADING...")
            }
        }
    }
}

@Composable
fun SearchBar(onQueryChanged: (query: String) -> Unit) {
    var textInput by remember { mutableStateOf("") }
    Row {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.search),
                            "",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    placeholder = {
                        Text("Search a game or an app...")
                    },
                    value = textInput,
                    onValueChange = { input ->
                        textInput = input
                        onQueryChanged(input)
                    },
                    maxLines = 1,
                    modifier = Modifier
                        .weight(0.1f)
                        .clip(RoundedCornerShape(50.dp)),
//                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
