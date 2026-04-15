package com.example.gifthub.models

data class ProductDto(
    val idProduct: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val categoryId: String = "",
    val stock: Int = 0,
    val description: String = "",
    val customizable: Boolean = false,
    val active: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)