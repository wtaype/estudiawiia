package com.estudiawii.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.estudiawii.app.MainActivity
import com.estudiawii.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "EstudiaWii Alerta"
        val body = message.notification?.body ?: message.data["body"] ?: "Tienes una clase programada próximamente."
        showNotification(applicationContext, title, body)
    }

    companion object {
        const val CHANNEL_ID = "estudiawii_notifications"
        const val CHANNEL_NAME = "EstudiaWii Clases"

        fun showNotification(context: Context, title: String, body: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertas de clases y cursos de EstudiaWii"
                    enableLights(true)
                    lightColor = android.graphics.Color.YELLOW
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}

class NotificationSimulatorReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.estudiawii.app.SIMULATE_NOTIFICATION") {
            Handler(Looper.getMainLooper()).postDelayed({
                MyFirebaseMessagingService.showNotification(
                    context,
                    "¡Hora de Estudiar! 📚",
                    "Tu clase de 'Desarrollo Android Premium' está por comenzar. ¡Abre EstudiaWii!"
                )
            }, 2000)
        }
    }
}
