package com.sajeg.timetracker.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.sajeg.timetracker.classes.PieChartPlottingData
import com.sajeg.timetracker.composables.PieChart
import com.sajeg.timetracker.millisecondsToTimeString
import com.sajeg.timetracker.database.AppEntity
import com.sajeg.timetracker.database.DatabaseManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun ViewData(navController: NavController) {
    val currentDestination = navController.currentDestination?.route
    Log.d("Navigation", currentDestination.toString())

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        NavigationRail(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
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
        LeftPart(
            Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
        )
        RightPart(
            Modifier
                .fillMaxHeight()
        ) {
            navController.navigate(DetailScreen(it))
        }
    }
}

@Composable
fun LeftPart(modifier: Modifier) {
    val context = LocalContext.current
    val dbManager = DatabaseManager(context)
    val userZoneOffset = ZonedDateTime.now(ZoneId.systemDefault()).offset
    val today = LocalDate.now()
    val startTime =
        LocalDateTime.of(today.year, today.month.value, today.dayOfMonth, 0, 0, 0)
            .toEpochSecond(userZoneOffset)
    val usage = remember { mutableStateListOf<PieChartPlottingData>() }
    val appNames = remember { mutableStateListOf<AppEntity>() }
    if (usage.isEmpty() && appNames.isEmpty()) {
        LaunchedEffect(usage) {
            dbManager.getAppNames { names ->
                dbManager.getPlaytime(startTime * 1000, System.currentTimeMillis()) { playtime ->
                    usage.clear()
                    playtime.forEach { app ->
                        val name = names.find { it.packageName == app.key }?.displayName ?: ""
                        usage.add(PieChartPlottingData(name, app.value))
                    }
                }
            }
        }
    }
    Column(
        modifier = modifier
    ) {
        PieChart(
            Modifier.fillMaxHeight(0.5f),
            *usage.toTypedArray()
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 40.dp, bottom = 40.dp)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("I don't know what to put here")
                Text("Seriously?")
                Text("Yeah \uD83D\uDE44")
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun RightPart(modifier: Modifier, onClick: (String) -> Unit) {
    val usageAllTime = remember { mutableMapOf<String?, Long>() }
    val usageWeek = remember { mutableMapOf<String?, Long>() }
    val appNames = remember { mutableStateListOf<AppEntity>() }
    val context = LocalContext.current
    val packageManager = context.packageManager

    if (usageWeek.isEmpty() && usageAllTime.isEmpty() && appNames.isEmpty()) {
        LaunchedEffect(usageAllTime) {
            val dbManager = DatabaseManager(context)
            dbManager.getPlaytime(0L, System.currentTimeMillis()) { events ->
                usageAllTime.clear()
                events.forEach { event ->
                    usageAllTime.put(event.key, event.value)
                }
                Log.d("UsageAllTime", usageAllTime.toString())
                dbManager.close()
            }
        }
        LaunchedEffect(usageWeek) {
            val dbManager = DatabaseManager(context)
            val userZoneOffset = ZonedDateTime.now(ZoneId.systemDefault()).offset
            val lastWeek = LocalDate.now().minusWeeks(1)
            val startTime =
                LocalDateTime.of(lastWeek.year, lastWeek.month, lastWeek.dayOfMonth, 0, 0, 0)
                    .toEpochSecond(userZoneOffset)
            dbManager.getPlaytime(startTime * 1000, System.currentTimeMillis()) { events ->
                usageWeek.clear()
                events.forEach { event ->
                    usageWeek.put(event.key, event.value)
                }
                Log.d("UsageAllWeek", usageAllTime.toString())
                dbManager.close()
            }
        }
        LaunchedEffect(appNames) {
            val dbManager = DatabaseManager(context)
            dbManager.getAppNames { apps ->
                appNames.clear()
                apps.forEach { app ->
                    appNames.add(app)
                }
                Log.d("AppNames", usageAllTime.toString())
                dbManager.close()
            }
        }
    } else {
        Column(
            modifier = modifier
        ) {
            if (!usageWeek.isEmpty()) {
                Text(
                    "Top usage this week:",
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.5f)
                ) {
                    itemsIndexed(usageWeek.toList()
                        .sortedByDescending { it.second }) { index, (packageName, timeDiff) ->
                        if (packageName == null) {
                            return@itemsIndexed
                        }
                        val name = appNames.find { it.packageName == packageName }?.displayName
                            ?: ""
                        ListItem(
                            modifier,
                            packageName,
                            name,
                            "",
                            timeDiff,
                            context
                        ) { onClick(it) }
                    }
                }
            }
            Text(
                "Top usage all time:",
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            LazyColumn {
                itemsIndexed(usageAllTime.toList()
                    .sortedByDescending { it.second }) { index, (packageName, timeDiff) ->
                    if (packageName == null) {
                        return@itemsIndexed
                    }
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val name = appNames.find { it.packageName == packageName }?.displayName
                        ?: packageManager.getApplicationLabel(appInfo).toString()
                    ListItem(
                        modifier,
                        packageName,
                        name,
                        "",
                        timeDiff,
                        context
                    ) { onClick(it) }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun ListItem(
    modifier: Modifier,
    packageName: String,
    displayName: String,
    icon: String?,
    usage: Long,
    context: Context,
    onClick: (packageName: String) -> Unit
) {
    val packageManager = context.packageManager
    val placeholder = try {
        placeholder(packageManager.getApplicationIcon(packageName))
    } catch (e: Exception) {
        placeholder(R.drawable.android)
    }
    Card(
        modifier = Modifier
            .padding(5.dp)
            .height(75.dp)
            .clickable { onClick(packageName) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.75f)
        ) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                GlideImage(
                    model = icon,
                    contentDescription = "",
                    modifier = Modifier.size(75.dp),
                    contentScale = ContentScale.Crop,
                    failure = placeholder
                )
            }
            Spacer(modifier.width(20.dp))
            Column(
                modifier = modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(displayName, style = MaterialTheme.typography.headlineSmall)
                Text(millisecondsToTimeString(usage), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}