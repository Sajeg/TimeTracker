package com.sajeg.timetracker

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun convertEpochToDate(epoch: Long, usFormat: Boolean = false): String {
    val instant = Instant.ofEpochMilli(epoch)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = if (usFormat) {
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
    } else {
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
    return dateTime.format(formatter)
}

fun convertEpochToDateWithSeconds(epoch: Long, usFormat: Boolean = false): String {
    val instant = Instant.ofEpochMilli(epoch)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = if (usFormat) {
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
    } else {
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
    }
    return dateTime.format(formatter)
}

fun millisecondsToTimeString(time: Long): String {
    val seconds = time / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        seconds > 0 -> "${seconds}s"
        else -> "0h"
    }
}