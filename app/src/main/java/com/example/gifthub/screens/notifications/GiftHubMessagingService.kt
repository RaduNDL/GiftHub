package com.example.gifthub.screens.notifications

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GiftHubMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = data["title"] ?: notification?.title ?: "GiftHub"
        val message = data["message"] ?: notification?.body ?: "You have a new notification"
        val type = data["type"] ?: "general"
        val id = (data["id"] ?: System.currentTimeMillis().toString()).hashCode()

        val channel = when (type.lowercase()) {
            "product" -> NotificationHelper.CHANNEL_ID_PRODUCTS
            "order" -> NotificationHelper.CHANNEL_ID_ORDERS
            "promotion" -> NotificationHelper.CHANNEL_ID_PROMOTIONS
            else -> NotificationHelper.CHANNEL_ID_GENERAL
        }

        NotificationHelper.show(
            context = this,
            channelId = channel,
            title = title,
            message = message,
            notificationId = id
        )
    }

    private fun saveToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        val updates = mapOf(
            "fcmToken" to token,
            "fcmTokens.$token" to true,
            "lastTokenRefreshAt" to FieldValue.serverTimestamp()
        )

        userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
    }

    companion object {
        fun showLocalNotification(
            context: Context,
            title: String,
            message: String,
            notificationId: Int
        ) {
            NotificationHelper.show(
                context = context,
                channelId = NotificationHelper.CHANNEL_ID_GENERAL,
                title = title,
                message = message,
                notificationId = notificationId
            )
        }
    }
}