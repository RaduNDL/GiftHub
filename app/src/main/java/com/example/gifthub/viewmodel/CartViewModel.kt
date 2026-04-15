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

    var cart by mutableStateOf(emptyCart())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    fun loadCart() {
        isLoading = true
        repository.getCart(
            onSuccess = {
                cart = it
                isLoading = false
            },
            onError = { error ->
                userMessage = error
                cart = emptyCart()
                isLoading = false
            }
        )
    }

    fun addToCart(product: ProductDto, quantity: Int = 1) {
        if (quantity <= 0) {
            userMessage = "Invalid quantity"
            return
        }
        if (product.idProduct.isBlank()) {
            userMessage = "Invalid product"
            return
        }

        isLoading = true
        repository.addToCart(
            product = product,
            quantityToAdd = quantity,
            onSuccess = {
                refreshCart("Product added to cart")
            },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        if (cartItemId.isBlank()) {
            userMessage = "Invalid cart item ID"
            return
        }

        isLoading = true
        repository.updateItemQuantity(
            cartItemId = cartItemId,
            newQuantity = newQuantity,
            onSuccess = { refreshCart(null) },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    fun removeFromCart(cartItemId: String) {
        if (cartItemId.isBlank()) {
            userMessage = "Invalid cart item ID"
            return
        }

        isLoading = true
        repository.removeFromCart(
            cartItemId = cartItemId,
            onSuccess = { refreshCart("Product removed from cart") },
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
                cart = emptyCart().copy(userId = cart.userId)
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

    private fun refreshCart(successMessage: String?) {
        repository.getCart(
            onSuccess = {
                cart = it
                if (successMessage != null) userMessage = successMessage
                isLoading = false
            },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    private fun emptyCart() = ShoppingCartDto(
        cartId = "current",
        userId = "",
        items = emptyList(),
        temporaryValue = 0.0
    )
}