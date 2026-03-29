package com.example.cthehabit.services

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
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
                    Log.d(
                        "SYNC_WORKER",
                        "Dia: $day | App: $app | Tiempo(ms): $time"
                    )
                }
            }

            Log.d("SYNC_WORKER", "Sync ejecutado correctamente")

            // próximo sync
            val nextSync = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)

            val prefs =
                applicationContext.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

            prefs.edit()
                .putLong("next_sync_time", nextSync)
                .apply()

            Result.success()

        } catch (e: Exception) {

            Log.e("SYNC_WORKER", "Error en sync", e)
            Result.retry()
        }
    }

    companion object {

        private const val WORK_NAME = "SyncAppsUsageWorker"

        fun schedule(context: Context) {

            val request =
                PeriodicWorkRequestBuilder<SyncAppsUsageWorker>(
                    1, TimeUnit.HOURS
                ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )

            val nextSync = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)

            val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

            prefs.edit()
                .putLong("next_sync_time", nextSync)
                .apply()
        }
    }
}