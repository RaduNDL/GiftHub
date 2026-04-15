package com.example.gifthub.repositories

import com.example.gifthub.models.ProductDto
import com.google.firebase.firestore.FirebaseFirestore

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("products")

    fun getAllProducts(onSuccess: (List<ProductDto>) -> Unit, onError: (String) -> Unit) {
        collection.get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { it.toObject(ProductDto::class.java) }
                onSuccess(products)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load products") }
    }

    fun getProductsByCategory(categoryId: String, onSuccess: (List<ProductDto>) -> Unit, onError: (String) -> Unit) {
        collection.whereEqualTo("categoryId", categoryId).get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { it.toObject(ProductDto::class.java) }
                onSuccess(products)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load products") }
    }

    fun getProductById(productId: String, onSuccess: (ProductDto?) -> Unit, onError: (String) -> Unit) {
        collection.document(productId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess(document.toObject(ProductDto::class.java))
                } else {
                    onError("Product not found")
                }
            }
            .addOnFailureListener { onError(it.message ?: "Failed to fetch product") }
    }

    fun addProduct(product: ProductDto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val documentRef = collection.document()
        val productToSave = product.copy(idProduct = documentRef.id)

        documentRef.set(productToSave)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to add product") }
    }

    fun updateProduct(product: ProductDto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        collection.document(product.idProduct).set(product)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to update product") }
    }

    fun deleteProduct(productId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        collection.document(productId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to delete product") }
    }
}