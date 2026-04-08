package com.example.cthehabit.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.cthehabit.data.repositories.getUsageLast24h
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncAppsUsageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val usageStats = getUsageLast24h(applicationContext)
            usageStats.forEach { (day, apps) ->
                apps.forEach { (app, time) ->
                    Log.d("SYNC_WORKER", "Dia: $day | App: $app | Tiempo(ms): $time")
                }
            }

            val now = System.currentTimeMillis()
            val nextSync = now + TimeUnit.MINUTES.toMillis(15)

            val prefs = applicationContext.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

            // Paso 1: actualiza el próximo sync (el contador de la UI lo leerá)
            prefs.edit().putLong("next_sync_time", nextSync).apply()

            // Paso 2: dispara el refresh de estadísticas (listener escucha este key)
            prefs.edit().putLong("last_sync_time", now).apply()

            Log.d("SYNC_WORKER", "Sync OK. Próximo en 15 min")
            Result.success()

        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Error en sync", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "SyncAppsUsageWorker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncAppsUsageWorker>(
                15, TimeUnit.MINUTES
            )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, // ← KEEP: no reinicia si ya existe
                    request
                )

            // Solo escribe el primer next_sync_time si no existe
            val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            if (!prefs.contains("next_sync_time")) {
                prefs.edit()
                    .putLong("next_sync_time", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15))
                    .apply()
            }
        }
    }
}