package com.example.gifthub.models

data class CartItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val basePrice: Double = 0.0,
    val quantity: Int = 0,
    val selectedCustomizations: List<SelectedCustomization> = emptyList(),
    val customizationsHash: String = "",
    val lineExtraPrice: Double = 0.0,
    val lineTotalPrice: Double = 0.0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)