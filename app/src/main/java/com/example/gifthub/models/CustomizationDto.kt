package com.example.gifthub.models

data class CustomizationDto(
    val engravingText: String = "",
    val uploadedImageUrl: String = "",
    val customizationFee: Double = 0.0
)