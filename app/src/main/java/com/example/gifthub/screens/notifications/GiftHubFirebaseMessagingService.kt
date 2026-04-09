package com.example.gifthub.screens.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gifthub.GiftHubApp
import com.example.gifthub.MainActivity
import com.example.gifthub.R
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.notifications.PushNotificationManager
import com.example.gifthub.notifications.PushTokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class GiftHubFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushTokenManager.saveTokenForCurrentUser(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val title = data["title"] ?: "GiftHub"
        val body = data["body"] ?: "You have a new notification."
        val targetRoute = data["targetRoute"]?.ifBlank { GiftHubDestinations.ORDER_HISTORY }
            ?: GiftHubDestinations.ORDER_HISTORY
        val notificationId = data["notificationId"].orEmpty()
        val orderId = data["orderId"].orEmpty()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(PushNotificationManager.EXTRA_FROM_PUSH, true)
            putExtra(PushNotificationManager.EXTRA_TARGET_ROUTE, targetRoute)
            if (notificationId.isNotBlank()) putExtra(PushNotificationManager.EXTRA_NOTIFICATION_ID, notificationId)
            if (orderId.isNotBlank()) putExtra(PushNotificationManager.EXTRA_ORDER_ID, orderId)
        }

        val requestCode = if (notificationId.isNotBlank()) notificationId.hashCode() else Random.nextInt()
        val pendingIntent = PendingIntent.getActivity(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, GiftHubApp.GIFT_HUB_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val hasPermission =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return
        NotificationManagerCompat.from(this).notify(requestCode, notification)
    }
}