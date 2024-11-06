package com.sajeg.timetracker.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.sajeg.timetracker.classes.UsageStatsFetcher

@Composable
fun DetailScreen(navController: NavController, packageName: String) {
    navController.context
    val modelProducer = remember { CartesianChartModelProducer() }
    val scrollState = rememberVicoScrollState()
    val zoomState = rememberVicoZoomState()
    val context = LocalContext.current
    val usageDataHourly = UsageStatsFetcher(context).getHourlyAppUsage(packageName)
    val horizontalAxis = HorizontalAxis.rememberBottom(
        label = rememberAxisLabelComponent(),
        tick = rememberAxisTickComponent(),
        guideline = rememberAxisGuidelineComponent(),
        title = "Time of the Day"
    )
    usageDataHourly.forEach {
        usageDataHourly.put(it.key, it.value / (1000 * 60))
    }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineSeries { series(x = usageDataHourly.keys, y = usageDataHourly.values) }
        }
    }

    Box(
        modifier = Modifier.padding(10.dp)
    ) {
        ProvideVicoTheme(rememberM3VicoTheme()) {
            CartesianChartHost(
                rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = horizontalAxis
                ),
                modelProducer,
                Modifier,
                scrollState,
                zoomState
            )
        }
    }
}
