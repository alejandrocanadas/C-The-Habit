package com.example.cthehabit.utils

import com.example.cthehabit.data.entity.AppUsage
import java.util.*

fun getUsageByApp(apps: List<AppUsage>): Map<String, Float> {

    val map = mutableMapOf<String, Long>()

    apps.forEach { app ->
        map[app.packageName] =
            (map[app.packageName] ?: 0) + app.timeInForeground
    }

    return map.mapValues {
        (it.value / 1000f / 60f)
    }
}

fun getDailyUsage(apps: List<AppUsage>): Map<String, Float> {

    val calendar = Calendar.getInstance()

    val map = mutableMapOf<String, Long>()

    apps.forEach {

        calendar.timeInMillis = it.lastTimeUsed
        val key =
            "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)+1}"

        map[key] =
            (map[key] ?: 0) + it.timeInForeground
    }

    return map.mapValues {
        it.value / 1000f / 60f
    }
}