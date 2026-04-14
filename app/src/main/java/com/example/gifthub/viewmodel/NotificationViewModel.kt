package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.NotificationDto
import com.example.gifthub.repositories.NotificationRepository

class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()

    var notifications by mutableStateOf<List<NotificationDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadNotifications() {
        isLoading = true
        errorMessage = null

        repository.getNotifications(
            onSuccess = {
                notifications = it
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun markAsRead(notificationId: String) {
        repository.markAsRead(
            notificationId = notificationId,
            onSuccess = {
                notifications = notifications.map { n ->
                    if (n.notificationID == notificationId) n.copy(markedAsRead = true) else n
                }
            },
            onError = { error -> errorMessage = error }
        )
    }

    fun deleteNotification(notificationId: String) {
        repository.deleteNotification(
            notificationId = notificationId,
            onSuccess = { notifications = notifications.filterNot { it.notificationID == notificationId } },
            onError = { error -> errorMessage = error }
        )
    }

    fun clearError() {
        errorMessage = null
    }
}