package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.ShoppingCartDto
import com.example.gifthub.repositories.CartRepository

class CartViewModel : ViewModel() {

    private val repository = CartRepository()

    var cart by mutableStateOf(
        ShoppingCartDto(
            cartId = "current",
            userId = "",
            items = emptyList(),
            temporaryValue = 0.0
        )
    )
        private set

    var isLoading by mutableStateOf(false)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    fun loadCart() {
        isLoading = true
        repository.getCart(
            onSuccess = { loadedCart ->
                cart = loadedCart
                isLoading = false
            },
            onError = { error ->
                userMessage = error
                cart = ShoppingCartDto(
                    cartId = "current",
                    userId = "",
                    items = emptyList(),
                    temporaryValue = 0.0
                )
                isLoading = false
            }
        )
    }

    fun addToCart(product: ProductDto, quantity: Int = 1) {
        if (quantity <= 0) {
            userMessage = "Invalid quantity"
            return
        }
        isLoading = true
        repository.addToCart(
            product = product,
            quantityToAdd = quantity,
            onSuccess = {
                repository.getCart(
                    onSuccess = { loadedCart ->
                        cart = loadedCart
                        userMessage = "Product added to cart"
                        isLoading = false
                    },
                    onError = { error ->
                        userMessage = error
                        isLoading = false
                    }
                )
            },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    // ✅ COMPLETE IMPLEMENTATION - was incomplete before
    fun addCustomizedToCart(
        product: ProductDto,
        quantity: Int,
        customText: String,
        customColor: String
    ) {
        if (quantity <= 0) {
            userMessage = "Invalid quantity"
            return
        }

        if (product.idProduct.isBlank()) {
            userMessage = "Invalid product"
            return
        }

        isLoading = true
        repository.addCustomizedToCart(
            product = product,
            quantityToAdd = quantity,
            customText = customText,
            customColor = customColor,
            onSuccess = {
                // ✅ Reload cart after successful add
                repository.getCart(
                    onSuccess = { loadedCart ->
                        cart = loadedCart
                        userMessage = "Personalized product added to cart ✓"
                        isLoading = false
                    },
                    onError = { error ->
                        userMessage = "Added to cart but failed to refresh: $error"
                        isLoading = false
                    }
                )
            },
            onError = { error ->
                userMessage = "Error adding to cart: $error"
                isLoading = false
            }
        )
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        if (productId.isBlank()) {
            userMessage = "Invalid product ID"
            return
        }

        isLoading = true
        repository.updateItemQuantity(
            productId = productId,
            newQuantity = newQuantity,
            onSuccess = {
                repository.getCart(
                    onSuccess = { loadedCart ->
                        cart = loadedCart
                        isLoading = false
                    },
                    onError = { error ->
                        userMessage = error
                        isLoading = false
                    }
                )
            },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    fun removeFromCart(productId: String) {
        if (productId.isBlank()) {
            userMessage = "Invalid product ID"
            return
        }

        isLoading = true
        repository.removeFromCart(
            productId = productId,
            onSuccess = {
                repository.getCart(
                    onSuccess = { loadedCart ->
                        cart = loadedCart
                        userMessage = "Product removed from cart"
                        isLoading = false
                    },
                    onError = { error ->
                        userMessage = error
                        isLoading = false
                    }
                )
            },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    fun clearCart() {
        isLoading = true
        repository.clearCart(
            onSuccess = {
                cart = ShoppingCartDto(
                    cartId = "current",
                    userId = cart.userId,
                    items = emptyList(),
                    temporaryValue = 0.0
                )
                userMessage = "Cart cleared"
                isLoading = false
            },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    fun clearUserMessage() {
        userMessage = null
    }
}