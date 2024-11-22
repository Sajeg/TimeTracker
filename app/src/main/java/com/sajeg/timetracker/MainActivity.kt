package com.sajeg.timetracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sajeg.timetracker.classes.BackgroundUpdater
import com.sajeg.timetracker.ui.theme.TimeTrackerTheme
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeTrackerTheme {
                navController = rememberNavController()
                SetupNavGraph(navController = navController)
                val updateWorkRequest =
                    PeriodicWorkRequestBuilder<BackgroundUpdater>(12, TimeUnit.HOURS)
                        .setConstraints(
                            Constraints.Builder()
                                .build()
                        )
                        .build()
                WorkManager
                    .getInstance(this)
                    .enqueueUniquePeriodicWork(
                        "updatePlaytime",
                        ExistingPeriodicWorkPolicy.KEEP,
                        updateWorkRequest
                    )
            }
        }
    }
}