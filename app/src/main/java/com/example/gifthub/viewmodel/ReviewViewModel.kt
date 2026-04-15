package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.ReviewDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReviewViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Recenziile pentru UN singur produs (folosit în ProductDetailsScreen)
    var reviewsList by mutableStateOf<List<ReviewDto>>(emptyList())
        private set

    // TOATE recenziile din colecție (folosit în ProductsScreen pentru rating + filtrare)
    var allReviews by mutableStateOf<List<ReviewDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchReviews(productId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val snapshot = db.collection("reviews")
                    .whereEqualTo("productId", productId)
                    .get()
                    .await()
                reviewsList = snapshot.documents
                    .mapNotNull { it.toObject(ReviewDto::class.java) }
                    .sortedByDescending { it.postDate }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Preia TOATE recenziile dintr-o singură interogare Firestore.
     * Necesar pentru calculul ratingurilor în lista de produse,
     * astfel încât să nu se suprascrie reviewsList la fiecare produs.
     */
    fun fetchAllReviews() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val snapshot = db.collection("reviews")
                    .get()
                    .await()
                allReviews = snapshot.documents
                    .mapNotNull { it.toObject(ReviewDto::class.java) }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun saveReview(
        productId: String,
        userId: String,
        rating: Int,
        comment: String,
        existingReviewId: String?
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                val reviewId = existingReviewId ?: UUID.randomUUID().toString()
                val review = ReviewDto(
                    idReview = reviewId,
                    userId = userId,
                    productId = productId,
                    rating = rating,
                    comment = comment,
                    postDate = System.currentTimeMillis()
                )
                db.collection("reviews").document(reviewId).set(review).await()
                fetchReviews(productId)
                // Reîncarcă și lista globală ca rating-urile să fie la zi în ProductsScreen
                fetchAllReviews()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteReview(reviewId: String, productId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                db.collection("reviews").document(reviewId).delete().await()
                fetchReviews(productId)
                fetchAllReviews()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}