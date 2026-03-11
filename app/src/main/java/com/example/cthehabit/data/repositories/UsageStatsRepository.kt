package com.example.cthehabit.data.repositories

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.cthehabit.data.entity.AppUsage

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

fun getUsageStats(context: Context): List<AppUsage> {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val endTime = System.currentTimeMillis()
    val startTime = endTime - (1000L * 60 * 60 * 24)

    return calculateUsage(usm, startTime, endTime)
}

fun getUsageStatsLast7Days(context: Context): List<AppUsage> {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val endTime = System.currentTimeMillis()
    val startTime = endTime - (1000L * 60 * 60 * 24 * 7)

    return calculateUsage(usm, startTime, endTime)
}

private fun calculateUsage(
    usm: UsageStatsManager,
    startTime: Long,
    endTime: Long
): List<AppUsage> {

    val events = usm.queryEvents(startTime, endTime)
    val event = UsageEvents.Event()

    val appStartTimes = mutableMapOf<String, Long>()
    val usageTimes = mutableMapOf<String, Long>()
    val lastUsedTimes = mutableMapOf<String, Long>()

    while (events.hasNextEvent()) {
        events.getNextEvent(event)

        val packageName = event.packageName ?: continue

        if (packageName !in SOCIAL_APPS) continue

        when (event.eventType) {

            UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                appStartTimes[packageName] = event.timeStamp

                val currentLastUsed = lastUsedTimes[packageName] ?: 0L
                if (event.timeStamp > currentLastUsed) {
                    lastUsedTimes[packageName] = event.timeStamp
                }
            }

            UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                val start = appStartTimes[packageName] ?: continue
                val duration = event.timeStamp - start

                val currentUsage = usageTimes[packageName] ?: 0L
                usageTimes[packageName] = currentUsage + duration

                val currentLastUsed = lastUsedTimes[packageName] ?: 0L
                if (event.timeStamp > currentLastUsed) {
                    lastUsedTimes[packageName] = event.timeStamp
                }

                appStartTimes.remove(packageName)
            }
        }
    }

    return usageTimes
        .map { entry ->
            AppUsage(
                packageName = entry.key,
                timeInForeground = entry.value,
                lastTimeUsed = lastUsedTimes[entry.key] ?: 0L
            )
        }
        .sortedByDescending { it.timeInForeground }
}