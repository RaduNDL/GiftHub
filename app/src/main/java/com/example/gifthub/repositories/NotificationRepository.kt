package com.example.gifthub.repositories

import com.example.gifthub.models.NotificationDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun currentUserId(): String? = auth.currentUser?.uid

    private fun notificationsCollection(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("notifications")

    fun createOrderNotification(
        userId: String = currentUserId().orEmpty(),
        title: String,
        message: String,
        orderId: String = "",
        targetRoute: String = "order_history",
        type: String = "order_update",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (userId.isBlank()) {
            onError("User not authenticated"); return
        }
        if (title.isBlank() || message.isBlank()) {
            onError("Notification title and message are required"); return
        }

        val docRef = notificationsCollection(userId).document()
        val payload = NotificationDto(
            notificationID = docRef.id,
            userId = userId,
            title = title.trim(),
            message = message.trim(),
            createdDate = System.currentTimeMillis(),
            markedAsRead = false,
            type = type,
            targetRoute = targetRoute,
            orderId = orderId
        )

        docRef.set(payload)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to create notification")
            }
    }

    fun getNotifications(
        onSuccess: (List<NotificationDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        notificationsCollection(uid)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val notifications = snapshot.documents.map { doc ->
                    NotificationDto(
                        notificationID = doc.id,
                        userId = uid,
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        createdDate = doc.getLong("createdDate") ?: 0L,
                        markedAsRead = doc.getBoolean("markedAsRead") ?: false,
                        type = doc.getString("type") ?: "giftHubNotification",
                        targetRoute = doc.getString("targetRoute") ?: "order_history",
                        orderId = doc.getString("orderId") ?: ""
                    )
                }
                onSuccess(notifications)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load notifications") }
    }

    fun markAsRead(notificationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: return onError("User not authenticated")
        if (notificationId.isBlank()) return onError("Invalid notification ID")

        notificationsCollection(uid)
            .document(notificationId)
            .update("markedAsRead", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to update notification") }
    }

    fun deleteNotification(notificationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: return onError("User not authenticated")
        if (notificationId.isBlank()) return onError("Invalid notification ID")

        notificationsCollection(uid)
            .document(notificationId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to delete notification") }
    }
}