package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.ReviewDto
import com.example.gifthub.repositories.NotificationRepository
import com.example.gifthub.screens.notifications.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val notificationRepository = NotificationRepository()

    var reviewsList by mutableStateOf<List<ReviewDto>>(emptyList())
        private set

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

    fun fetchAllReviews() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val snapshot = db.collection("reviews").get().await()
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
                NotificationHelper.notifyReviewPosted(getApplication())
                notificationRepository.createProductNotification(
                    title = "Review posted",
                    message = "Your review was posted",
                    type = "review_added"
                )
                fetchReviews(productId)
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
                NotificationHelper.notifyReviewDeleted(getApplication())
                notificationRepository.createProductNotification(
                    title = "Review deleted",
                    message = "Your review was removed",
                    type = "review_deleted"
                )
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