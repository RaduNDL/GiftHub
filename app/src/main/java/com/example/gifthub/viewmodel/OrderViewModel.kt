package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.OrderDto
import com.example.gifthub.models.ShoppingCartDto
import com.example.gifthub.repositories.OrderRepository

class OrderViewModel : ViewModel() {

    private val orderRepository = OrderRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    var orders by mutableStateOf<List<OrderDto>>(emptyList())
        private set

    fun clearError() {
        errorMessage = null
    }

    fun clearUserMessage() {
        userMessage = null
    }

    fun placeOrder(
        cart: ShoppingCartDto,
        address: String,
        paymentMethod: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (address.isBlank()) {
            errorMessage = "Please enter or select a delivery address."
            onError(errorMessage!!)
            return
        }

        if (paymentMethod.isBlank()) {
            errorMessage = "Please enter or select a payment method."
            onError(errorMessage!!)
            return
        }

        if (cart.items.isEmpty()) {
            errorMessage = "Your cart is empty."
            onError(errorMessage!!)
            return
        }

        isLoading = true
        errorMessage = null
        userMessage = null

        val order = OrderDto(
            items = cart.items,
            totalAmount = cart.temporaryValue,
            address = address.trim(),
            paymentMethod = paymentMethod.trim(),
            status = "Pending"
        )

        orderRepository.placeOrder(
            order = order,
            onSuccess = { orderId ->
                isLoading = false
                userMessage = "Order #${orderId.take(8).uppercase()} placed successfully."
                onSuccess(orderId)
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

        if (isEmployee) {
            orderRepository.getAllOrders(
                onSuccess = { result ->
                    orders = result
                    isLoading = false
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        } else {
            orderRepository.getOrdersForCurrentUser(
                onSuccess = { result ->
                    orders = result
                    isLoading = false
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        errorMessage = null

        orderRepository.updateOrderStatus(
            orderId = orderId,
            newStatus = newStatus,
            onSuccess = {
                orders = orders.map { order ->
                    if (order.orderId == orderId) {
                        order.copy(status = newStatus)
                    } else {
                        order
                    }
                }
                userMessage = "Order status updated."
            },
            onError = { error ->
                errorMessage = error
            }
        )
    }
}