package com.sajeg.timetracker.classes

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import java.time.ZonedDateTime

class UsageStatsFetcher(context: Context) {
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getUsedApps(startTime: Long, endTime: Long): List<UsageStats> {
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST, startTime, endTime
        )

        return usageStatsList
    }

    fun getHourlyAppUsage(packageName: String): HashMap<Int, Long> {
        val output = HashMap<Int, Long>()
        var startTime =
            ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).toEpochSecond() * 1000L

        output.put(0, 0L)
        for (hourOfTime in 1..24) {
            var usageTime = getUsageStats(
                startTime + (3600 * 1000) * (hourOfTime - 1),
                startTime + (3600 * 1000) * hourOfTime
            )
            val appUsed = usageTime.get(packageName)
            if (appUsed == null) {
                output.put(hourOfTime, 0)
            } else {
                output.put(hourOfTime, appUsed)
            }
        }

        return output
    }

    fun getUsageStats(startTime: Long, endTime: Long): HashMap<String, Long> {
        var usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val map = HashMap<String, MutableList<UsageEvents.Event>>()
        val appUsage = HashMap<String, Long>()
        while (usageEvents.hasNextEvent()) {
            val currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED
            ) {
                val key = currentEvent.packageName
                map.getOrPut(key) { mutableListOf() }.add(currentEvent)
            }
        }

        map.forEach { (packageName, events) ->
            var totalTime = 0L
            val totalEvents = events.size

            if (totalEvents > 1) {
                for (i in 0 until totalEvents - 1) {
                    val e0 = events[i]
                    val e1 = events[i + 1]

                    if (e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                        e1.eventType == UsageEvents.Event.ACTIVITY_PAUSED
                    ) {
                        val timeDiff = e1.timeStamp - e0.timeStamp
                        if (timeDiff > 0 && timeDiff < endTime - startTime) {
                            totalTime += timeDiff
                        }
                    }
                }
            }
            if (events.isNotEmpty()) {
                if (events.first().eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                    val timeDiff = events.first().timeStamp - startTime
                    if (timeDiff > 0) {
                        totalTime += timeDiff
                    }
                }

                if (events.last().eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    val timeDiff = endTime - events.last().timeStamp
                    if (timeDiff > 0) {
                        totalTime += timeDiff
                    }
                }
            }

            appUsage.put(packageName, totalTime)
        }

        return appUsage
    }
}