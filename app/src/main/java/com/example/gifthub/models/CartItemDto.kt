package com.example.gifthub.models

data class CartItemDto(
    val cartItemId: String = "",
    val productId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val customText: String = "",
    val customColor: String = "",
    val lineExtraPrice: Double = 0.0,
    val lineTotalPrice: Double = 0.0,
    val customizationsHash: String = "",
    val selectedCustomizations: List<SelectedCustomizationDto> = emptyList()
)