package com.example.gifthub.viewmodel

import com.example.gifthub.models.ProductDto

data class ProductCustomizationUiState(
    val loading: Boolean = false,
    val product: ProductDto? = null,
    val selectedByOption: Map<String, Set<String>> = emptyMap(),
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,
    val error: String? = null,
    val successAdded: Boolean = false
)