package com.example.gifthub.screens.notifications

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object NotificationRealtimeListener {
    private var registration: ListenerRegistration? = null
    private val shownIds = mutableSetOf<String>()

    fun start(context: Context) {
        stop()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        registration = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("notifications")
            .addSnapshotListener { snapshot, _ ->
                snapshot ?: return@addSnapshotListener
                snapshot.documentChanges.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val notificationId = doc.id
                        if (shownIds.contains(notificationId)) return@forEach
                        shownIds.add(notificationId)

                        val title = doc.getString("title") ?: "GiftHub"
                        val message = doc.getString("message") ?: "You have a new notification"
                        val route = doc.getString("targetRoute")

                        NotificationHelper.show(
                            context = context,
                            channelId = NotificationHelper.CHANNEL_ID_GENERAL,
                            title = title,
                            message = message,
                            targetRoute = route,
                            notificationId = notificationId.hashCode()
                        )
                    }
                }
            }
    }

    fun stop() {
        registration?.remove()
        registration = null
        shownIds.clear()
    }
}