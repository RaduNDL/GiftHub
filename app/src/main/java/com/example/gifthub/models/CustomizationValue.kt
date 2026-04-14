package com.example.gifthub.models

data class CustomizationValue(
    val id: String = "",
    val label: String = "",
    val extraPrice: Double = 0.0,
    val colorHex: String? = null,
    val imageUrl: String? = null
)