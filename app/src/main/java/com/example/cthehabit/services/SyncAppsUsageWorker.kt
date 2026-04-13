package com.example.cthehabit.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.cthehabit.data.repositories.getUsageLast24h
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

val SOCIAL_KEYWORDS = listOf(
    "instagram", "facebook", "tiktok", "musically",
    "twitter", "x.", "reddit", "youtube", "snapchat", "vsco", "whatsapp"
)

fun isSocialApp(packageName: String): Boolean {
    val pkg = packageName.lowercase()
    return SOCIAL_KEYWORDS.any { pkg.contains(it) }
}


fun getTodaySocialMinutes(usageStats: Map<String, Map<String, Long>>): Long {
    val todayKey = java.text.SimpleDateFormat("d/M", java.util.Locale.getDefault())
        .format(java.util.Date())

    val todayApps = usageStats[todayKey] ?: run {
        Log.d("SYNC_WORKER", "Sin datos para hoy ($todayKey). Keys: ${usageStats.keys}")
        return 0L
    }

    val totalMs = todayApps
        .filter { (app, _) -> isSocialApp(app) }
        .also { it.forEach { (app, ms) -> Log.d("SYNC_WORKER", "  $app → ${ms / 60_000} min") } }
        .values.sumOf { it }

    return totalMs / 60_000
}

class SyncAppsUsageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val usageStats = getUsageLast24h(applicationContext)
            val totalSocialMinutes = getTodaySocialMinutes(usageStats)
            Log.d("SYNC_WORKER", "Total redes sociales HOY: $totalSocialMinutes min")

            NotificationHelper.checkAndNotify(applicationContext, totalSocialMinutes)

            val now = System.currentTimeMillis()
            applicationContext.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE).edit()
                .putLong("next_sync_time", now + TimeUnit.MINUTES.toMillis(15))
                .putLong("last_sync_time", now)
                .apply()

            Log.d("SYNC_WORKER", "Sync OK")
            Result.success()
        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Error en sync", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "SyncAppsUsageWorker"

        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SyncAppsUsageWorker>(15, TimeUnit.MINUTES)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                    .build()
            )

            val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            if (!prefs.contains("next_sync_time")) {
                prefs.edit()
                    .putLong("next_sync_time", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15))
                    .apply()
            }
        }
    }
}