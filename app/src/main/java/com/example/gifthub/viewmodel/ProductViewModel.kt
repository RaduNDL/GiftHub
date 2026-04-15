package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.ProductDto
import com.example.gifthub.repositories.ProductRepository

class ProductViewModel : ViewModel() {
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

    fun loadProductsByCategory(categoryId: String) {
        isLoading = true
        errorMessage = null
        repository.getProductsByCategory(
            categoryId = categoryId,
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
        isLoading = true
        errorMessage = null

        repository.getProductById(
            productId = productId,
            onSuccess = { product ->
                selectedProduct = product
                isLoading = false
            },
            onError = { error ->
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
            errorMessage = "Please fill all fields correctly"
            return
        }

        isLoading = true
        val product = ProductDto(
            idProduct = "",
            name = name,
            description = description,
            price = price,
            stock = stock,
            categoryId = categoryIdStr,
            imageUrl = imageUrl,
            active = true,
            customizable = false,
            customizationOptions = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        repository.addProduct(
            product = product,
            onSuccess = {
                isLoading = false
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

        if (name.isBlank() || price == null || stock == null || categoryIdStr.isBlank()) {
            errorMessage = "Please fill all fields correctly"
            return
        }

        isLoading = true
        val product = ProductDto(
            idProduct = productId,
            name = name,
            description = description,
            price = price,
            stock = stock,
            categoryId = categoryIdStr,
            imageUrl = imageUrl,
            active = true,
            customizable = false,
            customizationOptions = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        repository.updateProduct(
            product = product,
            onSuccess = {
                isLoading = false
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
        isLoading = true
        repository.deleteProduct(
            productId = productId,
            onSuccess = {
                isLoading = false
                loadProducts()
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }
}