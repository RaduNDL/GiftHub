package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.gifthub.models.ProductDto
import com.example.gifthub.repositories.ProductRepository
import com.example.gifthub.screens.notifications.NotificationHelper

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository()

    val productsList = mutableStateListOf<ProductDto>()
    var selectedProduct by mutableStateOf<ProductDto?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadProducts() {
        isLoading = true
        errorMessage = null
        repository.getAllProducts(
            onSuccess = { products ->
                productsList.clear()
                productsList.addAll(products)
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun loadProductById(productId: String) {
        if (productId.isBlank()) {
            selectedProduct = null
            errorMessage = "Invalid product ID"
            return
        }

        isLoading = true
        errorMessage = null

        repository.getProductById(
            productId = productId,
            onSuccess = { product ->
                selectedProduct = product
                if (product == null) errorMessage = "Product not found"
                isLoading = false
            },
            onError = { error ->
                selectedProduct = null
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun addProduct(
        name: String,
        description: String,
        priceStr: String,
        stockStr: String,
        categoryIdStr: String,
        imageUrl: String,
        onSuccess: () -> Unit
    ) {
        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()

        if (name.isBlank() || price == null || stock == null || categoryIdStr.isBlank()) {
            errorMessage = "Fill in all fields correctly"
            return
        }

        isLoading = true
        errorMessage = null

        val now = System.currentTimeMillis()
        val product = ProductDto(
            idProduct = "",
            name = name.trim(),
            description = description.trim(),
            price = price,
            stock = stock,
            categoryId = categoryIdStr.trim(),
            imageUrl = imageUrl.trim(),
            active = true,
            createdAt = now,
            updatedAt = now
        )

        repository.addProduct(
            product = product,
            onSuccess = {
                isLoading = false
                NotificationHelper.notifyProductAdded(getApplication(), product.name)
                loadProducts()
                onSuccess()
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun updateProduct(
        productId: String,
        name: String,
        description: String,
        priceStr: String,
        stockStr: String,
        categoryIdStr: String,
        imageUrl: String,
        onSuccess: () -> Unit
    ) {
        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()

        if (productId.isBlank() || name.isBlank() || price == null || stock == null || categoryIdStr.isBlank()) {
            errorMessage = "Fill in all fields correctly"
            return
        }

        isLoading = true
        errorMessage = null

        val createdAtValue = selectedProduct?.createdAt ?: System.currentTimeMillis()
        val product = ProductDto(
            idProduct = productId,
            name = name.trim(),
            description = description.trim(),
            price = price,
            stock = stock,
            categoryId = categoryIdStr.trim(),
            imageUrl = imageUrl.trim(),
            active = selectedProduct?.active ?: true,
            createdAt = createdAtValue,
            updatedAt = System.currentTimeMillis()
        )

        repository.updateProduct(
            product = product,
            onSuccess = {
                isLoading = false
                NotificationHelper.notifyProductUpdated(getApplication(), product.name)
                loadProducts()
                onSuccess()
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun deleteProduct(productId: String) {
        if (productId.isBlank()) {
            errorMessage = "Invalid product ID"
            return
        }

        val productName = productsList.firstOrNull { it.idProduct == productId }?.name
            ?: selectedProduct?.name
            ?: "Product"

        isLoading = true
        errorMessage = null

        repository.deleteProduct(
            productId = productId,
            onSuccess = {
                isLoading = false
                NotificationHelper.notifyProductDeleted(getApplication(), productName)
                loadProducts()
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }
}