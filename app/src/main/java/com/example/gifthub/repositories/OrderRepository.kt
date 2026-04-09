package com.example.gifthub.repositories

import com.example.gifthub.models.CartItemDto
import com.example.gifthub.models.OrderDto
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()

    private fun currentUserId(): String? = auth.currentUser?.uid

    private fun ordersCollection(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("orders")

    private fun notificationsCollection(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("notifications")

    private fun cartDocument(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("shoppingCart")
            .document("current")

    private fun cartItemsCollection(userId: String) =
        cartDocument(userId).collection("items")

    private fun productsCollection() = db.collection("products")

    fun placeOrder(
        order: OrderDto,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        if (order.items.isEmpty()) {
            onError("Your cart is empty.")
            return
        }

        if (order.address.isBlank()) {
            onError("Please enter or select a delivery address.")
            return
        }

        if (order.paymentMethod.isBlank()) {
            onError("Please enter or select a payment method.")
            return
        }

        val orderRef = ordersCollection(uid).document()
        val notificationRef = notificationsCollection(uid).document()
        val cartRef = cartDocument(uid)

        db.runTransaction { transaction ->
            val verifiedItems = mutableListOf<Map<String, Any>>()
            var recalculatedTotal = 0.0

            order.items.forEach { item ->
                if (item.productId.isBlank()) {
                    throw IllegalStateException("Invalid product found in cart.")
                }

                val productRef = productsCollection().document(item.productId)
                val productSnapshot = transaction.get(productRef)

                if (!productSnapshot.exists()) {
                    val missingName = item.name.ifBlank { item.productId }
                    throw IllegalStateException("Product '$missingName' is no longer available.")
                }

                val productName = productSnapshot.getString("name") ?: item.name
                val productPrice = productSnapshot.getDouble("price") ?: item.price
                val productStock = (productSnapshot.getLong("stock") ?: 0L).toInt()
                val productImageUrl = productSnapshot.getString("imageUrl") ?: item.imageUrl

                if (item.quantity <= 0) {
                    throw IllegalStateException("Invalid quantity for '$productName'.")
                }

                if (productStock < item.quantity) {
                    throw IllegalStateException(
                        "Not enough stock for '$productName'. Only $productStock left."
                    )
                }

                transaction.update(productRef, "stock", productStock - item.quantity)

                verifiedItems.add(
                    mapOf(
                        "productId" to item.productId,
                        "name" to productName,
                        "price" to productPrice,
                        "quantity" to item.quantity,
                        "imageUrl" to productImageUrl
                    )
                )

                recalculatedTotal += productPrice * item.quantity
                transaction.delete(cartItemsCollection(uid).document(item.productId))
            }

            val now = Timestamp.now()
            val safeStatus = order.status.ifBlank { "Pending" }

            val orderData = hashMapOf<String, Any>(
                "orderId" to orderRef.id,
                "userId" to uid,
                "items" to verifiedItems,
                "totalAmount" to recalculatedTotal,
                "address" to order.address.trim(),
                "paymentMethod" to order.paymentMethod.trim(),
                "status" to safeStatus,
                "createdAt" to now
            )

            val notificationData = hashMapOf<String, Any>(
                "notificationID" to notificationRef.id,
                "userId" to uid,
                "title" to "Order placed",
                "message" to "Your order #${orderRef.id.take(8).uppercase()} was placed successfully.",
                "createdDate" to System.currentTimeMillis(),
                "markedAsRead" to false,
                "type" to "order_created",
                "targetRoute" to "order_history",
                "orderId" to orderRef.id
            )

            transaction.set(orderRef, orderData)
            transaction.set(notificationRef, notificationData)
            transaction.set(
                cartRef,
                mapOf(
                    "cartId" to "current",
                    "userId" to uid,
                    "temporaryValue" to 0.0
                )
            )

            orderRef.id
        }.addOnSuccessListener { orderId ->
            // Send push notification
            sendPushNotification(
                title = "Order Placed",
                message = "Your order #${orderId.take(8).uppercase()} was placed successfully.",
                type = "order_created",
                orderId = orderId
            )
            onSuccess(orderId)
        }.addOnFailureListener { e ->
            onError(e.message ?: "Failed to place order")
        }
    }

    fun getOrdersForCurrentUser(
        onSuccess: (List<OrderDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        ordersCollection(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(parseOrders(snapshot.documents))
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to load orders")
            }
    }

    fun getAllOrders(
        onSuccess: (List<OrderDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collectionGroup("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(parseOrders(snapshot.documents))
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to load all orders")
            }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collectionGroup("orders")
            .whereEqualTo("orderId", orderId)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val document = snapshot.documents.firstOrNull()

                if (document == null) {
                    onError("Order not found")
                    return@addOnSuccessListener
                }

                val userId = document.getString("userId").orEmpty()

                document.reference
                    .update("status", newStatus)
                    .addOnSuccessListener {
                        if (userId.isBlank()) {
                            onSuccess()
                            return@addOnSuccessListener
                        }

                        val notificationRef = notificationsCollection(userId).document()
                        val notificationData = hashMapOf<String, Any>(
                            "notificationID" to notificationRef.id,
                            "userId" to userId,
                            "title" to "Order updated",
                            "message" to "Your order #${orderId.take(8).uppercase()} is now '$newStatus'.",
                            "createdDate" to System.currentTimeMillis(),
                            "markedAsRead" to false,
                            "type" to "order_status_updated",
                            "targetRoute" to "order_history",
                            "orderId" to orderId
                        )

                        notificationRef.set(notificationData)
                            .addOnSuccessListener {
                                // Send push notification to customer
                                sendPushNotification(
                                    title = "Order Updated",
                                    message = "Your order #${orderId.take(8).uppercase()} is now '$newStatus'.",
                                    type = "order_status_updated",
                                    orderId = orderId
                                )
                                onSuccess()
                            }
                            .addOnFailureListener { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to update status")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to find order")
            }
    }

    private fun sendPushNotification(
        title: String,
        message: String,
        type: String,
        orderId: String
    ) {
        val uid = currentUserId() ?: return

        // Get FCM token and send notification
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val fcmToken = userDoc.getString("fcmToken")
                if (fcmToken != null) {
                    // In a real app, you'd call your backend to send the push notification
                    // using Cloud Functions or similar
                    // For now, we're just logging it
                    println("Push notification ready to send: $title - $message")
                }
            }
    }

    private fun parseOrders(documents: List<DocumentSnapshot>): List<OrderDto> {
        return documents.map { doc ->
            val rawItems = doc.get("items") as? List<*> ?: emptyList<Any>()

            val items = rawItems.mapNotNull { rawItem ->
                val map = rawItem as? Map<*, *> ?: return@mapNotNull null

                CartItemDto(
                    productId = map["productId"] as? String ?: "",
                    name = map["name"] as? String ?: "",
                    price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                    quantity = (map["quantity"] as? Number)?.toInt() ?: 1,
                    imageUrl = map["imageUrl"] as? String ?: ""
                )
            }

            OrderDto(
                orderId = doc.getString("orderId") ?: doc.id,
                userId = doc.getString("userId") ?: "",
                items = items,
                totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0,
                address = doc.getString("address") ?: "",
                paymentMethod = doc.getString("paymentMethod") ?: "",
                status = doc.getString("status") ?: "Pending",
                createdAt = doc.getTimestamp("createdAt")
            )
        }
    }
}