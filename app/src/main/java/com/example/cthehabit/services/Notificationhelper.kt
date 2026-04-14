package com.example.cthehabit.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cthehabit.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "social_usage_channel"
    private const val CHANNEL_NAME = "Uso de Redes Sociales"
    private const val CHANNEL_DESC = "Avisos cuando llevas demasiado tiempo en redes sociales"

    // CONFIGURACIÓN DE PRUEBAS
    //    MODO_PRUEBA = true  → notifica cada 2 minutos
    //    MODO_PRUEBA = false → notifica cada 60 minutos (1 hora)

    private const val MODO_PRUEBA = false
    private val THRESHOLD_MINUTES = if (MODO_PRUEBA) 2L else 60L

    private fun notifIdForUnit(unit: Int) = 100 + unit



    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = CHANNEL_DESC }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    // Notificación de bienvenida (una sola vez)

    fun showWelcomeNotification(context: Context) {
        val prefs = context.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("welcome_shown", false)) return
        createChannel(context)

        try {
            NotificationManagerCompat.from(context).notify(
                200,
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("👋 Bienvenido a C The Habit")
                    .setContentText("Te ayudamos a reducir tu uso de redes sociales 💪")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(
                        "El equipo de C The Habit te da la bienvenida 🎉. Empieza hoy a construir mejores hábitos."
                    ))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()
            )
            prefs.edit().putBoolean("welcome_shown", true).apply()
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    // Check principal: llamar tras cada sync o refresh

    fun checkAndNotify(context: Context, totalSocialMinutes: Long) {
        createChannel(context)

        if (totalSocialMinutes < THRESHOLD_MINUTES) return

        val prefs = context.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)

        // Resetear si cambió el día
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        if (prefs.getString("last_notif_day", "") != today) {
            prefs.edit()
                .putString("last_notif_day", today)
                .putInt("last_notified_unit", 0)
                .apply()
        }

        // Cuántas "unidades" (horas en prod, 2-min en prueba) se han completado hoy
        val unitsReached = (totalSocialMinutes / THRESHOLD_MINUTES).toInt()
        val lastNotified = prefs.getInt("last_notified_unit", 0)

        for (unit in (lastNotified + 1)..unitsReached) {
            sendNotification(context, unit, totalSocialMinutes)
        }

        if (unitsReached > lastNotified) {
            prefs.edit().putInt("last_notified_unit", unitsReached).apply()
        }
    }

    // Envío de notificación individual

    private fun sendNotification(context: Context, unit: Int, totalMinutes: Long) {
        val (title, body) = buildMessage(unit, totalMinutes)

        val pendingIntent = PendingIntent.getActivity(
            context, unit,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            NotificationManagerCompat.from(context).notify(
                notifIdForUnit(unit),
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
            )
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    // Mensajes
    private fun buildMessage(unit: Int, totalMinutes: Long): Pair<String, String> {
        return if (MODO_PRUEBA) {
            "⏱️ ${totalMinutes} min en redes hoy" to
                    "Llevas $totalMinutes minutos en redes sociales. ¡Recuerda tus misiones en C The Habit! 💪"
        } else {
            val title = when (unit) {
                1    -> "⏱️ 1 hora en redes sociales"
                2    -> "📱 Ya llevas 2 horas en redes"
                3    -> "😬 3 horas... ¿todo bien?"
                4    -> "🚨 4 horas en redes sociales hoy"
                else -> "📵 ${unit} horas en redes sociales"
            }
            val body = when {
                unit == 1 ->
                    "Llevas 1 hora en redes hoy. Recuerda que tienes misiones pendientes en C The Habit. ¡Un pequeño descanso puede ayudar!"
                unit <= 3 ->
                    "Ya son $unit horas en redes. Tienes misiones esperándote en C The Habit, ¿qué tal si las revisas?"
                else ->
                    "¡$unit horas en redes hoy! Es un buen momento para desconectarte."
            }
            title to body
        }
    }
}