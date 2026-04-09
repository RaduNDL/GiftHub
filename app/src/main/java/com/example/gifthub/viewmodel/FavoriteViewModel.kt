package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.ProductDto
import com.example.gifthub.repositories.FavoriteRepository
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val repository: FavoriteRepository = FavoriteRepository()
) : ViewModel() {

    var favoriteProductIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var favoriteProducts by mutableStateOf<List<ProductDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    fun clearUserMessage() {
        userMessage = null
    }

    fun loadFavoriteIds(userId: String) {
        if (userId.isBlank()) return

        viewModelScope.launch {
            favoriteProductIds = repository.getFavoriteProductIds(userId)
        }
    }

    fun loadFavoriteProducts(userId: String) {
        if (userId.isBlank()) return

        viewModelScope.launch {
            isLoading = true
            favoriteProducts = repository.getFavoriteProducts(userId)
            favoriteProductIds = favoriteProducts.map { it.idProduct }.toSet()
            isLoading = false
        }
    }

    fun isFavorite(productId: String): Boolean {
        return favoriteProductIds.contains(productId)
    }

    fun toggleFavorite(
        userId: String,
        product: ProductDto
    ) {
        if (userId.isBlank()) {
            userMessage = "You must be logged in."
            return
        }

        if (product.idProduct.isBlank()) {
            userMessage = "Invalid product."
            return
        }

        viewModelScope.launch {
            val result = repository.toggleFavorite(userId, product)

            if (result.success) {
                if (result.isFavorite == true) {
                    favoriteProductIds = favoriteProductIds + product.idProduct

                    if (favoriteProducts.none { it.idProduct == product.idProduct }) {
                        favoriteProducts = listOf(product) + favoriteProducts
                    }

                    userMessage = "${product.name} added to favorites."
                } else {
                    favoriteProductIds = favoriteProductIds - product.idProduct
                    favoriteProducts = favoriteProducts.filterNot { it.idProduct == product.idProduct }
                    userMessage = "${product.name} removed from favorites."
                }
            } else {
                userMessage = result.errorMessage ?: "Could not update favorites."
            }
        }
    }

    fun removeFromFavorites(
        userId: String,
        productId: String,
        productName: String
    ) {
        if (userId.isBlank()) {
            userMessage = "You must be logged in."
            return
        }

        viewModelScope.launch {
            val success = repository.removeFromFavorites(userId, productId)
            if (success) {
                favoriteProductIds = favoriteProductIds - productId
                favoriteProducts = favoriteProducts.filterNot { it.idProduct == productId }
                userMessage = "$productName removed from favorites."
            } else {
                userMessage = "Could not remove product from favorites."
            }
        }
    }
}