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
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        if (cart.items.isEmpty()) {
            onError("Cart is empty")
            return
        }

        if (address.isBlank()) {
            onError("Address is required")
            return
        }

        if (paymentMethod.isBlank()) {
            onError("Payment method is required")
            return
        }

        val orderId = UUID.randomUUID().toString()
        val orderRef = firestore.collection("users").document(uid).collection("orders").document(orderId)

        val orderDto = OrderDto(
            orderId = orderId,
            userId = uid,
            items = cart.items,
            totalAmount = cart.temporaryValue,
            address = address,
            paymentMethod = paymentMethod,
            status = "Pending",
            createdAt = Timestamp.now()
        )

        orderRef.set(orderDto)
            .addOnSuccessListener {
                onSuccess(orderId)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to place order")
            }
    }

    fun loadOrders(
        isEmployee: Boolean,
        onSuccess: (List<OrderDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        try {
            if (isEmployee) {
                firestore.collectionGroup("orders")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val orders = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(OrderDto::class.java)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        onSuccess(orders)
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Failed to load orders")
                    }
            } else {
                firestore.collection("users").document(uid).collection("orders")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val orders = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(OrderDto::class.java)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        onSuccess(orders)
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Failed to load orders")
                    }
            }
        } catch (e: Exception) {
            onError(e.message ?: "An error occurred")
        }
    }

    fun getOrderById(
        orderId: String,
        onSuccess: (OrderDto?) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        firestore.collection("users").document(uid).collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val order = document.toObject(OrderDto::class.java)
                        onSuccess(order)
                    } catch (e: Exception) {
                        onError("Failed to parse order: ${e.message}")
                    }
                } else {
                    onError("Order not found")
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to fetch order")
            }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (newStatus.isBlank()) {
            onError("Status cannot be empty")
            return
        }

        firestore.collectionGroup("orders")
            .whereEqualTo("orderId", orderId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.documents.isNotEmpty()) {
                    val doc = snapshot.documents.first()
                    doc.reference.update("status", newStatus)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener {
                            onError(it.message ?: "Failed to update status")
                        }
                } else {
                    onError("Order not found")
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to find order")
            }
    }

    fun cancelOrder(
        orderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        firestore.collection("users").document(uid).collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val order = document.toObject(OrderDto::class.java)
                    if (order != null && order.status == "Pending") {
                        document.reference.update("status", "Cancelled")
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener {
                                onError(it.message ?: "Failed to cancel order")
                            }
                    } else {
                        onError("Order cannot be cancelled. Current status: ${order?.status}")
                    }
                } else {
                    onError("Order not found")
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to fetch order")
            }
    }

    fun getOrderCount(
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        firestore.collection("users").document(uid).collection("orders")
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.documents.size)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to get order count")
            }
    }

    fun getOrdersByStatus(
        status: String,
        onSuccess: (List<OrderDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        firestore.collection("users").document(uid).collection("orders")
            .whereEqualTo("status", status)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(OrderDto::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                onSuccess(orders)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to load orders")
            }
    }
}