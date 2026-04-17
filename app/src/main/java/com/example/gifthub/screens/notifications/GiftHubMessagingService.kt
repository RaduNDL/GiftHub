package com.example.gifthub.screens.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.gifthub.R

object GiftHubMessagingService {
    private const val CHANNEL_ID = "gifthub_local_channel"
    private val lastShownByKey = mutableMapOf<String, Long>()
    private const val DEDUP_WINDOW_MS = 45_000L

    private fun shouldShow(key: String): Boolean {
        val now = System.currentTimeMillis()
        val last = lastShownByKey[key] ?: 0L
        if (now - last < DEDUP_WINDOW_MS) return false
        lastShownByKey[key] = now
        return true
    }

    fun showLocalNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        dedupKey: String = "$title|$message"
    ) {
        if (!shouldShow(dedupKey)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GiftHub Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }
}