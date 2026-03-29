package com.example.cthehabit.data.repositories

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.*

private val SOCIAL_APPS = setOf(
    "com.instagram.android",
    "com.zhiliaoapp.musically",
    "com.zhiliaoapp.musically.go",
    "com.ss.android.ugc.trill",
    "com.reddit.frontpage",
    "com.twitter.android",
    "com.facebook.katana",
    "com.vsco.cam",
    "com.google.android.youtube"
)

fun getUsageLast24h(context: Context): Map<String, Map<String, Long>> {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val endTime = System.currentTimeMillis()
    val startTime = endTime - (1000L * 60 * 60 * 24)

    return calculateUsageByDay(usm, startTime, endTime)
}

fun getUsageLast7Days(context: Context): Map<String, Map<String, Long>> {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val calendar = Calendar.getInstance()
    val endTime = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_YEAR, -6)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val startTime = calendar.timeInMillis

    return calculateUsageByDay(usm, startTime, endTime)
}

private fun calculateUsageByDay(
    usm: UsageStatsManager,
    startTime: Long,
    endTime: Long
): Map<String, Map<String, Long>> {

    val events = usm.queryEvents(startTime, endTime)
    val event = UsageEvents.Event()

    val appStartTimes = mutableMapOf<String, Long>()
    val usagePerDay = mutableMapOf<String, MutableMap<String, Long>>()

    val calendar = Calendar.getInstance()

    while (events.hasNextEvent()) {
        events.getNextEvent(event)

        val packageName = event.packageName ?: continue
        if (packageName !in SOCIAL_APPS) continue

        when (event.eventType) {

            UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                appStartTimes[packageName] = event.timeStamp
            }

            UsageEvents.Event.MOVE_TO_BACKGROUND -> {

                val start = appStartTimes[packageName] ?: continue
                var sessionStart = start
                val sessionEnd = event.timeStamp

                while (sessionStart < sessionEnd) {

                    calendar.timeInMillis = sessionStart

                    val endOfDay = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    val segmentEnd = minOf(sessionEnd, endOfDay)
                    val duration = segmentEnd - sessionStart

                    val key =
                        "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"

                    val appMap = usagePerDay.getOrPut(key) { mutableMapOf() }

                    appMap[packageName] =
                        (appMap[packageName] ?: 0L) + duration

                    sessionStart = segmentEnd + 1
                }

                appStartTimes.remove(packageName)
            }
        }
    }

    // 🔥 cerrar sesiones abiertas
    val currentTime = System.currentTimeMillis()

    appStartTimes.forEach { (packageName, start) ->

        var sessionStart = start

        while (sessionStart < currentTime) {

            calendar.timeInMillis = sessionStart

            val endOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val segmentEnd = minOf(currentTime, endOfDay)
            val duration = segmentEnd - sessionStart

            val key =
                "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"

            val appMap = usagePerDay.getOrPut(key) { mutableMapOf() }

            appMap[packageName] =
                (appMap[packageName] ?: 0L) + duration

            sessionStart = segmentEnd + 1
        }
    }

    return usagePerDay
}

fun getUsageByApp(usageData: Map<String, Map<String, Long>>): Map<String, Float> {
    // Sumar todos los días por app
    val map = mutableMapOf<String, Float>()
    for (dayMap in usageData.values) {
        for ((app, duration) in dayMap) {
            map[app] = (map[app] ?: 0f) + duration.toFloat() / 60000f // convertir ms a min
        }
    }
    return map
}

fun getDailyUsage(usageData: Map<String, Map<String, Long>>): Map<String, Float> {
    // Sumar todas las apps por día
    val map = mutableMapOf<String, Float>()
    for ((day, dayMap) in usageData) {
        map[day] = dayMap.values.sum().toFloat() / 60000f
    }
    return map
}