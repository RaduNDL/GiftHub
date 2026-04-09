package com.example.gifthub.models

import com.google.firebase.Timestamp

data class OrderDto(
    val orderId: String = "",
    val userId: String = "",
    val items: List<CartItemDto> = emptyList(),
    val totalAmount: Double = 0.0,
    val address: String = "",
    val paymentMethod: String = "",
    val status: String = "Pending",
    val createdAt: Timestamp? = null
)