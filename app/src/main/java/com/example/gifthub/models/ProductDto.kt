package com.example.gifthub.models

data class ProductDto(
    val idProduct: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val categoryId: String = "",
    val imageUrl: String = "",
    val stock: Int = 0,
    val active: Boolean = true,
    val customizable: Boolean = false,
    val customizationOptions: List<CustomizationOption> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)