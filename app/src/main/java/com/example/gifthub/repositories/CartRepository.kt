package com.example.gifthub.repositories

import com.example.gifthub.models.CartItemDto
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.ShoppingCartDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class CartRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun currentUserId(): String? = auth.currentUser?.uid

    private fun cartDocument(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("shoppingCart")
            .document("current")

    private fun cartItemsCollection(userId: String) =
        cartDocument(userId).collection("items")

    fun getCart(
        onSuccess: (ShoppingCartDto) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        cartDocument(uid)
            .get()
            .addOnSuccessListener { cartSnapshot ->
                cartItemsCollection(uid)
                    .get()
                    .addOnSuccessListener { itemsSnapshot ->
                        val items = itemsSnapshot.documents
                            .sortedByDescending { document ->
                                document.getLong("addedAt") ?: 0L
                            }
                            .map { document ->
                                CartItemDto(
                                    productId = document.getString("productId") ?: document.id,
                                    name = document.getString("name") ?: "",
                                    price = document.getDouble("price") ?: 0.0,
                                    quantity = document.getLong("quantity")?.toInt() ?: 1,
                                    imageUrl = document.getString("imageUrl") ?: "",
                                    customText = document.getString("customText") ?: "",
                                    customColor = document.getString("customColor") ?: ""
                                )
                            }

                        val total = items.sumOf { it.price * it.quantity }

                        val cart = ShoppingCartDto(
                            cartId = cartSnapshot.getString("cartId") ?: "current",
                            userId = cartSnapshot.getString("userId") ?: uid,
                            items = items,
                            temporaryValue = total
                        )

                        onSuccess(cart)
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Failed to load cart items")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to load cart")
            }
    }

    fun addToCart(
        product: ProductDto,
        quantityToAdd: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")
        if (quantityToAdd <= 0) return onError("Invalid quantity")

        val cartRef = cartDocument(uid)
        val itemRef = cartItemsCollection(uid).document(product.idProduct)

        cartRef.set(
            mapOf("cartId" to "current", "userId" to uid),
            SetOptions.merge()
        ).addOnSuccessListener {
            itemRef.get().addOnSuccessListener { snapshot ->
                val existingQuantity = snapshot.getLong("quantity")?.toInt() ?: 0
                val newQuantity = existingQuantity + quantityToAdd

                itemRef.set(
                    mapOf(
                        "productId" to product.idProduct,
                        "name" to product.name,
                        "price" to product.price,
                        "quantity" to newQuantity,
                        "imageUrl" to product.imageUrl,
                        "addedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).addOnSuccessListener {
                    recalculateCartTotal(uid, onSuccess, onError)
                }.addOnFailureListener { onError(it.message ?: "Error") }
            }
        }.addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun addCustomizedToCart(
        product: ProductDto,
        quantityToAdd: Int,
        customText: String,
        customColor: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")
        val itemRef = cartItemsCollection(uid).document("${product.idProduct}_custom_${System.currentTimeMillis()}")

        cartDocument(uid).set(
            mapOf("cartId" to "current", "userId" to uid),
            SetOptions.merge()
        ).addOnSuccessListener {
            itemRef.set(
                mapOf(
                    "productId" to product.idProduct,
                    "name" to product.name,
                    "price" to product.price,
                    "quantity" to quantityToAdd,
                    "imageUrl" to product.imageUrl,
                    "customText" to customText,
                    "customColor" to customColor,
                    "addedAt" to System.currentTimeMillis()
                )
            ).addOnSuccessListener {
                recalculateCartTotal(uid, onSuccess, onError)
            }.addOnFailureListener { onError(it.message ?: "Error") }
        }
    }

    fun updateItemQuantity(productId: String, newQuantity: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: return onError("User not authenticated")
        val itemRef = cartItemsCollection(uid).document(productId)

        if (newQuantity <= 0) {
            itemRef.delete().addOnSuccessListener { recalculateCartTotal(uid, onSuccess, onError) }
        } else {
            itemRef.update("quantity", newQuantity).addOnSuccessListener { recalculateCartTotal(uid, onSuccess, onError) }
        }
    }

    fun removeFromCart(productId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: return onError("User not authenticated")
        cartItemsCollection(uid).document(productId).delete().addOnSuccessListener { recalculateCartTotal(uid, onSuccess, onError) }
    }

    fun clearCart(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = currentUserId() ?: return onError("User not authenticated")
        cartItemsCollection(uid).get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().addOnSuccessListener {
                cartDocument(uid).update("temporaryValue", 0.0).addOnSuccessListener { onSuccess() }
            }
        }
    }

    private fun recalculateCartTotal(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        cartItemsCollection(userId).get().addOnSuccessListener { snapshot ->
            val total = snapshot.documents.sumOf { (it.getDouble("price") ?: 0.0) * (it.getLong("quantity")?.toInt() ?: 0) }
            cartDocument(userId).update("temporaryValue", total).addOnSuccessListener { onSuccess() }
        }
    }
}
