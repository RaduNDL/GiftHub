package com.example.gifthub.models

data class FavoriteDto(
    val idFavorite: String = "",
    val userId: String = "",
    val productId: String = "",
    val addedAt: Long = System.currentTimeMillis()
)