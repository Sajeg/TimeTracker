package com.sajeg.timetracker.classes

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.sajeg.timetracker.composables.isValidApp
import com.sajeg.timetracker.convertEpochToDateWithSeconds
import com.sajeg.timetracker.database.DatabaseManager
import com.sajeg.timetracker.database.EventEntity
import com.sajeg.timetracker.millisecondsToTimeString
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class UsageStatsFetcher(val context: Context) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun updateDatabase(done: () -> Unit) {
        SettingsManager(context).readLong("last_scan") { startTime ->
            val endTime = System.currentTimeMillis()
            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            val eventList = HashMap<String, MutableList<UsageEvents.Event>>()
            val eventEntities = mutableListOf<EventEntity>()
            while (usageEvents.hasNextEvent()) {
                val currentEvent = UsageEvents.Event()
                usageEvents.getNextEvent(currentEvent)
                if ((currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                            currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED ||
                            currentEvent.eventType == UsageEvents.Event.ACTIVITY_STOPPED) &&
                    isValidApp(currentEvent.packageName, context)
                ) {
                    val key = currentEvent.packageName
                    Log.d("EventRegister", key)
                    eventList.getOrPut(key) { mutableListOf(currentEvent) }.add(currentEvent)
                }
            }

            eventList.forEach { (packageName, events) ->
                val totalEvents = events.size

                val filteredEvents = mutableListOf<UsageEvents.Event>()
                events.sortBy { it.timeStamp }
                var lastStopEvent: UsageEvents.Event? = null
                for (event in events) {
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        lastStopEvent?.let { filteredEvents.add(it) }
                        lastStopEvent = null
                        filteredEvents.add(event)
                    } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED || event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                        lastStopEvent = event
                    }
                }
                lastStopEvent?.let { filteredEvents.add(it) }


                if (filteredEvents.size > 1) {
                    for (i in 0 until filteredEvents.size - 1) {
                        val e0 = filteredEvents[i]
                        val e1 = filteredEvents[i + 1]

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
                    if (filteredEvents[1].eventType == UsageEvents.Event.ACTIVITY_PAUSED || filteredEvents[1].eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                        eventEntities.add(
                            EventEntity(
                                0,
                                packageName,
                                startTime,
                                filteredEvents[1].timeStamp,
                                filteredEvents[1].timeStamp - startTime
                            )
                        )
                    }
                    if (filteredEvents.last().eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        eventEntities.add(
                            EventEntity(
                                0,
                                packageName,
                                filteredEvents.last().timeStamp,
                                endTime,
                                endTime - filteredEvents.last().timeStamp
                            )
                        )
                    }
                    Log.d("EventScanner", "new event")
                }
                Log.d("EventTypeSortedArray", events.map { it.eventType }.toString())

            }
            Log.d("ALLEVENTS", eventEntities.toString())
            SettingsManager(context).saveLong("last_scan", endTime)
            val dbManager = DatabaseManager(context)
            dbManager.addEvent(*eventEntities.toTypedArray())
            dbManager.close()
            done()
        }
    }

    fun getHourlyDayAppUsage(
        packageName: String,
        year: Int,
        month: Int,
        day: Int,
        onResponse: (PlottingData) -> Unit
    ) {
        val output = HashMap<Long, Long>()
        val userZoneId = ZoneId.systemDefault()
        val userZoneOffset = ZonedDateTime.now(userZoneId).offset
        val startTime = LocalDateTime.of(year, month, day, 0, 0, 0).toEpochSecond(userZoneOffset)
        val hoursList = mutableListOf<Long>()
        val minutesList = mutableListOf<Long>()
        Log.d("StartTime", startTime.toString())
        output.put(0, 0L)
        for (hourOfTime in 1..24) {
            getUsageStats(
                (startTime + (3600 * (hourOfTime - 1))) * 1000,
                (startTime + (3600 * hourOfTime)) * 1000
            ) { usageTime ->
                val appUsed = usageTime[packageName]
                output.put(hourOfTime.toLong(), appUsed?.div(60 * 1000) ?: 0)
                minutesList.add(appUsed?.div(60 * 1000) ?: 0)
                hoursList.add(hourOfTime.toLong())
                if (output.size == 24) {
                    onResponse(PlottingData(output.keys, output.values))
                }
            }
        }
    }

    private fun getUsageStats(
        startTime: Long,
        endTime: Long,
        result: (appUsage: HashMap<String, Long>) -> Unit
    ) {
        val appUsage = HashMap<String, Long>()
        val database = DatabaseManager(context)
        database.getEvents(startTime, endTime) { events ->
            events.forEach { event ->
                if (event.startTime < startTime && event.endTime > endTime) {
                    appUsage.merge(event.packageName, (endTime - startTime), Long::plus)
                } else if (event.startTime < startTime) {
                    appUsage.merge(event.packageName, (event.endTime - startTime), Long::plus)
                } else if (event.endTime > endTime) {
                    appUsage.merge(event.packageName, (endTime - event.startTime), Long::plus)
                } else {
                    appUsage.merge(event.packageName, event.timeDiff, Long::plus)
                }
            }
            result(appUsage)
            database.close()
        }
    }
}