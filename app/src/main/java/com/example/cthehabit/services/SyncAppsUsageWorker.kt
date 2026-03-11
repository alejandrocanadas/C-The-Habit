package com.example.cthehabit.services

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.cthehabit.data.repositories.getUsageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncAppsUsageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        try {

            val usageStats = getUsageStats(applicationContext)

            usageStats.forEach {
                Log.d(
                    "SYNC_WORKER",
                    "App: ${it.packageName} Tiempo: ${it.timeInForeground}"
                )
            }

            Log.d("SYNC_WORKER", "Sync ejecutado correctamente")

            // actualizar el tiempo del próximo sync
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

            // guardar el primer tiempo de sync
            val nextSync = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)

            val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

            prefs.edit()
                .putLong("next_sync_time", nextSync)
                .apply()
        }
    }
}