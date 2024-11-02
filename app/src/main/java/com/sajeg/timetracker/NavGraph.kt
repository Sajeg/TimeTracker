package com.sajeg.timetracker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable


@Composable
fun SetupNavGraph(
    navController: NavHostController,
) {
    NavHost(navController = navController, startDestination = RecentScreen) {
        composable<RecentScreen> {
//            Recent(navController)
        }
        composable<FileViewerScreen> {
            val params = it.toRoute<FileViewerScreen>()
//            FileViewer(navController, params.path)
        }
    }
}

@Serializable
object RecentScreen


@Serializable
data class FileViewerScreen(
    val path: String
)

