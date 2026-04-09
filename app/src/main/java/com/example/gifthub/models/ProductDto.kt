package com.example.gifthub.models

data class ProductDto(
    val idProduct: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val categoryId: String,
    val imageUrl: String = ""
)