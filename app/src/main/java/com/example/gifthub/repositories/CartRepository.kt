package com.example.gifthub.repositories

import com.example.gifthub.models.CartItemDto
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.ShoppingCartDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun cartItemsRef(userId: String) =
        firestore.collection("users").document(userId).collection("cart")

    fun getCart(onSuccess: (ShoppingCartDto) -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")

        cartItemsRef(uid).get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        CartItemDto(
                            cartItemId = doc.id,
                            productId = doc.getString("productId") ?: "",
                            name = doc.getString("productName") ?: "",
                            imageUrl = doc.getString("productImage") ?: "",
                            price = doc.getDouble("basePrice") ?: 0.0,
                            quantity = (doc.getLong("quantity") ?: 1L).toInt(),
                            lineTotalPrice = doc.getDouble("lineTotalPrice") ?: 0.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                val total = items.sumOf { it.lineTotalPrice }
                onSuccess(
                    ShoppingCartDto(
                        cartId = "current",
                        userId = uid,
                        items = items,
                        temporaryValue = total
                    )
                )
            }
            .addOnFailureListener { onError(it.message ?: "Error fetching cart") }
    }

    fun addToCart(
        product: ProductDto,
        quantityToAdd: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")
        if (product.idProduct.isBlank()) return onError("Invalid product")
        if (quantityToAdd <= 0) return onError("Invalid quantity")

        val safeQty = quantityToAdd.coerceAtLeast(1)
        val docId = product.idProduct
        val itemRef = cartItemsRef(uid).document(docId)

        itemRef.get()
            .addOnSuccessListener { doc ->
                val now = System.currentTimeMillis()
                if (doc.exists()) {
                    val oldQty = (doc.getLong("quantity") ?: 0L).toInt()
                    val newQty = oldQty + safeQty
                    val newTotal = product.price * newQty
                    itemRef.update(
                        mapOf(
                            "quantity" to newQty,
                            "lineTotalPrice" to newTotal,
                            "updatedAt" to now
                        )
                    )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Error updating item") }
                } else {
                    val item = hashMapOf(
                        "id" to docId,
                        "userId" to uid,
                        "productId" to product.idProduct,
                        "productName" to product.name,
                        "productImage" to product.imageUrl,
                        "basePrice" to product.price,
                        "quantity" to safeQty,
                        "lineTotalPrice" to product.price * safeQty,
                        "createdAt" to now,
                        "updatedAt" to now
                    )
                    itemRef.set(item)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Error adding item") }
                }
            }
            .addOnFailureListener { onError(it.message ?: "Error reading cart") }
    }

    fun updateItemQuantity(
        cartItemId: String,
        newQuantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")
        if (cartItemId.isBlank()) return onError("Invalid cart item ID")
        if (newQuantity <= 0) return onError("Invalid quantity")

        cartItemsRef(uid).document(cartItemId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener onError("Item not found")

                val basePrice = doc.getDouble("basePrice") ?: 0.0
                val newTotal = basePrice * newQuantity

                cartItemsRef(uid).document(cartItemId).update(
                    mapOf(
                        "quantity" to newQuantity,
                        "lineTotalPrice" to newTotal,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error updating quantity") }
            }
            .addOnFailureListener { onError(it.message ?: "Error fetching item") }
    }

    fun removeFromCart(
        cartItemId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")
        if (cartItemId.isBlank()) return onError("Invalid cart item ID")

        cartItemsRef(uid).document(cartItemId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error removing item") }
    }

    fun clearCart(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")

        cartItemsRef(uid).get()
            .addOnSuccessListener { snapshot ->
                var deleted = 0
                snapshot.documents.forEach { doc ->
                    cartItemsRef(uid).document(doc.id).delete()
                        .addOnSuccessListener { deleted++ }
                        .addOnFailureListener { return@addOnFailureListener onError(it.message ?: "Error clearing cart") }
                }
                if (deleted == snapshot.documents.size) onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Error fetching cart") }
    }
}