package com.example.gifthub.repositories

import com.example.gifthub.models.OrderDto
import com.example.gifthub.models.ShoppingCartDto
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun placeOrder(cart: ShoppingCartDto, address: String, paymentMethod: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
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

    fun loadOrders(isEmployee: Boolean, onSuccess: (List<OrderDto>) -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        if (isEmployee) {
            firestore.collectionGroup("orders")
                .get()
                .addOnSuccessListener { snapshot ->
                    val orders = snapshot.documents.mapNotNull { it.toObject(OrderDto::class.java) }
                        .sortedByDescending { it.createdAt }
                    onSuccess(orders)
                }
                .addOnFailureListener { onError(it.message ?: "Failed to load orders") }
        } else {
            firestore.collection("users").document(uid).collection("orders")
                .get()
                .addOnSuccessListener { snapshot ->
                    val orders = snapshot.documents.mapNotNull { it.toObject(OrderDto::class.java) }
                        .sortedByDescending { it.createdAt }
                    onSuccess(orders)
                }
                .addOnFailureListener { onError(it.message ?: "Failed to load orders") }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firestore.collectionGroup("orders").whereEqualTo("orderId", orderId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.documents.isNotEmpty()) {
                    val doc = snapshot.documents.first()
                    doc.reference.update("status", newStatus)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Failed to update status") }
                } else {
                    onError("Order not found")
                }
            }
            .addOnFailureListener { onError(it.message ?: "Failed to find order") }
    }
}