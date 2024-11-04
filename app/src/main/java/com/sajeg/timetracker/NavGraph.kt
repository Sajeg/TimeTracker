package com.sajeg.timetracker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
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
//        composable<FileViewerScreen> {
//            val params = it.toRoute<FileViewerScreen>()
//            FileViewer(navController, params.path)
//        }
    }
}

@Serializable
object Setup

@Serializable
object ViewData

//@Serializable
//data class FileViewerScreen(
//    val path: String
//)

