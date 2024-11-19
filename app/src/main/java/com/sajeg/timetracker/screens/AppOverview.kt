package com.sajeg.timetracker.screens

import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.DetailScreen
import com.sajeg.timetracker.R
import com.sajeg.timetracker.Settings
import com.sajeg.timetracker.classes.SettingsManager
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager
import com.sajeg.timetracker.millisecondsToTimeString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun AppOverview(navController: NavController) {
    val currentDestination = navController.currentDestination?.route
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        NavigationRail {
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
            NavigationRailItem(
                selected = currentDestination == "com.sajeg.timetracker.Settings",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = ""
                    )
                },
                onClick = { navController.navigate(Settings) }
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
    var sort = remember { mutableIntStateOf(0) }
    var time = remember { mutableIntStateOf(0) }
    var timeOffset = remember { mutableLongStateOf(0) }
    val timeText = remember { mutableStateOf("Today") }
    val sortOptions = listOf("A-Z", "Z-A", "Playtime")
    val timeFrameOptions = listOf("Day", "Week", "Month", "All time")
    val usFormat = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SettingsManager(context).readInt("time_format") {
            usFormat.value = it == 1
            SettingsManager(context).readInt("sort") {
                sort.intValue = it
            }
            SettingsManager(context).readInt("time") {
                time.intValue = it
                calculateDate(
                    time.intValue,
                    timeOffset.longValue,
                    usFormat.value
                ) { startTime, endTime, text ->
                    val dbManager = DatabaseManager(context)
                    dbManager.getPlaytime(startTime, endTime) { events ->
                        playtimeMap.clear()
                        events.forEach {
                            playtimeMap.put(it.key, it.value)
                            Log.d("Playtime", playtimeMap.toString())
                        }
                        dbManager.close()
                        timeText.value = text
                    }
                }
            }
        }
    }

    if (storeNames.isEmpty()) {
        LaunchedEffect(storeNames) {
            val dbManager = DatabaseManager(context)
            dbManager.getAppNames { names ->
                names.forEach { storeNames.add(it) }
            }
            dbManager.close()
        }
    }

    if (sort.intValue == 0) {
        storeNames.sortBy { it.displayName }
    } else if (sort.intValue == 1) {
        storeNames.sortByDescending { it.displayName }
    } else if (sort.intValue == 2) {
        storeNames.sortByDescending { playtimeMap[it.packageName] }
    }

    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .padding(top = 25.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                modifier = Modifier.padding(horizontal = 15.dp),
                onClick = {
                    timeOffset.longValue += 1
                    calculateDate(
                        time.intValue,
                        timeOffset.longValue,
                        usFormat.value
                    ) { startTime, endTime, text ->
                        val dbManager = DatabaseManager(context)
                        dbManager.getPlaytime(startTime, endTime) { events ->
                            playtimeMap.clear()
                            events.forEach {
                                playtimeMap.put(it.key, it.value)
                                Log.d("Playtime", playtimeMap.toString())
                            }
                            dbManager.close()
                            timeText.value = text
                        }
                    }
                }) {
                Icon(
                    painter = painterResource(R.drawable.back),
                    "",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(0.1f),
                text = timeText.value,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall
            )
            if (timeOffset.longValue != 0L) {
                IconButton(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    onClick = {
                        if (timeOffset.longValue > 0L) {
                            timeOffset.longValue -= 1
                        }
                        calculateDate(
                            time.intValue,
                            timeOffset.longValue,
                            usFormat.value
                        ) { startTime, endTime, text ->
                            val dbManager = DatabaseManager(context)
                            dbManager.getPlaytime(startTime, endTime) { events ->
                                playtimeMap.clear()
                                events.forEach {
                                    playtimeMap.put(it.key, it.value)
                                    Log.d("Playtime", playtimeMap.toString())
                                }
                                dbManager.close()
                                timeText.value = text
                            }
                        }
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.forward),
                        "",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .width(77.dp)
                        .padding(horizontal = 15.dp)
                )
            }
        }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SingleChoiceSegmentedButtonRow {
                timeFrameOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = timeFrameOptions.size
                        ),
                        onClick = {
                            SettingsManager(context).saveInt("time", index)
                            time.intValue = index
                            timeOffset.longValue = 0L
                            calculateDate(index, timeOffset.longValue, usFormat.value) { startTime, endTime, text ->
                                val dbManager = DatabaseManager(context)
                                dbManager.getPlaytime(startTime, endTime) { events ->
                                    playtimeMap.clear()
                                    events.forEach {
                                        playtimeMap.put(it.key, it.value)
                                        Log.d("Playtime", playtimeMap.toString())
                                    }
                                    dbManager.close()
                                    timeText.value = text
                                }
                            }
                        },
                        selected = index == time.intValue
                    ) { Text(label) }
                }
            }
            SingleChoiceSegmentedButtonRow {
                sortOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = sortOptions.size
                        ),
                        onClick = {
                            sort.intValue = index
                            SettingsManager(context).saveInt("sort", index)
                        },
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

fun calculateDate(
    selected: Int,
    timeOffset: Long,
    usFormat: Boolean,
    newDate: (startDate: Long, endDate: Long, text: String) -> Unit
) {
    val userZoneOffset = ZonedDateTime.now(ZoneId.systemDefault()).offset
    var startTime = 0L
    var endTime = 0L
    var startDay = LocalDate.now()
    var endDay = LocalDate.now().minusDays(-1)
    var text = ""
    if (selected == 0) {
        startDay = startDay.minusDays(timeOffset)
        endDay = endDay.minusDays(timeOffset)
        text = if (usFormat) {
            "${startDay.monthValue}/${startDay.dayOfMonth}/${
                startDay.year.toString().replace("20", "")
            }"
        } else {
            "${startDay.dayOfMonth}.${startDay.monthValue}.${
                startDay.year.toString().replace("20", "")
            }"
        }
    } else if (selected == 1) {
        startDay = startDay.minusWeeks(timeOffset + 1)
        endDay = endDay.minusWeeks(timeOffset)
        val endDisplayDay = endDay.minusDays(1)
        text = if (usFormat) {
            "${startDay.monthValue}/${startDay.dayOfMonth}/${
                startDay.year.toString().replace("20", "")
            }" +
                    " - ${endDisplayDay.monthValue}/${endDisplayDay.dayOfMonth}/${
                        endDisplayDay.year.toString().replace("20", "")
                    }"
        } else {
            "${startDay.dayOfMonth}.${startDay.monthValue}.${
                startDay.year.toString().replace("20", "")
            }" +
                    " - ${endDisplayDay.dayOfMonth}.${endDisplayDay.monthValue}.${
                        endDisplayDay.year.toString().replace("20", "")
                    }"
        }
    } else if (selected == 2) {
        startDay = startDay.minusMonths(timeOffset + 1)
        endDay = endDay.minusMonths(timeOffset)
        val endDisplayDay = endDay.minusDays(1)
        text = if (usFormat) {
            "${startDay.monthValue}/${startDay.dayOfMonth}/${
                startDay.year.toString().replace("20", "")
            }" +
                    " - ${endDisplayDay.monthValue}/${endDisplayDay.dayOfMonth}/${
                        endDisplayDay.year.toString().replace("20", "")
                    }"
        } else {
            "${startDay.dayOfMonth}.${startDay.monthValue}.${
                startDay.year.toString().replace("20", "")
            }" +
                    " - ${endDisplayDay.dayOfMonth}.${endDisplayDay.monthValue}.${
                        endDisplayDay.year.toString().replace("20", "")
                    }"
        }
    } else if (selected == 3) {
        startDay = startDay.minusYears(20)
        text = "All time"
    }

    startTime = LocalDateTime
        .of(startDay.year, startDay.month.value, startDay.dayOfMonth, 0, 0, 0)
        .toEpochSecond(userZoneOffset)
    endTime = LocalDateTime
        .of(endDay.year, endDay.month.value, endDay.dayOfMonth, 0, 0, 0)
        .toEpochSecond(userZoneOffset)
    startTime = startTime * 1000
    endTime = endTime * 1000
    Log.d("Time", "Start $startTime End $endTime")
    newDate(startTime, endTime, text)
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