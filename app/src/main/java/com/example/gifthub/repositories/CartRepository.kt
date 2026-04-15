package com.example.gifthub.repositories

import com.example.gifthub.models.CartItemDto
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.SelectedCustomizationDto
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
                        val selectedCustomizations = (doc.get("selectedCustomizations") as? List<*>)
                            ?.mapNotNull { item ->
                                if (item is Map<*, *>) {
                                    @Suppress("UNCHECKED_CAST")
                                    val map = item as Map<String, Any?>
                                    SelectedCustomizationDto(
                                        optionId = map["optionId"] as? String ?: "",
                                        optionName = map["optionName"] as? String ?: "",
                                        selectedValueIds = (map["selectedValueIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                        selectedLabels = (map["selectedLabels"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                        uploadedImageUrl = map["uploadedImageUrl"] as? String ?: "",
                                        textInput = map["textInput"] as? String ?: "",
                                        extraPriceTotal = (map["extraPriceTotal"] as? Number)?.toDouble() ?: 0.0
                                    )
                                } else null
                            }
                            ?: emptyList()

                        CartItemDto(
                            cartItemId = doc.id,
                            productId = doc.getString("productId") ?: "",
                            name = doc.getString("productName") ?: "",
                            imageUrl = doc.getString("productImage") ?: "",
                            price = doc.getDouble("basePrice") ?: 0.0,
                            quantity = (doc.getLong("quantity") ?: 1L).toInt(),
                            customText = "",
                            customColor = "",
                            lineExtraPrice = doc.getDouble("lineExtraPrice") ?: 0.0,
                            lineTotalPrice = doc.getDouble("lineTotalPrice") ?: 0.0,
                            customizationsHash = doc.getString("customizationsHash") ?: "",
                            selectedCustomizations = selectedCustomizations
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
        selections: List<SelectedCustomizationDto>,
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
        selections: List<SelectedCustomizationDto>,
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
                    val item = hashMapOf(
                        "id" to docId,
                        "userId" to uid,
                        "productId" to product.idProduct,
                        "productName" to if (selections.isEmpty()) product.name else "${product.name} (Custom)",
                        "productImage" to product.imageUrl,
                        "basePrice" to product.price,
                        "quantity" to safeQty,
                        "selectedCustomizations" to selections.map { it.toMap() },
                        "customizationsHash" to hash,
                        "lineExtraPrice" to extraPerUnit,
                        "lineTotalPrice" to (product.price + extraPerUnit) * safeQty,
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
                val extraPrice = doc.getDouble("lineExtraPrice") ?: 0.0
                val pricePerUnit = basePrice + (extraPrice / (doc.getLong("quantity") ?: 1L).toInt())
                val newTotal = pricePerUnit * newQuantity

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

    private fun buildHash(selections: List<SelectedCustomizationDto>): String {
        if (selections.isEmpty()) return ""
        return selections
            .sortedBy { it.optionId }
            .joinToString("_") { sel ->
                sel.selectedValueIds.sorted().joinToString("-")
            }
            .hashCode()
            .toString()
            .take(8)
    }
}

private fun SelectedCustomizationDto.toMap(): Map<String, Any> {
    return mapOf(
        "optionId" to optionId,
        "optionName" to optionName,
        "selectedValueIds" to selectedValueIds,
        "selectedLabels" to selectedLabels,
        "uploadedImageUrl" to uploadedImageUrl,
        "textInput" to textInput,
        "extraPriceTotal" to extraPriceTotal
    )
}