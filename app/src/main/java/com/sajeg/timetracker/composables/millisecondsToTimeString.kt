package com.sajeg.timetracker.composables

fun millisecondsToTimeString(time: Long): String {
    var seconds = time / 1000
    var minutes = seconds / 60
    var hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        else -> "0h"
    }
}