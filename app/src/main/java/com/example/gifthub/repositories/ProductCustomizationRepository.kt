package com.example.gifthub.repositories

import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.SelectedCustomization

interface ProductCustomizationRepository {
    suspend fun getProduct(productId: String): ProductDto
    suspend fun addCustomizedItemToCart(
        userId: String,
        product: ProductDto,
        quantity: Int,
        selections: List<SelectedCustomization>
    ): Result<Unit>
    fun computeCustomizationExtra(selections: List<SelectedCustomization>): Double
    fun buildCustomizationHash(selections: List<SelectedCustomization>): String
    fun validateSelections(product: ProductDto, selections: List<SelectedCustomization>): Result<Unit>
}