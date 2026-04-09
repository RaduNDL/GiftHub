package com.example.gifthub.models

data class OrderItemDto(
    val productId: String = "",
    val quantity: Int = 0,
    val currentUnitPrice: Double = 0.0
)