package com.example.cthehabit.data.repositories

import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.cthehabit.data.entity.AppUsage

fun getUsageStats(context: Context): List<AppUsage> {

    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val endTime = System.currentTimeMillis()
    val startTime = endTime - (1000 * 60 * 60 * 24)

    val stats = usm.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        endTime
    ) ?: return emptyList()

    return stats
        .filter { it.totalTimeInForeground > 0 }
        .map {
            AppUsage(
                packageName = it.packageName,
                timeInForeground = it.totalTimeInForeground,
                lastTimeUsed = it.lastTimeUsed
            )
        }
        .sortedByDescending { it.timeInForeground }
}