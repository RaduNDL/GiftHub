package com.example.gifthub.models

data class ReviewDto(
    val idReview: String = "",
    val userId: String = "",
    val productId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val postDate: Long = System.currentTimeMillis()
)