package com.sajeg.timetracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sajeg.timetracker.AppOverview
import com.sajeg.timetracker.R
import com.sajeg.timetracker.Settings
import com.sajeg.timetracker.classes.FeedbackManager
import com.sajeg.timetracker.classes.SettingsManager

@Composable
fun Settings(navController: NavController) {
    val context = LocalContext.current
    val timeFormat = remember { mutableIntStateOf(-1) }
    var message = remember { mutableStateOf("") }
    val currentDestination = navController.currentDestination?.route
    if (timeFormat.intValue == -1) {
        SettingsManager(context).readInt("time_format") { timeFormat.intValue = it }
    }
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
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
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row {
                Text(
                    "mm/dd/yy date format",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(15.dp)
                )
                Switch(
                    checked = timeFormat.intValue == 1,
                    onCheckedChange = {
                        timeFormat.intValue = if (it) 1 else 0
                        SettingsManager(context).saveInt(
                            "time_format",
                            if (it) 1 else 0
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Got some feedback? Or feature request? Let me know: ",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 15.dp)
            )
            TextField(
                value = message.value,
                onValueChange = { message.value = it },
                modifier = Modifier
                    .size(500.dp, 200.dp)
                    .padding(15.dp)
            )
            Button(
                onClick = {
                    FeedbackManager().sendDiscordMessage(message.value)
                    message.value = ""
                },
                modifier = Modifier.padding(horizontal = 15.dp)
            ) {
                Text("Send Feedback to the Developer")
            }

        }
    }
}