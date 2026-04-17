package com.example.gifthub.screens.notifications

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object NotificationRealtimeListener {
    private const val PREFS_NAME = "gifthub_listener_prefs"
    private const val KEY_SHOWN_IDS = "shown_notification_ids"
    private const val MAX_SHOWN_IDS = 500

    private var registration: ListenerRegistration? = null
    private var startTimestamp: Long = 0L
    private var activeUserId: String? = null

    fun start(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (activeUserId == uid && registration != null) return

        stop()

        val appContext = context.applicationContext
        DeviceIdProvider.init(appContext)

        activeUserId = uid
        startTimestamp = System.currentTimeMillis()
        val deviceId = DeviceIdProvider.getDeviceId(appContext)

        registration = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("notifications")
            .addSnapshotListener { snapshot, _ ->
                snapshot ?: return@addSnapshotListener
                if (snapshot.metadata.hasPendingWrites()) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->
                    if (change.type != DocumentChange.Type.ADDED) return@forEach

                    val doc = change.document
                    val notificationId = doc.id
                    val createdDate = doc.getLong("createdDate") ?: 0L
                    val source = doc.getString("sourceDeviceId").orEmpty()
                    val alreadyRead = doc.getBoolean("markedAsRead") ?: false

                    if (alreadyRead) return@forEach
                    if (createdDate <= startTimestamp) return@forEach
                    if (source.isNotEmpty() && source == deviceId) return@forEach
                    if (isAlreadyShown(appContext, notificationId)) return@forEach

                    val title = doc.getString("title").orEmpty().ifBlank { "GiftHub" }
                    val message = doc.getString("message").orEmpty().ifBlank { return@forEach }
                    val route = doc.getString("targetRoute")

                    NotificationHelper.show(
                        context = appContext,
                        channelId = NotificationHelper.CHANNEL_ID_GENERAL,
                        title = title,
                        message = message,
                        targetRoute = route,
                        notificationId = notificationId.hashCode()
                    )
                    rememberAsShown(appContext, notificationId)
                }
            }
    }

    fun stop() {
        registration?.remove()
        registration = null
        activeUserId = null
    }

    private fun isAlreadyShown(context: Context, notificationId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_SHOWN_IDS, emptySet()).orEmpty()
        return notificationId in set
    }

    private fun rememberAsShown(context: Context, notificationId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SHOWN_IDS, emptySet()).orEmpty().toMutableSet()
        current.add(notificationId)
        val trimmed = if (current.size > MAX_SHOWN_IDS) {
            current.toList().takeLast(MAX_SHOWN_IDS).toSet()
        } else {
            current
        }
        prefs.edit().putStringSet(KEY_SHOWN_IDS, trimmed).apply()
    }
}