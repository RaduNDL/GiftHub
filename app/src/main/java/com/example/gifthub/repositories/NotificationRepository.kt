package com.example.gifthub.repositories

import android.content.Context
import android.util.Log
import com.example.gifthub.models.NotificationDto
import com.example.gifthub.notifications.FcmV1Sender
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

    fun createOrderNotificationAndPush(
        context: Context,
        userId: String,
        title: String,
        message: String,
        orderId: String = "",
        targetRoute: String = "order_history",
        type: String = "order_update",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val docRef = notificationsCollection(userId).document()
        val notificationId = docRef.id

        val payload = hashMapOf(
            "notificationID" to notificationId,
            "userId" to userId,
            "title" to title,
            "message" to message,
            "createdDate" to System.currentTimeMillis(),
            "markedAsRead" to false,
            "type" to type,
            "targetRoute" to targetRoute,
            "orderId" to orderId
        )

        docRef.set(payload)
            .addOnSuccessListener {
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val token = userDoc.getString("fcmToken").orEmpty()
                        FcmV1Sender.sendDataPush(
                            context = context,
                            toToken = token,
                            title = title,
                            body = message,
                            targetRoute = targetRoute,
                            notificationId = notificationId,
                            orderId = orderId,
                            type = type
                        ) { ok, result ->
                            Log.d("FcmV1Sender", "send push ok=$ok result=$result")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("NotificationRepository", "Failed to load token: ${e.message}")
                    }

                onSuccess()
            }
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
            .addOnFailureListener {
                onError(it.message ?: "Failed to load notifications")
            }
    }

    fun markAsRead(notificationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        notificationsCollection(uid)
            .document(notificationId)
            .update("markedAsRead", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to update notification") }
    }

    fun deleteNotification(notificationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        notificationsCollection(uid)
            .document(notificationId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to delete notification") }
    }
}