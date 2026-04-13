package com.example.cthehabit.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cthehabit.data.repositories.getUsageLast24h
import kotlinx.coroutines.*

class UsageMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val FOREGROUND_CHANNEL_ID = "usage_monitor_channel"
        private const val FOREGROUND_NOTIF_ID = 1  // ID fijo de la notificación persistente

        // CAMBIA ESTO SEGÚN EL MODO
        //    PRUEBA:      30_000L  (30 segundos)
        //    PRODUCCIÓN:  300_000L (5 minutos)

        private const val CHECK_INTERVAL_MS = 30_000L

        fun start(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, UsageMonitorService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createForegroundChannel()
        // Arranca el servicio con una notificación persistente (requerido por Android)
        startForeground(FOREGROUND_NOTIF_ID, buildForegroundNotification())
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY: Android reinicia el servicio si lo mata
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }


    // Loop principal: revisa el uso cada CHECK_INTERVAL_MS

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val usageStats = getUsageLast24h(applicationContext)
                    val totalMinutes = getTodaySocialMinutes(usageStats)
                    Log.d("MONITOR_SERVICE", "Redes sociales hoy: $totalMinutes min")
                    NotificationHelper.checkAndNotify(applicationContext, totalMinutes)
                } catch (e: Exception) {
                    Log.e("MONITOR_SERVICE", "Error en check", e)
                }
                delay(CHECK_INTERVAL_MS)
            }
        }
    }


    // Canal y notificación persistente del Foreground Service

    private fun createForegroundChannel() {
        val channel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "Monitor de uso",
            NotificationManager.IMPORTANCE_LOW  // LOW = sin sonido, solo icono
        ).apply {
            description = "Servicio activo que monitorea tu uso de redes sociales"
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("C The Habit activo")
            .setContentText("Monitoreando tu uso de redes sociales 👀")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}