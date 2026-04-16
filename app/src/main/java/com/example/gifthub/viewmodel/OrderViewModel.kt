package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.gifthub.models.OrderDto
import com.example.gifthub.models.ShoppingCartDto
import com.example.gifthub.screens.notifications.NotificationHelper
import com.example.gifthub.repositories.CartRepository
import com.example.gifthub.repositories.OrderRepository

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository = OrderRepository()
    private val cartRepository = CartRepository()

    var orders by mutableStateOf<List<OrderDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    fun placeOrder(
        cart: ShoppingCartDto,
        address: String,
        paymentMethod: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null
        orderRepository.placeOrder(
            cart = cart,
            address = address,
            paymentMethod = paymentMethod,
            onSuccess = { orderId ->
                NotificationHelper.notifyOrderPlaced(getApplication(), orderId)
                cartRepository.clearCart(
                    onSuccess = {
                        isLoading = false
                        userMessage = "Your order was placed successfully!"
                        onSuccess(orderId)
                    },
                    onError = { error ->
                        isLoading = false
                        onError(error)
                    }
                )
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
                onError(error)
            }
        )
    }

    fun loadOrders(isEmployee: Boolean) {
        isLoading = true
        errorMessage = null
        orderRepository.loadOrders(
            isEmployee = isEmployee,
            onSuccess = { loadedOrders ->
                orders = loadedOrders
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        isLoading = true
        errorMessage = null
        orderRepository.updateOrderStatus(
            orderId = orderId,
            newStatus = newStatus,
            onSuccess = {
                val updatedOrders = orders.map { if (it.orderId == orderId) it.copy(status = newStatus) else it }
                orders = updatedOrders
                userMessage = "Order status updated"
                NotificationHelper.notifyOrderStatusUpdated(getApplication(), orderId, newStatus)
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun cancelOrder(orderId: String) {
        isLoading = true
        errorMessage = null
        orderRepository.cancelOrder(
            orderId = orderId,
            onSuccess = {
                val updated = orders.map { if (it.orderId == orderId) it.copy(status = "Cancelled") else it }
                orders = updated
                userMessage = "Order has been cancelled"
                NotificationHelper.notifyOrderCancelled(getApplication(), orderId)
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearUserMessage() {
        userMessage = null
    }
}