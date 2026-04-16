package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.ProductDto
import com.example.gifthub.screens.notifications.NotificationHelper
import com.example.gifthub.repositories.FavoriteRepository
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FavoriteRepository()

    var favoriteProductIds by mutableStateOf<Set<String>>(emptySet())
        private set

    private val _favoriteProducts = mutableStateListOf<ProductDto>()
    val favoriteProducts: List<ProductDto> get() = _favoriteProducts

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
            try {
                favoriteProductIds = repository.getFavoriteProductIds(userId)
            } catch (e: Exception) {
                userMessage = "Error loading favorites"
            }
        }
    }

    fun loadFavoriteProducts(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            try {
                isLoading = true
                val products = repository.getFavoriteProducts(userId)
                _favoriteProducts.clear()
                _favoriteProducts.addAll(products)
                favoriteProductIds = products.map { it.idProduct }.toSet()
                isLoading = false
            } catch (e: Exception) {
                userMessage = "Error loading favorites"
                isLoading = false
            }
        }
    }

    fun isFavorite(productId: String): Boolean {
        return favoriteProductIds.contains(productId)
    }

    fun toggleFavorite(userId: String, product: ProductDto) {
        if (userId.isBlank()) {
            userMessage = "You must be logged in."
            return
        }
        if (product.idProduct.isBlank()) {
            userMessage = "Invalid product."
            return
        }

        viewModelScope.launch {
            try {
                val result = repository.toggleFavorite(userId, product)
                if (result.success) {
                    if (result.isFavorite == true) {
                        favoriteProductIds = favoriteProductIds + product.idProduct
                        if (_favoriteProducts.none { p -> p.idProduct == product.idProduct }) {
                            _favoriteProducts.add(0, product)
                        }
                        userMessage = "${product.name} added to favorites"
                        NotificationHelper.notifyFavoriteAdded(getApplication(), product.name)
                    } else {
                        favoriteProductIds = favoriteProductIds - product.idProduct
                        _favoriteProducts.removeAll { p -> p.idProduct == product.idProduct }
                        userMessage = "${product.name} removed from favorites"
                        NotificationHelper.notifyFavoriteRemoved(getApplication(), product.name)
                    }
                } else {
                    userMessage = result.errorMessage ?: "Could not update favorites"
                }
            } catch (e: Exception) {
                userMessage = "Error: ${e.message}"
            }
        }
    }

    fun removeFromFavorites(userId: String, productId: String, productName: String) {
        if (userId.isBlank()) {
            userMessage = "You must be logged in."
            return
        }

        viewModelScope.launch {
            try {
                val success = repository.removeFromFavorites(userId, productId)
                if (success) {
                    favoriteProductIds = favoriteProductIds - productId
                    _favoriteProducts.removeAll { p -> p.idProduct == productId }
                    userMessage = "$productName removed from favorites"
                    NotificationHelper.notifyFavoriteRemoved(getApplication(), productName)
                } else {
                    userMessage = "Could not remove product"
                }
            } catch (e: Exception) {
                userMessage = "Error: ${e.message}"
            }
        }
    }
}