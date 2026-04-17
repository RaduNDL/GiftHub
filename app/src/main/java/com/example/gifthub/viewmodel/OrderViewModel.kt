package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.gifthub.models.OrderDto
import com.example.gifthub.models.ShoppingCartDto
import com.example.gifthub.repositories.NotificationRepository
import com.example.gifthub.repositories.OrderRepository
import com.example.gifthub.screens.notifications.NotificationHelper

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository = OrderRepository()
    private val notificationRepository = NotificationRepository()

    var orders by mutableStateOf<List<OrderDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    private var isPlacingOrder by mutableStateOf(false)

    fun placeOrder(
        cart: ShoppingCartDto,
        address: String,
        paymentMethod: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isPlacingOrder) return
        isPlacingOrder = true
        isLoading = true
        errorMessage = null

        orderRepository.placeOrder(
            cart = cart,
            address = address,
            paymentMethod = paymentMethod,
            onSuccess = { orderId ->
                NotificationHelper.notifyOrderPlaced(getApplication(), orderId)
                notificationRepository.createOrderNotification(
                    title = "Order placed",
                    message = "Your order #$orderId was placed successfully",
                    orderId = orderId,
                    type = "order_placed"
                )
                userMessage = "Your order was placed successfully!"
                isLoading = false
                isPlacingOrder = false
                onSuccess(orderId)
            },
            onError = { error ->
                isLoading = false
                isPlacingOrder = false
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

    fun updateOrderStatus(userId: String, orderId: String, newStatus: String) {
        isLoading = true
        errorMessage = null
        orderRepository.updateOrderStatus(
            userId = userId,
            orderId = orderId,
            newStatus = newStatus,
            onSuccess = { updatedOrder ->
                orders = orders.map {
                    if (it.orderId == orderId) it.copy(status = newStatus, updatedAt = updatedOrder.updatedAt) else it
                }
                userMessage = "Order status updated"
                NotificationHelper.notifyOrderStatusUpdated(getApplication(), orderId, newStatus)
                notificationRepository.createOrderNotification(
                    userId = updatedOrder.userId,
                    title = "Order status updated",
                    message = "Order #$orderId is now $newStatus",
                    orderId = orderId,
                    type = "order_status"
                )
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
                orders = orders.map { if (it.orderId == orderId) it.copy(status = "Cancelled") else it }
                userMessage = "Order has been cancelled"
                NotificationHelper.notifyOrderCancelled(getApplication(), orderId)
                notificationRepository.createOrderNotification(
                    title = "Order cancelled",
                    message = "Order #$orderId was cancelled",
                    orderId = orderId,
                    type = "order_cancelled"
                )
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