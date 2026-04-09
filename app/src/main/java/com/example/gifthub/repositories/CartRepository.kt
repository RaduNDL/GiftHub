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
                                    imageUrl = document.getString("imageUrl") ?: ""
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
        val uid = currentUserId()
        android.util.Log.d("CART_DEBUG", "Current user id = $uid")
        android.util.Log.d("CART_DEBUG", "Product id = ${product.idProduct}")
        android.util.Log.d("CART_DEBUG", "Quantity = $quantityToAdd")

        if (uid == null) {
            onError("User not authenticated")
            return
        }

        if (quantityToAdd <= 0) {
            onError("Invalid quantity")
            return
        }

        val cartRef = cartDocument(uid)
        val itemRef = cartItemsCollection(uid).document(product.idProduct)

        cartRef.set(
            mapOf(
                "cartId" to "current",
                "userId" to uid
            ),
            SetOptions.merge()
        ).addOnSuccessListener {
            android.util.Log.d("CART_DEBUG", "Cart document created/updated successfully")

            itemRef.get()
                .addOnSuccessListener { snapshot ->
                    val existingQuantity = snapshot.getLong("quantity")?.toInt() ?: 0
                    val newQuantity = existingQuantity + quantityToAdd

                    android.util.Log.d("CART_DEBUG", "Existing quantity = $existingQuantity")
                    android.util.Log.d("CART_DEBUG", "New quantity = $newQuantity")

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
                        android.util.Log.d("CART_DEBUG", "Item saved successfully")

                        recalculateCartTotal(
                            userId = uid,
                            onSuccess = {
                                android.util.Log.d("CART_DEBUG", "Cart total recalculated successfully")
                                onSuccess()
                            },
                            onError = { error ->
                                android.util.Log.e("CART_DEBUG", "Recalculate total failed: $error")
                                onError(error)
                            }
                        )
                    }.addOnFailureListener { exception ->
                        android.util.Log.e("CART_DEBUG", "Failed saving item", exception)
                        onError(exception.message ?: "Failed to add item to cart")
                    }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("CART_DEBUG", "Failed reading existing cart item", exception)
                    onError(exception.message ?: "Failed to read cart item")
                }
        }.addOnFailureListener { exception ->
            android.util.Log.e("CART_DEBUG", "Failed initializing cart", exception)
            onError(exception.message ?: "Failed to initialize cart")
        }
    }
    fun updateItemQuantity(
        productId: String,
        newQuantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")
        val itemRef = cartItemsCollection(uid).document(productId)

        if (newQuantity <= 0) {
            itemRef.delete()
                .addOnSuccessListener {
                    recalculateCartTotal(uid, onSuccess, onError)
                }
                .addOnFailureListener { exception ->
                    onError(exception.message ?: "Failed to remove item")
                }
            return
        }

        itemRef.set(
            mapOf(
                "quantity" to newQuantity,
                "addedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).addOnSuccessListener {
            recalculateCartTotal(uid, onSuccess, onError)
        }.addOnFailureListener { exception ->
            onError(exception.message ?: "Failed to update quantity")
        }
    }

    fun removeFromCart(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        cartItemsCollection(uid)
            .document(productId)
            .delete()
            .addOnSuccessListener {
                recalculateCartTotal(uid, onSuccess, onError)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to remove item from cart")
            }
    }

    fun clearCart(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        cartItemsCollection(uid)
            .get()
            .addOnSuccessListener { itemsSnapshot ->
                val batch = db.batch()

                itemsSnapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        cartDocument(uid)
                            .set(
                                mapOf(
                                    "cartId" to "current",
                                    "userId" to uid,
                                    "temporaryValue" to 0.0
                                ),
                                SetOptions.merge()
                            )
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { exception ->
                                onError(exception.message ?: "Failed to reset cart")
                            }
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Failed to clear cart items")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to load cart items")
            }
    }

    private fun recalculateCartTotal(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        cartItemsCollection(userId)
            .get()
            .addOnSuccessListener { itemsSnapshot ->
                val total = itemsSnapshot.documents.sumOf { document ->
                    val price = document.getDouble("price") ?: 0.0
                    val quantity = document.getLong("quantity")?.toInt() ?: 0
                    price * quantity
                }

                cartDocument(userId)
                    .set(
                        mapOf(
                            "cartId" to "current",
                            "userId" to userId,
                            "temporaryValue" to total
                        ),
                        SetOptions.merge()
                    )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Failed to update cart total")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to recalculate cart")
            }
    }
}