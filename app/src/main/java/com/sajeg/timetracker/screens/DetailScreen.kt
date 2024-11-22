package com.sajeg.timetracker.screens

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.R
import com.sajeg.timetracker.Settings
import com.sajeg.timetracker.classes.PlottingData
import com.sajeg.timetracker.classes.SettingsManager
import com.sajeg.timetracker.classes.UsageStatsFetcher
import com.sajeg.timetracker.composables.Plot
import com.sajeg.timetracker.convertEpochToDate
import com.sajeg.timetracker.database.DatabaseManager
import com.sajeg.timetracker.database.EventEntity
import com.sajeg.timetracker.millisecondsToTimeString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailScreen(navController: NavController, packageName: String) {
    navController.context
    val context = LocalContext.current
    val today = LocalDate.now()
    var name by remember { mutableStateOf("") }
    var usageDataHourly by remember { mutableStateOf<PlottingData?>(null) }
    val userZoneOffset = ZonedDateTime.now(ZoneId.systemDefault()).offset
    var events by remember { mutableStateOf<List<EventEntity>>(listOf()) }
    var totalPlaytime by remember { mutableLongStateOf(0L) }
    var todayPlaytime by remember { mutableLongStateOf(0L) }
    var lastWeekTotalPlaytime by remember { mutableLongStateOf(0L) }
    var lastWeekAllGamesPlaytime by remember { mutableLongStateOf(-1L) }
    var usFormat by remember { mutableStateOf(false) }
    var deleteWarningShown by remember { mutableStateOf(false) }
    val currentDestination = navController.currentDestination?.route

    val packageManager = context.packageManager
    val placeholder = try {
        placeholder(packageManager.getApplicationIcon(packageName))
    } catch (e: Exception) {
        placeholder(R.drawable.android)
    }

    LaunchedEffect(Unit) {
        SettingsManager(context).readInt("time_format") {
            usFormat = it == 1
        }
        UsageStatsFetcher(context)
            .getHourlyDayAppUsage(
                packageName = packageName,
                year = today.year,
                month = today.month.value,
                day = today.dayOfMonth
            ) {
                usageDataHourly = it
            }
    }

    if (name == "") {
        val dbManager = DatabaseManager(context)
        dbManager.getAppNames { names ->
            name = names.find { it.packageName == packageName }?.displayName ?: "GAME"
            dbManager.close()
        }
    }
    if (events.isEmpty()) {
        val dbManager = DatabaseManager(context)
        dbManager.getAppEvents(packageName) { appEvent ->
            dbManager.close()
            var todayTime = LocalDateTime
                .of(today.year, today.month, today.dayOfMonth, 0, 0, 0)
                .toEpochSecond(userZoneOffset)
            todayTime *= 1000
            events = appEvent
            events.sortedBy { it.startTime }
            events.forEach { event ->
                totalPlaytime += event.timeDiff
                if (event.startTime > todayTime) {
                    todayPlaytime += event.timeDiff
                } else if (event.endTime > todayTime) {
                    todayPlaytime += event.endTime - todayTime
                }
            }
        }
    }
    if (lastWeekAllGamesPlaytime == -1L) {
        val dbManager = DatabaseManager(context)
        val lastWeekDay = LocalDate.now().minusWeeks(1)
        val lastWeekTime = LocalDateTime
            .of(lastWeekDay.year, lastWeekDay.month, lastWeekDay.dayOfMonth, 0, 0, 0)
            .toEpochSecond(userZoneOffset)
        dbManager.getEvents(lastWeekTime * 1000, System.currentTimeMillis()) { appEvents ->
            lastWeekAllGamesPlaytime = 0L
            appEvents.forEach { event ->
                lastWeekAllGamesPlaytime += event.timeDiff
                if (event.packageName == packageName) {
                    lastWeekTotalPlaytime += event.timeDiff
                }
            }
            dbManager.close()
        }
    }
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
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
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 30.dp)
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .height(220.dp)
                        .weight(0.4f)
                ) {
                    GlideImage(
                        model = "https://files.cocaine.trade/LauncherIcons/oculus_landscape/${packageName}.jpg",
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        failure = placeholder
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(15.dp)
                        )
                        .weight(0.5f)
                ) {
                    if (usageDataHourly != null) {
                        Plot(
                            Modifier.padding(10.dp),
                            0.4f,
                            usageDataHourly!!,
//                            lastWeekDataHourly!!
                        )
                    }
                }
            }
            Row {
                if (lastWeekAllGamesPlaytime != -1L) {
                    val percentage =
                        lastWeekTotalPlaytime.toFloat() / lastWeekAllGamesPlaytime.toFloat()
                    val text = buildAnnotatedString {
                        append("In the past week you played ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(millisecondsToTimeString(lastWeekTotalPlaytime))
                        }
                        append(" ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(name)
                        }
                        append(" that's ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append((percentage * 100).toInt().toString())
                        }
                        append("% of your weeks total playtime.")
                    }
                    Text(text, fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground)
                }
            }
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                columns = GridCells.Fixed(3)
            ) {
                if (events.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .padding(15.dp)
                        ) {
                            Column {
                                Text(
                                    "Since ${
                                        convertEpochToDate(
                                            events[0].startTime,
                                            usFormat
                                        )
                                    } \nyou " +
                                            "played ${millisecondsToTimeString(totalPlaytime)}",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 28.sp,
                                    lineHeight = 32.sp
                                )
                            }
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .padding(15.dp)
                        ) {
                            Column {
                                Text(
                                    "You played it the \nlast time on \n${
                                        convertEpochToDate(
                                            events.last().endTime,
                                            usFormat
                                        )
                                    }",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 28.sp,
                                    lineHeight = 32.sp
                                )
                            }
                        }
                    }
                    if (todayPlaytime > 0L) {
                        item {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(15.dp)
                                    )
                                    .padding(15.dp)
                            ) {
                                Column {
                                    Text(
                                        "Today you played ${millisecondsToTimeString(todayPlaytime)}",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 28.sp,
                                        lineHeight = 32.sp
                                    )
                                }
                            }
                        }
                    }
                    if (deleteWarningShown) {
                        item {
                            val colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                disabledContentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(15.dp)
                                    )
                                    .padding(15.dp)
                            ) {
                                Column {
                                    Text(
                                        "Reset all data about this game?",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 28.sp,
                                        lineHeight = 32.sp
                                    )
                                    Text(
                                        "This can't be undone.",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Button({
                                            DatabaseManager(context).deleteAppData(packageName)
                                            navController.navigate(AppOverview)
                                        }, colors = colors) { Text("Reset data") }
                                        Button({ deleteWarningShown = false }) { Text("Cancel") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
            ) {
                val colors = IconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.weight(0.05f))
                Text(
                    text = "Have a great idea what to display here? Send me a message in the settings.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(0.1f)
                )
                FilledIconButton(
                    { deleteWarningShown = !deleteWarningShown; Log.d("Pressed", "the burron") },
                    colors = colors
                ) {
                    Icon(painterResource(R.drawable.delete), "delete")
                }
            }
        }
    }
}
