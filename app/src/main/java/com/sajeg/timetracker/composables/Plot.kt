package com.sajeg.timetracker.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.sajeg.timetracker.classes.PlottingData

@Composable
fun Plot(modifier: Modifier, zoom: Float, vararg data: PlottingData) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val scrollState = rememberVicoScrollState()
    val zoomState = rememberVicoZoomState(
        zoomEnabled = false,
        initialZoom = remember { Zoom.static(zoom) }
    )
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineSeries {
                data.forEach {
                    series(x = it.x, y = it.y)
                }
                series(
                    x = listOf(
                        0,
                        1,
                        2,
                        3,
                        4,
                        5,
                        6,
                        7,
                        8,
                        9,
                        10,
                        11,
                        12,
                        13,
                        14,
                        15,
                        16,
                        17,
                        18,
                        19,
                        20,
                        21,
                        22,
                        23,
                        24
                    ),
                    y = listOf(
                        0,
                        10,
                        24,
                        36,
                        42,
                        35,
                        36,
                        75,
                        58,
                        79,
                        53,
                        23,
                        45,
                        24,
                        65,
                        87,
                        56,
                        94,
                        32,
                        54,
                        10,
                        12,
                        40,
                        35,
                        24
                    )
                )
                series(
                    x = listOf(
                        0,
                        1,
                        2,
                        3,
                        4,
                        5,
                        6,
                        7,
                        8,
                        9,
                        10,
                        11,
                        12,
                        13,
                        14,
                        15,
                        16,
                        17,
                        18,
                        19,
                        20,
                        21,
                        22,
                        23,
                        24
                    ),
                    y = listOf(
                        0,
                        40,
                        34,
                        64,
                        3,
                        35,
                        31,
                        76,
                        58,
                        79,
                        53,
                        23,
                        0,
                        45,
                        65,
                        87,
                        56,
                        94,
                        32,
                        54,
                        10,
                        12,
                        50,
                        45,
                        24
                    )
                )
            }
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            rememberCartesianChart(
                rememberLineCartesianLayer(
                    LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            remember { LineCartesianLayer.LineFill.single(fill(Color(0xffa485e0))) }
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    itemPlacer = remember { VerticalAxis.ItemPlacer.step({ 5.0 }) },
                    title = "Minutes played"
                ),
                bottomAxis =
                HorizontalAxis.rememberBottom(
                    guideline = null,
                    itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned(3) },
                    title = "Time",
                )
            ),
            modelProducer,
            modifier,
            scrollState,
            zoomState
        )
    }
}