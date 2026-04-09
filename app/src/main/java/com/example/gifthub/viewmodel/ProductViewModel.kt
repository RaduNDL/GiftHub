package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.ProductDto
import com.example.gifthub.repositories.ProductRepository

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var productsList by mutableStateOf<List<ProductDto>>(emptyList())
        private set

    var selectedProduct by mutableStateOf<ProductDto?>(null)
        private set

    init {
        loadProducts()
    }

    fun loadProducts() {
        isLoading = true
        errorMessage = null

        repository.getAllProducts(
            onSuccess = { products ->
                productsList = products
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
        priceStr: String,
        stockStr: String,
        categoryIdStr: String,
        imageUrl: String,
        onSuccess: () -> Unit
    ) {
        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()
        val categoryId = categoryIdStr.trim()

        if (name.isBlank() || price == null || stock == null || imageUrl.isBlank() || categoryId.isBlank()) {
            errorMessage = "All fields are required."
            return
        }

        isLoading = true
        errorMessage = null

        val product = ProductDto(
            name = name.trim(),
            price = price,
            stock = stock,
            categoryId = categoryId,
            imageUrl = imageUrl.trim()
        )

        repository.addProduct(
            product = product,
            onSuccess = {
                isLoading = false
                loadProducts()
                onSuccess()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun updateProduct(
        productId: String,
        name: String,
        priceStr: String,
        stockStr: String,
        categoryIdStr: String,
        imageUrl: String,
        onSuccess: () -> Unit
    ) {
        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()
        val categoryId = categoryIdStr.trim()

        if (productId.isBlank() || name.isBlank() || price == null || stock == null || imageUrl.isBlank() || categoryId.isBlank()) {
            errorMessage = "All fields are required."
            return
        }

        isLoading = true
        errorMessage = null

        val updatedProduct = ProductDto(
            idProduct = productId,
            name = name.trim(),
            price = price,
            stock = stock,
            categoryId = categoryId,
            imageUrl = imageUrl.trim()
        )

        repository.updateProduct(
            product = updatedProduct,
            onSuccess = {
                isLoading = false
                loadProducts()
                selectedProduct = updatedProduct
                onSuccess()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun deleteProduct(productId: String) {
        if (productId.isBlank()) {
            errorMessage = "Invalid product ID."
            return
        }

        isLoading = true
        errorMessage = null

        repository.deleteProduct(
            productId = productId,
            onSuccess = {
                isLoading = false
                loadProducts()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun getProductDetails(productId: String) {
        if (productId.isBlank()) {
            errorMessage = "Invalid product ID."
            return
        }

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
}