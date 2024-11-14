package com.sajeg.timetracker.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.R
import com.sajeg.timetracker.ViewData
import com.sajeg.timetracker.classes.PlottingData
import com.sajeg.timetracker.classes.UsageStatsFetcher
import com.sajeg.timetracker.composables.Plot
import java.time.LocalDate

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailScreen(navController: NavController, packageName: String) {
    navController.context
    val context = LocalContext.current
    val today = LocalDate.now()
    val lastWeek = LocalDate.now().minusWeeks(1)
    var usageDataHourly = remember { mutableStateOf<PlottingData?>(null) }
    var lastWeekDataHourly = remember { mutableStateOf<PlottingData?>(null) }
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
    if (lastWeekDataHourly.value == null) {
        LaunchedEffect(lastWeekDataHourly.value) {
            UsageStatsFetcher(context)
                .getHourlyDayAppUsage(
                    packageName = packageName,
                    year = lastWeek.year,
                    month = lastWeek.month.value,
                    day = lastWeek.dayOfMonth
                ) {
                    lastWeekDataHourly.value = it
                }
        }
    }
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        NavigationRail {
            NavigationRailItem(
                selected = false,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.hourglass),
                        contentDescription = ""
                    )
                },
                onClick = { navController.navigate(ViewData) }
            )
            NavigationRailItem(
                selected = false,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.apps),
                        contentDescription = ""
                    )
                },
                onClick = { navController.navigate(AppOverview) }
            )
        }
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
                        Plot(Modifier.padding(10.dp), 0.4f, usageDataHourly.value!!, lastWeekDataHourly.value!!)
                    }
                }
            }
            Row {

                var text = buildAnnotatedString {
                    append("In the past week you played ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("5h and 4m")
                    }
                    append(" ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("Synth Riders")
                    }
                    append(" that's ")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("5%")
                    }
                    append(" of your weeks total playtime.")
                }
                Text(text, fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            Row(
                modifier = Modifier
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
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
                            "Total playtime: 100h 53m",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp
                        )
                        Text(
                            "Last Played: 13. June 2023",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp
                        )
                        Text(
                            "Most played on one Day: 5h",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp
                        )
                        Text(
                            "First Played: 10. June 2024",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp
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
                            "This week: 10h",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp
                        )
                        Text(
                            "Last week: 4h",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp
                        )
                        Text(
                            "This month: 5h",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp
                        )
                        Text(
                            "Last month: 5h",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}
