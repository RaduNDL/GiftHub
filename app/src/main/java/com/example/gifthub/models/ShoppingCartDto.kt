package com.example.gifthub.models

data class ShoppingCartDto(
    val cartId: String = "",
    val userId: String = "",
    val items: List<CartItemDto> = emptyList(),
    val temporaryValue: Double = 0.0
)