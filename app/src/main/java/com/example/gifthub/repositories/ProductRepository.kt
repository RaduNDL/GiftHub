package com.example.gifthub.repositories

import com.example.gifthub.models.ProductDto
import com.google.firebase.firestore.FirebaseFirestore

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("products")

    fun getAllProducts(
        onSuccess: (List<ProductDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        collection.get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { doc ->
                    val rawCategoryId = doc.get("categoryId")
                    val categoryIdAsString = when (rawCategoryId) {
                        is String -> rawCategoryId
                        is Long -> rawCategoryId.toString()
                        is Int -> rawCategoryId.toString()
                        else -> ""
                    }

                    ProductDto(
                        idProduct = doc.id,
                        name = doc.getString("name") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        stock = (doc.getLong("stock") ?: 0L).toInt(),
                        categoryId = categoryIdAsString,
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                }
                onSuccess(products)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to load products")
            }
    }

    fun getProductById(
        productId: String,
        onSuccess: (ProductDto) -> Unit,
        onError: (String) -> Unit
    ) {
        collection.document(productId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val rawCategoryId = doc.get("categoryId")
                    val categoryIdAsString = when (rawCategoryId) {
                        is String -> rawCategoryId
                        is Long -> rawCategoryId.toString()
                        is Int -> rawCategoryId.toString()
                        else -> ""
                    }

                    val product = ProductDto(
                        idProduct = doc.id,
                        name = doc.getString("name") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        stock = (doc.getLong("stock") ?: 0L).toInt(),
                        categoryId = categoryIdAsString,
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                    onSuccess(product)
                } else {
                    onError("Product not found")
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to get product")
            }
    }

    fun addProduct(
        product: ProductDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = mapOf(
            "name" to product.name,
            "price" to product.price,
            "stock" to product.stock,
            "categoryId" to product.categoryId,
            "imageUrl" to product.imageUrl
        )

        collection.add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to add product")
            }
    }

    fun updateProduct(
        product: ProductDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (product.idProduct.isBlank()) {
            onError("Invalid product ID")
            return
        }

        val data = mapOf(
            "name" to product.name,
            "price" to product.price,
            "stock" to product.stock,
            "categoryId" to product.categoryId,
            "imageUrl" to product.imageUrl
        )

        collection.document(product.idProduct).set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to update product")
            }
    }

    fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        collection.document(productId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to delete product")
            }
    }
}