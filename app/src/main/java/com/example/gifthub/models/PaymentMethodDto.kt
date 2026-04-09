package com.example.gifthub.models

data class PaymentMethodDto(
    val transactionId: String = "",
    val orderID: String = "",
    val method: String = "",
    val paymentStatus: String = ""
)