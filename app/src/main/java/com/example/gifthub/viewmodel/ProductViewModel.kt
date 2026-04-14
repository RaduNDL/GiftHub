package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.ProductDto
import com.example.gifthub.repositories.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val isLoading: Boolean = false,
    val selectedProduct: ProductDto? = null,
    val error: String? = null
)

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            isLoading = true

            repository.getProductById(
                productId = productId,
                onSuccess = { product ->
                    selectedProduct = product
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedProduct = product,
                        error = null
                    )
                    isLoading = false
                },
                onError = { error ->
                    errorMessage = error
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                    isLoading = false
                }
            )
        }
    }

    fun getProductDetails(productId: String) {
        loadProductById(productId)
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

        if (name.isBlank() || price == null || stock == null || categoryIdStr.isBlank()) {
            errorMessage = "Please fill all fields correctly"
            return
        }

        isLoading = true
        val product = ProductDto(
            idProduct = "",
            name = name,
            price = price,
            stock = stock,
            categoryId = categoryIdStr,
            imageUrl = imageUrl
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
            price = price,
            stock = stock,
            categoryId = categoryIdStr,
            imageUrl = imageUrl
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