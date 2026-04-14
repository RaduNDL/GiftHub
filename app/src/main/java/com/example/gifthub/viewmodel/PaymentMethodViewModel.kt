package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.PaymentMethodDto
import com.example.gifthub.repositories.PaymentMethodRepository

class PaymentMethodViewModel : ViewModel() {

    private val repository = PaymentMethodRepository()

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
            // Add new
            repository.addPaymentMethod(
                method = method,
                paymentStatus = paymentStatus,
                onSuccess = {
                    loadPaymentMethods()
                    userMessage = "Payment method added"
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        } else {
            // Update existing
            val updateDto = PaymentMethodDto(
                transactionId = transactionId,
                orderID = "", 
                method = method,
                paymentStatus = paymentStatus
            )
            repository.updatePaymentMethod(
                paymentMethod = updateDto,
                onSuccess = {
                    loadPaymentMethods()
                    userMessage = "Payment method updated"
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
        repository.deletePaymentMethod(
            transactionId = transactionId,
            onSuccess = {
                loadPaymentMethods()
                userMessage = "Payment method deleted"
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
