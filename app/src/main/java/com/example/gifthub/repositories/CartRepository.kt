package com.example.gifthub.repositories

import com.example.gifthub.models.CartItem
import com.example.gifthub.models.CartItemDto
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.ShoppingCartDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCart(onSuccess: (ShoppingCartDto) -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        firestore.collection("users").document(uid)
            .collection("shoppingCart").document("current")
            .collection("items")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.mapNotNull { doc ->
                    val cartItem = doc.toObject(CartItem::class.java)
                    cartItem?.let {
                        CartItemDto(
                            productId = it.productId,
                            name = it.productName,
                            imageUrl = it.productImage,
                            price = it.basePrice,
                            quantity = it.quantity,
                            customText = "",
                            customColor = ""
                        )
                    }
                }
                var total = 0.0
                snapshot.documents.forEach { doc ->
                    val cartItem = doc.toObject(CartItem::class.java)
                    if (cartItem != null) {
                        total += cartItem.lineTotalPrice
                    }
                }
                onSuccess(ShoppingCartDto(cartId = "current", userId = uid, items = items, temporaryValue = total))
            }
            .addOnFailureListener {
                onError(it.message ?: "Error fetching cart")
            }
    }

    fun addToCart(product: ProductDto, quantityToAdd: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        val itemRef = firestore.collection("users").document(uid)
            .collection("shoppingCart").document("current")
            .collection("items").document(product.idProduct)

        itemRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val existingItem = doc.toObject(CartItem::class.java)
                if (existingItem != null) {
                    val newQuantity = existingItem.quantity + quantityToAdd
                    val newTotal = existingItem.basePrice * newQuantity
                    itemRef.update(
                        "quantity", newQuantity,
                        "lineTotalPrice", newTotal,
                        "updatedAt", System.currentTimeMillis()
                    ).addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it.message ?: "Error") }
                }
            } else {
                val cartItem = CartItem(
                    id = product.idProduct,
                    userId = uid,
                    productId = product.idProduct,
                    productName = product.name,
                    productImage = product.imageUrl,
                    basePrice = product.price,
                    quantity = quantityToAdd,
                    selectedCustomizations = emptyList(),
                    customizationsHash = "",
                    lineExtraPrice = 0.0,
                    lineTotalPrice = product.price * quantityToAdd,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                itemRef.set(cartItem)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error") }
            }
        }.addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun addCustomizedToCart(product: ProductDto, quantityToAdd: Int, customText: String, customColor: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        val hash = "$customText-$customColor"
        val uniqueId = "${product.idProduct}-${hash.hashCode()}"

        val itemRef = firestore.collection("users").document(uid)
            .collection("shoppingCart").document("current")
            .collection("items").document(uniqueId)

        val cartItem = CartItem(
            id = uniqueId,
            userId = uid,
            productId = product.idProduct,
            productName = "${product.name} (Customized)",
            productImage = product.imageUrl,
            basePrice = product.price,
            quantity = quantityToAdd,
            selectedCustomizations = emptyList(),
            customizationsHash = hash,
            lineExtraPrice = 0.0,
            lineTotalPrice = product.price * quantityToAdd,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        itemRef.set(cartItem)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun updateItemQuantity(productId: String, newQuantity: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        if (newQuantity <= 0) {
            removeFromCart(productId, onSuccess, onError)
            return
        }

        val itemRef = firestore.collection("users").document(uid)
            .collection("shoppingCart").document("current")
            .collection("items").document(productId)

        itemRef.get().addOnSuccessListener { doc ->
            val existingItem = doc.toObject(CartItem::class.java)
            if (existingItem != null) {
                val newTotal = existingItem.basePrice * newQuantity
                itemRef.update(
                    "quantity", newQuantity,
                    "lineTotalPrice", newTotal,
                    "updatedAt", System.currentTimeMillis()
                ).addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it.message ?: "Error") }
            }
        }.addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun removeFromCart(productId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        firestore.collection("users").document(uid)
            .collection("shoppingCart").document("current")
            .collection("items").document(productId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun clearCart(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        val itemsRef = firestore.collection("users").document(uid)
            .collection("shoppingCart").document("current")
            .collection("items")

        itemsRef.get().addOnSuccessListener { snapshot ->
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it.message ?: "Error") }
        }.addOnFailureListener { onError(it.message ?: "Error") }
    }
}