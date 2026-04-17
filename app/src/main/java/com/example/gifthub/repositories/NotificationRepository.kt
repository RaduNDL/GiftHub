package com.example.gifthub.repositories

import com.example.gifthub.models.NotificationDto
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.screens.notifications.DeviceIdProvider
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

    private fun normalizeType(type: String): String {
        val t = type.trim().lowercase()
        return when (t) {
            "favorite_update", "favorite_added", "favorite_removed" -> "favorite_update"
            "order_update", "order_placed", "order_status", "order_cancelled" -> "order_update"
            "cart_update", "cart_added", "cart_removed" -> "cart_update"
            "product_update", "product_added", "product_deleted" -> "product_update"
            "review_update", "review_added", "review_deleted" -> "review_update"
            "payment_update", "payment_added", "payment_deleted" -> "payment_update"
            "address_update", "address_added", "address_deleted" -> "address_update"
            "auth_update", "auth_login", "auth_register", "auth_logout" -> "auth_update"
            else -> if (t.isBlank()) "general" else t
        }
    }

    private fun normalizeTargetRoute(type: String, targetRoute: String): String {
        val route = targetRoute.trim()
        if (route.isNotEmpty()) return route
        return when (normalizeType(type)) {
            "favorite_update" -> GiftHubDestinations.FAVORITES
            "order_update" -> GiftHubDestinations.ORDER_HISTORY
            "cart_update" -> GiftHubDestinations.CART
            "product_update", "review_update" -> GiftHubDestinations.PRODUCTS
            "payment_update" -> GiftHubDestinations.SAVED_PAYMENTS
            "address_update" -> GiftHubDestinations.MANAGE_ADDRESS
            "auth_update" -> GiftHubDestinations.HOME
            else -> GiftHubDestinations.NOTIFICATIONS
        }
    }

    fun createNotification(
        userId: String = currentUserId().orEmpty(),
        title: String,
        message: String,
        type: String = "general",
        targetRoute: String = "",
        orderId: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (userId.isBlank()) {
            onError("User not authenticated")
            return
        }
        if (title.isBlank() || message.isBlank()) {
            onError("Notification title and message are required")
            return
        }

        val docRef = notificationsCollection(userId).document()
        val normalizedType = normalizeType(type)
        val normalizedRoute = normalizeTargetRoute(normalizedType, targetRoute)

        val payload = hashMapOf<String, Any>(
            "notificationID" to docRef.id,
            "userId" to userId,
            "title" to title.trim(),
            "message" to message.trim(),
            "createdDate" to System.currentTimeMillis(),
            "markedAsRead" to false,
            "type" to normalizedType,
            "targetRoute" to normalizedRoute,
            "orderId" to orderId,
            "sourceDeviceId" to DeviceIdProvider.getDeviceIdOrEmpty()
        )

        docRef.set(payload)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to create notification") }
    }

    fun createOrderNotification(
        userId: String = currentUserId().orEmpty(),
        title: String,
        message: String,
        orderId: String = "",
        targetRoute: String = GiftHubDestinations.ORDER_HISTORY,
        type: String = "order_update",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        createNotification(userId, title, message, type, targetRoute, orderId, onSuccess, onError)
    }

    fun createFavoriteNotification(
        userId: String = currentUserId().orEmpty(),
        title: String,
        message: String,
        targetRoute: String = GiftHubDestinations.FAVORITES,
        type: String = "favorite_update",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        createNotification(userId, title, message, type, targetRoute, "", onSuccess, onError)
    }

    fun createCartNotification(
        userId: String = currentUserId().orEmpty(),
        title: String,
        message: String,
        targetRoute: String = GiftHubDestinations.CART,
        type: String = "cart_update",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        createNotification(userId, title, message, type, targetRoute, "", onSuccess, onError)
    }

    fun createProductNotification(
        userId: String = currentUserId().orEmpty(),
        title: String,
        message: String,
        targetRoute: String = GiftHubDestinations.PRODUCTS,
        type: String = "product_update",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        createNotification(userId, title, message, type, targetRoute, "", onSuccess, onError)
    }

    fun createPaymentNotification(
        userId: String = currentUserId().orEmpty(),
        title: String,
        message: String,
        targetRoute: String = GiftHubDestinations.SAVED_PAYMENTS,
        type: String = "payment_update",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        createNotification(userId, title, message, type, targetRoute, "", onSuccess, onError)
    }

    fun createAddressNotification(
        userId: String = currentUserId().orEmpty(),
        title: String,
        message: String,
        targetRoute: String = GiftHubDestinations.MANAGE_ADDRESS,
        type: String = "address_update",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        createNotification(userId, title, message, type, targetRoute, "", onSuccess, onError)
    }

    fun getNotifications(
        onSuccess: (List<NotificationDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: run {
            onError("User not authenticated")
            return
        }

        notificationsCollection(uid)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val notifications = snapshot.documents.map { doc ->
                    val rawType = doc.getString("type") ?: "general"
                    val rawRoute = doc.getString("targetRoute") ?: ""
                    NotificationDto(
                        notificationID = doc.id,
                        userId = uid,
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        createdDate = doc.getLong("createdDate") ?: 0L,
                        markedAsRead = doc.getBoolean("markedAsRead") ?: false,
                        type = normalizeType(rawType),
                        targetRoute = normalizeTargetRoute(rawType, rawRoute),
                        orderId = doc.getString("orderId") ?: ""
                    )
                }
                onSuccess(notifications)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load notifications") }
    }

    fun markAsRead(notificationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: run {
            onError("User not authenticated")
            return
        }
        if (notificationId.isBlank()) {
            onError("Invalid notification ID")
            return
        }
        notificationsCollection(uid)
            .document(notificationId)
            .update("markedAsRead", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to update notification") }
    }

    fun deleteNotification(notificationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: run {
            onError("User not authenticated")
            return
        }
        if (notificationId.isBlank()) {
            onError("Invalid notification ID")
            return
        }
        notificationsCollection(uid)
            .document(notificationId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to delete notification") }
    }
}