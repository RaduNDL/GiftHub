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
            onSuccess = {
                paymentMethods = it
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun savePaymentMethod(
        transactionId: String,
        method: String,
        paymentStatus: String
    ) {
        if (method.isBlank() || paymentStatus.isBlank()) {
            errorMessage = "All fields are required."
            return
        }

        isLoading = true
        errorMessage = null

        if (transactionId.isBlank()) {
            repository.addPaymentMethod(
                method = method,
                paymentStatus = paymentStatus,
                onSuccess = {
                    userMessage = "Payment method added successfully."
                    loadPaymentMethods()
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        } else {
            repository.updatePaymentMethod(
                paymentMethod = PaymentMethodDto(
                    transactionId = transactionId,
                    orderID = "",
                    method = method.trim(),
                    paymentStatus = paymentStatus.trim()
                ),
                onSuccess = {
                    userMessage = "Payment method updated successfully."
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
        if (transactionId.isBlank()) {
            errorMessage = "Invalid payment method ID."
            return
        }

        isLoading = true
        errorMessage = null

        repository.deletePaymentMethod(
            transactionId = transactionId,
            onSuccess = {
                userMessage = "Payment method deleted successfully."
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