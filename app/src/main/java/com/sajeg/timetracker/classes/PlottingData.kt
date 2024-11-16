package com.sajeg.timetracker.classes

data class PlottingData(
    val x: MutableSet<Long>,
    val y: MutableCollection<Long>
)

data class PieChartPlottingData(
    val displayName: String,
    val amount: Long
)