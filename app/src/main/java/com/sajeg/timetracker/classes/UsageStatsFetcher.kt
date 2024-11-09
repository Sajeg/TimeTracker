package com.sajeg.timetracker.classes

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.sajeg.timetracker.composables.isValidApp
import com.sajeg.timetracker.database.EventEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class UsageStatsFetcher(val context: Context) {
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getUsedApps(startTime: Long, endTime: Long): List<UsageStats> {
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST, startTime, endTime
        )

        return usageStatsList
    }

    fun updateDatabase() {
        SettingsManager(context).readLong("last_scan") { startTime ->
            val endTime = System.currentTimeMillis()
            var usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            var eventList = HashMap<String, MutableList<UsageEvents.Event>>()
            var eventEntities = mutableListOf<EventEntity>()
            while (usageEvents.hasNextEvent()) {
                val currentEvent = UsageEvents.Event()
                usageEvents.getNextEvent(currentEvent)
                if ((currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                            currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED ||
                            currentEvent.eventType == UsageEvents.Event.ACTIVITY_STOPPED) &&
                    isValidApp(currentEvent.packageName, context)
                ) {
                    val key = currentEvent.packageName
                    eventList.getOrPut(key) { mutableListOf(currentEvent) }.add(currentEvent)
                }
            }

            eventList.forEach { (packageName, events) ->
                val totalEvents = events.size
                if (totalEvents > 1) {
                    for (i in 0 until totalEvents - 1) {
                        val e0 = events[i]
                        val e1 = events[i + 1]

                        // Maybe there is a need to include Event.ACTIVITY_STOPPED
                        if (e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                            (e1.eventType == UsageEvents.Event.ACTIVITY_PAUSED || e1.eventType == UsageEvents.Event.ACTIVITY_STOPPED)
                        ) {
                            eventEntities.add(
                                EventEntity(
                                    0,
                                    packageName,
                                    e0.timeStamp,
                                    e1.timeStamp,
                                    e1.timeStamp - e0.timeStamp
                                )
                            )
                        }
                    }
                    if (events[1].eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        eventEntities.add(
                            EventEntity(
                                0,
                                packageName,
                                startTime,
                                events[1].timeStamp,
                                events[1].timeStamp - startTime
                            )
                        )
                    }
                    if (events.last().eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        eventEntities.add(
                            EventEntity(
                                0,
                                packageName,
                                events.last().timeStamp,
                                endTime,
                                endTime - events.last().timeStamp
                            )
                        )
                    }
                }
            }
            SettingsManager(context).saveLong("last_scan", endTime)
            DatabaseManager(context).addEvent(*eventEntities.toTypedArray())
        }
    }

    fun getTotalPlaytime(packageName: String, onResponse: (time: kotlin.Long) -> Unit) {
        var totalPlayTime = 0L
        DatabaseManager(context).getAppEvents(packageName) { events ->
            events.forEach { event ->
                totalPlayTime += event.timeDiff
            }
            onResponse(totalPlayTime)
        }
    }

    fun getHourlyDayAppUsage(packageName: String, year: Int, month: Int, day: Int): PlottingData {
        val output = HashMap<Int, Long>()
        val userZoneId = ZoneId.systemDefault()
        val userZoneOffset = ZonedDateTime.now(userZoneId).offset
        val startTime = LocalDateTime.of(year, month, day, 0, 0, 0).toEpochSecond(userZoneOffset)
        val hoursList = mutableListOf<Long>()
        val minutesList = mutableListOf<Long>()
        Log.d("StartTime", startTime.toString())
        output.put(0, 0L)
        for (hourOfTime in 1..24) {
            var usageTime = getUsageStats(
                startTime + (3600) * (hourOfTime - 1),
                startTime + (3600) * hourOfTime
            )
            val appUsed = usageTime.get(packageName)
            minutesList.add(appUsed?.div(1000 * 60) ?: 0)
            hoursList.add(hourOfTime.toLong())
        }

        return PlottingData(hoursList, minutesList)
    }

    fun getUsageStats(startTime: Long, endTime: Long): HashMap<String, Long> {
        var usageEvents = usageStatsManager.queryEvents(startTime * 1000, endTime * 1000)
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