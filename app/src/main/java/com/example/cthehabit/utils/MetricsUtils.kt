package com.example.cthehabit.utils

/**
 * Convierte ms → minutos por día y app
 */
fun mapToMinutes(
    data: Map<String, Map<String, Long>>
): Map<String, Map<String, Float>> {

    return data.mapValues { (_, apps) ->
        apps.mapValues { it.value / 1000f / 60f }
    }
}

/**
 * Total de uso por app (sumando todos los días)
 */
fun getUsageByApp(
    data: Map<String, Map<String, Long>>
): Map<String, Float> {

    val result = mutableMapOf<String, Long>()

    data.forEach { (_, apps) ->
        apps.forEach { (app, time) ->
            result[app] = (result[app] ?: 0L) + time
        }
    }

    return result.mapValues {
        it.value / 1000f / 60f
    }
}

/**
 * Total de uso por día (para gráficas)
 */
fun getTotalUsagePerDay(
    data: Map<String, Map<String, Long>>
): Map<String, Float> {

    return data.mapValues { (_, apps) ->
        apps.values.sum() / 1000f / 60f
    }
}