package com.example.gifthub.repositories

import com.example.gifthub.models.CartItem
import com.example.gifthub.models.CartItemDto
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.SelectedCustomization
import com.example.gifthub.models.ShoppingCartDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun cartItemsRef(uid: String) =
        firestore.collection("users").document(uid)
            .collection("shoppingCart").document("current")
            .collection("items")

    fun getCart(onSuccess: (ShoppingCartDto) -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")

        cartItemsRef(uid).get()
            .addOnSuccessListener { snapshot ->
                var total = 0.0
                val items = snapshot.documents.mapNotNull { doc ->
                    val cartItem = doc.toObject(CartItem::class.java) ?: return@mapNotNull null
                    total += cartItem.lineTotalPrice
                    cartItem.toDto(doc.id)
                }
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
        addItemInternal(
            product = product,
            quantityToAdd = quantityToAdd,
            selections = emptyList(),
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun addCustomizedToCart(
        product: ProductDto,
        quantityToAdd: Int,
        selections: List<SelectedCustomization>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        addItemInternal(
            product = product,
            quantityToAdd = quantityToAdd,
            selections = selections,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun addItemInternal(
        product: ProductDto,
        quantityToAdd: Int,
        selections: List<SelectedCustomization>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not authenticated")
        if (product.idProduct.isBlank()) return onError("Invalid product")
        if (quantityToAdd <= 0) return onError("Invalid quantity")

        val safeQty = quantityToAdd.coerceAtLeast(1)
        val extraPerUnit = selections.sumOf { it.extraPriceTotal }
        val hash = buildHash(selections)
        val docId = if (selections.isEmpty()) product.idProduct else "${product.idProduct}_$hash"

        val itemRef = cartItemsRef(uid).document(docId)

        itemRef.get()
            .addOnSuccessListener { doc ->
                val now = System.currentTimeMillis()
                if (doc.exists()) {
                    val oldQty = (doc.getLong("quantity") ?: 0L).toInt()
                    val newQty = oldQty + safeQty
                    val newTotal = (product.price + extraPerUnit) * newQty
                    itemRef.update(
                        mapOf(
                            "quantity" to newQty,
                            "lineExtraPrice" to extraPerUnit,
                            "lineTotalPrice" to newTotal,
                            "updatedAt" to now
                        )
                    )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Error updating item") }
                } else {
                    val item = CartItem(
                        id = docId,
                        userId = uid,
                        productId = product.idProduct,
                        productName = if (selections.isEmpty()) product.name else "${product.name} (Customized)",
                        productImage = product.imageUrl,
                        basePrice = product.price,
                        quantity = safeQty,
                        selectedCustomizations = selections,
                        customizationsHash = hash,
                        lineExtraPrice = extraPerUnit,
                        lineTotalPrice = (product.price + extraPerUnit) * safeQty,
                        createdAt = now,
                        updatedAt = now
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

        if (newQuantity <= 0) {
            removeFromCart(cartItemId, onSuccess, onError)
            return
        }

        val itemRef = cartItemsRef(uid).document(cartItemId)
        itemRef.get()
            .addOnSuccessListener { doc ->
                val existing = doc.toObject(CartItem::class.java)
                if (existing == null) {
                    onError("Cart item not found")
                    return@addOnSuccessListener
                }
                val newTotal = (existing.basePrice + existing.lineExtraPrice) * newQuantity
                itemRef.update(
                    mapOf(
                        "quantity" to newQuantity,
                        "lineTotalPrice" to newTotal,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error updating quantity") }
            }
            .addOnFailureListener { onError(it.message ?: "Error reading cart item") }
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
                if (snapshot.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error clearing cart") }
            }
            .addOnFailureListener { onError(it.message ?: "Error reading cart") }
    }

    private fun buildHash(selections: List<SelectedCustomization>): String {
        if (selections.isEmpty()) return ""
        return selections
            .sortedBy { it.optionId }
            .joinToString("|") { s ->
                s.optionId + ":" + s.selectedValueIds.sorted().joinToString(",")
            }
    }

    private fun CartItem.toDto(docId: String): CartItemDto {
        val text = if (selectedCustomizations.isEmpty()) ""
        else selectedCustomizations.joinToString("; ") { sel ->
            "${sel.optionName}: ${sel.selectedLabels.joinToString(", ")}"
        }
        return CartItemDto(
            cartItemId = docId,
            productId = productId,
            name = productName,
            imageUrl = productImage,
            price = basePrice,
            quantity = quantity,
            customText = text,
            customColor = "",
            lineExtraPrice = lineExtraPrice,
            lineTotalPrice = lineTotalPrice,
            customizationsHash = customizationsHash,
            selectedCustomizations = selectedCustomizations
        )
    }
}