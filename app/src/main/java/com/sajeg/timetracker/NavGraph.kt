package com.sajeg.timetracker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sajeg.timetracker.screens.AppOverview
import com.sajeg.timetracker.screens.DetailScreen
import com.sajeg.timetracker.screens.Settings
import com.sajeg.timetracker.screens.Setup
import com.sajeg.timetracker.screens.ViewData
import kotlinx.serialization.Serializable


@Composable
fun SetupNavGraph(
    navController: NavHostController,
) {
    NavHost(navController = navController, startDestination = Setup) {
        composable<Setup> {
            Setup(navController)
        }
        composable<ViewData> {
            ViewData(navController)
        }
        composable<DetailScreen> {
            val params = it.toRoute<DetailScreen>()
            DetailScreen(navController, params.packageName)
        }
        composable<AppOverview> {
            AppOverview(navController)
        }
        composable<Settings> {
            Settings(navController)
        }
    }
}

@Serializable
object Setup

@Serializable
object ViewData

@Serializable
data class DetailScreen(
    val packageName: String
)

@Serializable
object AppOverview

@Serializable
object Settings

