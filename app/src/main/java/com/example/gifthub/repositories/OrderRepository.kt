package com.example.gifthub.repositories

import com.example.gifthub.models.OrderDto
import com.example.gifthub.models.ShoppingCartDto
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun placeOrder(
        cart: ShoppingCartDto,
        address: String,
        paymentMethod: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")

        if (cart.items.isEmpty()) return onError("Cart is empty")
        if (address.isBlank()) return onError("Address is required")
        if (paymentMethod.isBlank()) return onError("Payment method is required")

        val orderId = UUID.randomUUID().toString()
        val now = Timestamp.now()
        val total = cart.items.sumOf { it.lineTotalPrice }

        val orderRef = firestore.collection("users").document(uid)
            .collection("orders").document(orderId)

        val orderDto = OrderDto(
            orderId = orderId,
            userId = uid,
            items = cart.items,
            subtotal = total,
            totalAmount = total,
            address = address.trim(),
            paymentMethod = paymentMethod.trim(),
            status = "Pending",
            createdAt = now,
            updatedAt = now
        )

        orderRef.set(orderDto)
            .addOnSuccessListener {
                onSuccess(orderId)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to place order") }
    }

    fun loadOrders(
        isEmployee: Boolean,
        onSuccess: (List<OrderDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")

        val query = if (isEmployee) {
            firestore.collectionGroup("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            firestore.collection("users").document(uid).collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.documents.mapNotNull { doc ->
                    runCatching { doc.toObject(OrderDto::class.java) }.getOrNull()
                }
                onSuccess(orders)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load orders") }
    }

    fun getOrderById(
        orderId: String,
        onSuccess: (OrderDto?) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")
        if (orderId.isBlank()) return onError("Invalid order ID")

        firestore.collection("users").document(uid).collection("orders")
            .document(orderId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onError("Order not found")
                    return@addOnSuccessListener
                }
                val order = runCatching { document.toObject(OrderDto::class.java) }.getOrNull()
                if (order == null) onError("Failed to parse order")
                else onSuccess(order)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to fetch order") }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (orderId.isBlank()) return onError("Invalid order ID")
        if (newStatus.isBlank()) return onError("Status cannot be empty")

        firestore.collectionGroup("orders")
            .whereEqualTo("orderId", orderId)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                if (doc == null) {
                    onError("Order not found")
                    return@addOnSuccessListener
                }
                doc.reference.update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to Timestamp.now()
                    )
                )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Failed to update status") }
            }
            .addOnFailureListener { onError(it.message ?: "Failed to find order") }
    }

    fun cancelOrder(
        orderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")
        if (orderId.isBlank()) return onError("Invalid order ID")

        val orderRef = firestore.collection("users").document(uid)
            .collection("orders").document(orderId)

        orderRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onError("Order not found")
                    return@addOnSuccessListener
                }
                val order = document.toObject(OrderDto::class.java)
                if (order == null) {
                    onError("Failed to parse order")
                    return@addOnSuccessListener
                }
                if (!order.status.equals("Pending", ignoreCase = true)) {
                    onError("Order cannot be cancelled. Current status: ${order.status}")
                    return@addOnSuccessListener
                }
                orderRef.update(
                    mapOf(
                        "status" to "Cancelled",
                        "updatedAt" to Timestamp.now()
                    )
                )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Failed to cancel order") }
            }
            .addOnFailureListener { onError(it.message ?: "Failed to fetch order") }
    }

    fun getOrderCount(
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")

        firestore.collection("users").document(uid).collection("orders")
            .get()
            .addOnSuccessListener { snapshot -> onSuccess(snapshot.size()) }
            .addOnFailureListener { onError(it.message ?: "Failed to get order count") }
    }

    fun getOrdersByStatus(
        status: String,
        onSuccess: (List<OrderDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")
        if (status.isBlank()) return onError("Status cannot be empty")

        firestore.collection("users").document(uid).collection("orders")
            .whereEqualTo("status", status)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.documents.mapNotNull { doc ->
                    runCatching { doc.toObject(OrderDto::class.java) }.getOrNull()
                }
                onSuccess(orders)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load orders") }
    }
}