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
                        markedAsRead = doc.getBoolean("markedAsRead") ?: false
                    )
                }
                onSuccess(notifications)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to load notifications")
            }
    }

    fun markAsRead(
        notificationId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        notificationsCollection(uid)
            .document(notificationId)
            .update("markedAsRead", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to update notification")
            }
    }

    fun deleteNotification(
        notificationId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        notificationsCollection(uid)
            .document(notificationId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to delete notification")
            }
    }
}