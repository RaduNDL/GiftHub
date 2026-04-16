package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.gifthub.models.PaymentMethodDto
import com.example.gifthub.repositories.NotificationRepository
import com.example.gifthub.repositories.PaymentMethodRepository
import com.example.gifthub.screens.notifications.NotificationHelper

class PaymentMethodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PaymentMethodRepository()
    private val notificationRepository = NotificationRepository()

    var paymentMethods by mutableStateOf<List<PaymentMethodDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    fun loadPaymentMethods() {
        isLoading = true
        errorMessage = null
        repository.getPaymentMethods(
            onSuccess = { methods ->
                paymentMethods = methods
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun savePaymentMethod(transactionId: String, method: String, paymentStatus: String) {
        if (method.isBlank()) {
            userMessage = "Method name cannot be empty"
            return
        }

        isLoading = true
        if (transactionId.isEmpty()) {
            repository.addPaymentMethod(
                method = method,
                paymentStatus = paymentStatus,
                onSuccess = {
                    userMessage = "Payment method added"
                    NotificationHelper.notifyPaymentAdded(getApplication(), method)
                    notificationRepository.createPaymentNotification(
                        title = "Payment method added",
                        message = "$method was added",
                        type = "payment_added"
                    )
                    loadPaymentMethods()
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        } else {
            val updateDto = PaymentMethodDto(
                transactionId = transactionId,
                orderID = "",
                method = method,
                paymentStatus = paymentStatus
            )
            repository.updatePaymentMethod(
                paymentMethod = updateDto,
                onSuccess = {
                    userMessage = "Payment method updated"
                    NotificationHelper.notifyPaymentUpdated(getApplication(), method)
                    notificationRepository.createPaymentNotification(
                        title = "Payment method updated",
                        message = "$method was updated",
                        type = "payment_update"
                    )
                    loadPaymentMethods()
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        }
    }

    fun deletePaymentMethod(transactionId: String) {
        isLoading = true
        val label = paymentMethods.firstOrNull { it.transactionId == transactionId }?.method ?: "Payment method"
        repository.deletePaymentMethod(
            transactionId = transactionId,
            onSuccess = {
                userMessage = "Payment method deleted"
                NotificationHelper.notifyPaymentDeleted(getApplication(), label)
                notificationRepository.createPaymentNotification(
                    title = "Payment method deleted",
                    message = "$label was removed",
                    type = "payment_deleted"
                )
                loadPaymentMethods()
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun clearUserMessage() {
        userMessage = null
    }
}