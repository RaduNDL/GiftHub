package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.ShoppingCartDto
import com.example.gifthub.repositories.CartRepository
import com.example.gifthub.repositories.NotificationRepository
import com.example.gifthub.screens.notifications.NotificationHelper

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CartRepository()
    private val notificationRepository = NotificationRepository()

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
                NotificationHelper.notifyCartAdded(getApplication(), product.name)
                notificationRepository.createCartNotification(
                    title = "Added to cart",
                    message = "${product.name} was added to cart",
                    type = "cart_added"
                )
                refreshCart("${product.name} added to cart")
            },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        if (cartItemId.isBlank()) {
            userMessage = "Invalid ID"
            return
        }

        isLoading = true
        repository.updateItemQuantity(
            cartItemId = cartItemId,
            newQuantity = newQuantity,
            onSuccess = {
                NotificationHelper.notifyCartQuantityUpdated(getApplication())
                notificationRepository.createCartNotification(
                    title = "Cart updated",
                    message = "Cart quantity updated",
                    type = "cart_update"
                )
                refreshCart(null)
            },
            onError = { error ->
                userMessage = error
                isLoading = false
            }
        )
    }

    fun removeFromCart(cartItemId: String) {
        if (cartItemId.isBlank()) {
            userMessage = "Invalid ID"
            return
        }

        val itemName = cart.items.firstOrNull { it.cartItemId == cartItemId }?.name ?: "Product"

        isLoading = true
        repository.removeFromCart(
            cartItemId = cartItemId,
            onSuccess = {
                NotificationHelper.notifyCartRemoved(getApplication(), itemName)
                notificationRepository.createCartNotification(
                    title = "Removed from cart",
                    message = "$itemName was removed from cart",
                    type = "cart_removed"
                )
                refreshCart("$itemName removed from cart")
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
                cart = emptyCart().copy(userId = cart.userId)
                userMessage = "Cart has been cleared"
                NotificationHelper.notifyCartCleared(getApplication())
                notificationRepository.createCartNotification(
                    title = "Cart cleared",
                    message = "All items were removed from cart",
                    type = "cart_update"
                )
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