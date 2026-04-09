package com.example.gifthub.screens.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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

    companion object {
        private const val TAG = "GiftHubFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
        PushTokenManager.saveTokenForCurrentUser(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "onMessageReceived data=${message.data}")

        val data = message.data

        val title = data["title"]
            ?: message.notification?.title
            ?: "GiftHub"

        val body = data["body"]
            ?: message.notification?.body
            ?: "You have a new notification."

        val targetRoute = data["targetRoute"]
            ?.ifBlank { GiftHubDestinations.ORDER_HISTORY }
            ?: GiftHubDestinations.ORDER_HISTORY

        val notificationId = data["notificationId"].orEmpty()
        val orderId = data["orderId"].orEmpty()
        val type = data["type"].orEmpty()

        showNotification(
            title = title,
            body = body,
            targetRoute = targetRoute,
            notificationId = notificationId,
            orderId = orderId,
            type = type
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        targetRoute: String,
        notificationId: String,
        orderId: String,
        type: String
    ) {
        Log.d(
            TAG,
            "showNotification title=$title route=$targetRoute notificationId=$notificationId orderId=$orderId type=$type"
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(PushNotificationManager.EXTRA_FROM_PUSH, true)
            putExtra(PushNotificationManager.EXTRA_TARGET_ROUTE, targetRoute)

            if (notificationId.isNotBlank()) {
                putExtra(PushNotificationManager.EXTRA_NOTIFICATION_ID, notificationId)
            }

            if (orderId.isNotBlank()) {
                putExtra(PushNotificationManager.EXTRA_ORDER_ID, orderId)
            }

            if (type.isNotBlank()) {
                putExtra(PushNotificationManager.EXTRA_NOTIFICATION_TYPE, type)
            }
        }

        val requestCode = if (notificationId.isNotBlank()) {
            notificationId.hashCode()
        } else {
            Random.nextInt(100000, 999999)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
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

        Log.d(TAG, "hasNotificationPermission=$hasPermission")

        if (!hasPermission) return

        NotificationManagerCompat.from(this)
            .notify(requestCode, notification)
    }
}