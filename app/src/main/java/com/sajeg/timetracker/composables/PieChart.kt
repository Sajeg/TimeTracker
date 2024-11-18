package com.sajeg.timetracker.composables

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajeg.timetracker.classes.PieChartPlottingData
import com.sajeg.timetracker.millisecondsToTimeString
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PieChart(modifier: Modifier, vararg data: PieChartPlottingData) {
    Box(
        modifier = modifier.padding(20.dp)
    ) {
        val textMeasurer = rememberTextMeasurer()
        val textColor = MaterialTheme.colorScheme.background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val colors = listOf<Color>(
                Color(0xFFA09ABC),
                Color(0xFFB6A6CA),
                Color(0xFFD5CFE1),
                Color(0xFFE1DEE9),
                Color(0xFFD4BEBE)
            )
            val dataList = data.toList().sortedByDescending { it.amount }
            var totalAmount = data.sumOf { it.amount }
            var i = 0
            var startAngle = -90f
            var chartClosed = false

            for (usage in dataList) {
                var sweepAngle = usage.amount / totalAmount.toFloat()
                sweepAngle = sweepAngle * 360f

                val radius = 600f / 2f
                val middleAngle = (startAngle + sweepAngle / 2) * (Math.PI / 180f)
                val x = center.x + (radius * 0.4f * cos(middleAngle)).toFloat()
                val y = center.y + (radius * 0.4f * sin(middleAngle)).toFloat()
                if (i < 4 && sweepAngle > 20) {
                    val textSize = textMeasurer.measure(usage.displayName).size
                    Log.d("PIE", "App: ${usage.displayName} with ${millisecondsToTimeString(usage.amount)}")
                    drawArc(
                        color = colors[i],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - (size.height / 2), 0f),
                        size = Size(size.height, size.height)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = usage.displayName,
                        topLeft = Offset(x - (textSize.width / 2), y - (textSize.height / 2)),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                } else if ((i == 4 || sweepAngle < 21) && chartClosed == false) {
                    val textSize = textMeasurer.measure("Others").size
                    drawArc(
                        color = colors[4],
                        startAngle = startAngle,
                        sweepAngle = 270f - startAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - (size.height / 2), 0f),
                        size = Size(size.height, size.height)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "Others",
                        topLeft = Offset(x - (textSize.width / 2), y - (textSize.height / 2)),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    chartClosed = true
                }
                startAngle += sweepAngle
                i += 1
            }
            val textSize = textMeasurer.measure(millisecondsToTimeString(totalAmount)).size
            drawText(
                textMeasurer = textMeasurer,
                text = millisecondsToTimeString(totalAmount),
                topLeft = Offset(center.x - (textSize.width / 2), center.y - (textSize.height / 2)),
                style = TextStyle(
                    color = textColor,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}