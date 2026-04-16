package com.example.gifthub.screens.notifications


import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gifthub.MainActivity
import com.example.gifthub.repositories.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class GiftHubMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "gifthub_main"
        const val CHANNEL_NAME = "GiftHub Notifications"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "GiftHub push notifications"
                    enableVibration(true)
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }

        fun showLocalNotification(
            context: Context,
            title: String,
            message: String,
            notificationId: Int = System.currentTimeMillis().toInt()
        ) {
            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "GiftHub"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""
        val type = remoteMessage.data["type"] ?: "general"
        val targetRoute = remoteMessage.data["targetRoute"] ?: "home"
        val orderId = remoteMessage.data["orderId"] ?: ""

        showLocalNotification(this, title, body)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        NotificationRepository().createOrderNotification(
            userId = uid,
            title = title,
            message = body,
            orderId = orderId,
            targetRoute = targetRoute,
            type = type
        )
    }
}