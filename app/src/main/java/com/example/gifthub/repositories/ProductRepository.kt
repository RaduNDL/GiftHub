package com.example.gifthub.repositories

import com.example.gifthub.models.NotificationDto
import com.example.gifthub.models.ProductDto
import com.example.gifthub.navigation.GiftHubDestinations
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("products")

    fun getAllProducts(onSuccess: (List<ProductDto>) -> Unit, onError: (String) -> Unit) {
        collection.get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { doc ->
                    try {
                        val dto = doc.toObject(ProductDto::class.java) ?: return@mapNotNull null
                        dto.copy(idProduct = if (dto.idProduct.isBlank()) doc.id else dto.idProduct)
                    } catch (_: Exception) {
                        null
                    }
                }
                onSuccess(products)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load products") }
    }

    fun getProductsByCategory(categoryId: String, onSuccess: (List<ProductDto>) -> Unit, onError: (String) -> Unit) {
        collection.whereEqualTo("categoryId", categoryId).get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { doc ->
                    try {
                        val dto = doc.toObject(ProductDto::class.java) ?: return@mapNotNull null
                        dto.copy(idProduct = if (dto.idProduct.isBlank()) doc.id else dto.idProduct)
                    } catch (_: Exception) {
                        null
                    }
                }
                onSuccess(products)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load products") }
    }

    fun getProductById(productId: String, onSuccess: (ProductDto?) -> Unit, onError: (String) -> Unit) {
        if (productId.isBlank()) {
            onError("Invalid product id")
            return
        }

        collection.document(productId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }

                val dto = doc.toObject(ProductDto::class.java)
                if (dto == null) {
                    onError("Product data is invalid")
                    return@addOnSuccessListener
                }

                onSuccess(dto.copy(idProduct = if (dto.idProduct.isBlank()) doc.id else dto.idProduct))
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load product") }
    }

    fun addProduct(product: ProductDto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val docRef = if (product.idProduct.isBlank()) collection.document() else collection.document(product.idProduct)
        val productWithId = product.copy(idProduct = docRef.id)

        docRef.set(productWithId)
            .addOnSuccessListener {
                broadcastInAppNotificationToAllUsers(
                    title = "New product added",
                    message = "${productWithId.name} is now available",
                    type = "product_added",
                    targetRoute = GiftHubDestinations.PRODUCTS
                )
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Failed to add product") }
    }

    fun updateProduct(product: ProductDto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (product.idProduct.isBlank()) {
            onError("Invalid product id")
            return
        }

        collection.document(product.idProduct)
            .set(product)
            .addOnSuccessListener {
                broadcastInAppNotificationToAllUsers(
                    title = "Product updated",
                    message = "${product.name} was updated",
                    type = "product_update",
                    targetRoute = GiftHubDestinations.PRODUCTS
                )
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Failed to update product") }
    }

    fun deleteProduct(productId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (productId.isBlank()) {
            onError("Invalid product id")
            return
        }

        collection.document(productId)
            .delete()
            .addOnSuccessListener {
                broadcastInAppNotificationToAllUsers(
                    title = "Product removed",
                    message = "A product was removed",
                    type = "product_deleted",
                    targetRoute = GiftHubDestinations.PRODUCTS
                )
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Failed to delete product") }
    }

    private fun broadcastInAppNotificationToAllUsers(
        title: String,
        message: String,
        type: String,
        targetRoute: String
    ) {
        firestore.collection("users").get()
            .addOnSuccessListener { usersSnapshot ->
                if (usersSnapshot.isEmpty) return@addOnSuccessListener

                usersSnapshot.documents.chunked(400).forEach { chunk ->
                    val batch: WriteBatch = firestore.batch()

                    chunk.forEach { userDoc ->
                        val userId = userDoc.id
                        val notifRef = firestore.collection("users")
                            .document(userId)
                            .collection("notifications")
                            .document()

                        val payload = NotificationDto(
                            notificationID = notifRef.id,
                            userId = userId,
                            title = title,
                            message = message,
                            createdDate = System.currentTimeMillis(),
                            markedAsRead = false,
                            type = type,
                            targetRoute = targetRoute,
                            orderId = ""
                        )

                        batch.set(notifRef, payload)
                    }

                    batch.commit()
                }
            }
    }
}