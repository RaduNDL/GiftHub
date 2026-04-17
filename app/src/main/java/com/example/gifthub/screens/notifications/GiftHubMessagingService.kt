package com.example.gifthub.screens.notifications

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GiftHubMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        DeviceIdProvider.init(applicationContext)
        NotificationHelper.ensureChannels(applicationContext)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token.isBlank()) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        PushTokenManager.updateToken(uid, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (isDuplicateMessage(message.messageId)) return

        val data = message.data
        val title = message.notification?.title
            ?: data["title"]
            ?: "GiftHub"
        val body = message.notification?.body
            ?: data["message"]
            ?: data["body"]
            ?: return

        val targetRoute = data["targetRoute"]?.takeIf { it.isNotBlank() }
        val channelId = data["channelId"]?.takeIf { it.isNotBlank() }
            ?: NotificationHelper.CHANNEL_ID_GENERAL

        val stableKey = data["notificationId"]
            ?: message.messageId
            ?: "$title|$body"

        NotificationHelper.show(
            context = applicationContext,
            channelId = channelId,
            title = title,
            message = body,
            targetRoute = targetRoute,
            notificationId = stableKey.hashCode()
        )
    }

    private fun isDuplicateMessage(messageId: String?): Boolean {
        if (messageId.isNullOrBlank()) return false
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val seen = prefs.getStringSet(KEY_SEEN_IDS, emptySet()).orEmpty()
        if (messageId in seen) return true
        val updated = seen.toMutableSet().apply { add(messageId) }
        val trimmed = if (updated.size > MAX_SEEN_IDS) {
            updated.toList().takeLast(MAX_SEEN_IDS).toSet()
        } else {
            updated
        }
        prefs.edit().putStringSet(KEY_SEEN_IDS, trimmed).apply()
        return false
    }

    companion object {
        private const val PREFS_NAME = "gifthub_fcm_prefs"
        private const val KEY_SEEN_IDS = "seen_message_ids"
        private const val MAX_SEEN_IDS = 200
    }
}