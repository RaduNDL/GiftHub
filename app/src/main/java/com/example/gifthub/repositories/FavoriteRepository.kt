package com.example.gifthub.repositories

import com.example.gifthub.models.ProductDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ToggleFavoriteResult(
    val success: Boolean,
    val isFavorite: Boolean? = null,
    val errorMessage: String? = null
)

class FavoriteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private fun favoritesCollection(userId: String) =
        usersCollection.document(userId).collection("favorites")

    suspend fun getFavoriteProductIds(userId: String): Set<String> {
        return try {
            favoritesCollection(userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("productId") }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun toggleFavorite(userId: String, product: ProductDto): ToggleFavoriteResult {
        return try {
            val productId = product.idProduct

            if (productId.isBlank()) {
                return ToggleFavoriteResult(success = false, errorMessage = "Invalid product id.")
            }

            val favoriteDoc = favoritesCollection(userId).document(productId)
            val snapshot = favoriteDoc.get().await()

            if (snapshot.exists()) {
                favoriteDoc.delete().await()
                ToggleFavoriteResult(success = true, isFavorite = false)
            } else {
                val favoriteData = hashMapOf(
                    "productId" to productId,
                    "idProduct" to product.idProduct,
                    "name" to product.name,
                    "price" to product.price,
                    "imageUrl" to product.imageUrl,
                    "categoryId" to product.categoryId,
                    "stock" to product.stock,
                    "description" to (product.description ?: ""),
                    "addedAt" to System.currentTimeMillis()
                )

                favoriteDoc.set(favoriteData).await()
                ToggleFavoriteResult(success = true, isFavorite = true)
            }
        } catch (e: Exception) {
            ToggleFavoriteResult(success = false, errorMessage = e.message ?: "Error")
        }
    }

    suspend fun removeFromFavorites(userId: String, productId: String): Boolean {
        return try {
            favoritesCollection(userId).document(productId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFavoriteProducts(userId: String): List<ProductDto> {
        return try {
            val snapshots = favoritesCollection(userId).get().await().documents

            snapshots.sortedByDescending { it.getLong("addedAt") ?: 0L }
                .mapNotNull { document ->
                    try {
                        ProductDto(
                            idProduct = document.getString("idProduct") ?: document.id,
                            name = document.getString("name") ?: "",
                            price = document.getDouble("price") ?: 0.0,
                            imageUrl = document.getString("imageUrl") ?: "",
                            categoryId = document.getString("categoryId") ?: "",
                            stock = (document.getLong("stock") ?: 0L).toInt(),
                            description = document.getString("description") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}