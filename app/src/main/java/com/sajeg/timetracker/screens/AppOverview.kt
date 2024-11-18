package com.sajeg.timetracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.sajeg.timetracker.DetailScreen
import com.sajeg.timetracker.R
import com.sajeg.timetracker.composables.millisecondsToTimeString
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager

@Composable
fun AppOverview(navController: NavController) {
    val currentDestination = navController.currentDestination?.route
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
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
    var sort = remember { mutableIntStateOf(0) }
    var time = remember { mutableIntStateOf(0) }
    val sortOptions = listOf("A-Z", "Z-A", "Playtime")
    val timeFrameOptions = listOf("Day", "Week", "Month", "All time")

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
    Column {
        Row (
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .padding(top = 25.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            SingleChoiceSegmentedButtonRow {
                timeFrameOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = timeFrameOptions.size
                        ),
                        onClick = { time.intValue = index },
                        selected = index == time.intValue
                    ) { Text(label) }
                }
            }
            IconButton(modifier = Modifier.padding(horizontal = 15.dp), onClick = {}) {
                Icon(painter = painterResource(R.drawable.back), "", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                modifier = modifier.fillMaxWidth().weight(0.1f),
                text = "17. November",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall
            )
            IconButton(modifier = Modifier.padding(horizontal = 15.dp), onClick = {}) {
                Icon(painter = painterResource(R.drawable.forward), "", tint = MaterialTheme.colorScheme.onBackground)
            }
            SingleChoiceSegmentedButtonRow {
                sortOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = sortOptions.size
                        ),
                        onClick = { sort.intValue = index },
                        selected = index == sort.intValue
                    ) { Text(label) }
                }
            }
        }
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
                    Text(name.trim(), style = MaterialTheme.typography.titleLarge, maxLines = 1)
                    Text(millisecondsToTimeString(playtime))
                }
            }
        }
    }
}