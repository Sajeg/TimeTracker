package com.sajeg.timetracker.screens

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.timetracker.classes.PlottingData
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
    val lastWeek = LocalDate.now().minusWeeks(1)
    var name = remember { mutableStateOf("") }
    var usageDataHourly = remember { mutableStateOf<PlottingData?>(null) }
    var lastWeekDataHourly = remember { mutableStateOf<PlottingData?>(null) }
    val userZoneOffset = ZonedDateTime.now(ZoneId.systemDefault()).offset
    val events = remember { mutableStateOf<List<EventEntity>>(listOf()) }
    val totalPlaytime = remember { mutableLongStateOf(0L) }
    val lastWeekTotalPlaytime = remember { mutableLongStateOf(0L) }
    val lastWeekAllGamesPlaytime = remember { mutableLongStateOf(-1L) }

    if (usageDataHourly.value == null) {
        LaunchedEffect(usageDataHourly.value) {
            UsageStatsFetcher(context)
                .getHourlyDayAppUsage(
                    packageName = packageName,
                    year = today.year,
                    month = today.month.value,
                    day = today.dayOfMonth
                ) {
                    usageDataHourly.value = it
                }
        }
    }
//    if (lastWeekDataHourly.value == null) {
//        LaunchedEffect(lastWeekDataHourly.value) {
//            UsageStatsFetcher(context)
//                .getHourlyDayAppUsage(
//                    packageName = packageName,
//                    year = lastWeek.year,
//                    month = lastWeek.month.value,
//                    day = lastWeek.dayOfMonth
//                ) {
//                    lastWeekDataHourly.value = it
//                }
//        }
//    }
    if (name.value == "") {
        val dbManager = DatabaseManager(context)
        dbManager.getAppNames { names ->
            name.value = names.find { it.packageName == packageName }?.displayName ?: "GAME"
            dbManager.close()
        }
    }
    if (events.value.isEmpty()) {
        val dbManager = DatabaseManager(context)
        dbManager.getAppEvents(packageName) {
            events.value = it
            events.value.sortedBy { it.startTime }
            events.value.forEach { event ->
                totalPlaytime.value += event.timeDiff
            }
            dbManager.close()
        }
    }
    if (lastWeekAllGamesPlaytime.longValue == -1L) {
        val dbManager = DatabaseManager(context)
        val lastWeekDay = LocalDate.now().minusWeeks(1)
        val lastWeekTime = LocalDateTime
            .of(lastWeekDay.year, lastWeekDay.month.value, lastWeekDay.dayOfMonth, 0, 0, 0)
            .toEpochSecond(userZoneOffset)
        dbManager.getEvents(lastWeekTime * 1000, System.currentTimeMillis()) { events ->
            lastWeekAllGamesPlaytime.longValue = 0L
            events.forEach { event ->
                lastWeekAllGamesPlaytime.longValue += event.timeDiff
                if (event.packageName == packageName) {
                    lastWeekTotalPlaytime.longValue += event.timeDiff
                }
            }
            dbManager.close()
        }
    }
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
            .fillMaxSize()
    ) {
        Column {
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
                        contentScale = ContentScale.Crop,
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
                    if (usageDataHourly.value != null) {
                        Plot(
                            Modifier.padding(10.dp),
                            0.4f,
                            usageDataHourly.value!!,
//                            lastWeekDataHourly.value!!
                        )
                    }
                }
            }
            Row {
                if (lastWeekAllGamesPlaytime.longValue != -1L) {
                    val percentage =
                        lastWeekTotalPlaytime.longValue.toFloat() / lastWeekAllGamesPlaytime.longValue.toFloat()
                    var text = buildAnnotatedString {
                        append("In the past week you played ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(millisecondsToTimeString(lastWeekTotalPlaytime.longValue))
                        }
                        append(" ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(name.value)
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
            Row(
                modifier = Modifier
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                if (!events.value.isEmpty()) {
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
                            "Since ${convertEpochToDate(events.value[0].startTime)} \nyou " +
                                    "played ${millisecondsToTimeString(totalPlaytime.longValue)}",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 28.sp,
                                lineHeight = 32.sp
                                )
                        }
                    }
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
                                "You played it the \nlast time on \n${convertEpochToDate(events.value.last().endTime)}",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 28.sp,
                                lineHeight = 32.sp
                            )
                        }
                    }
                }
            }
            Row (
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().fillMaxSize()
            ){
                Text(
                    text = "Have a great idea what to display here? Send me a message in the settings.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
