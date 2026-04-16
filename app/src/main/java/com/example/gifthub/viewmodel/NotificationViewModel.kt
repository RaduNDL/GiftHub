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

    var isRefreshing by mutableStateOf(false)
        private set

    fun loadNotifications(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            isRefreshing = true
        } else {
            isLoading = true
        }
        errorMessage = null

        repository.getNotifications(
            onSuccess = { list ->
                notifications = list.sortedByDescending { it.createdDate }
                isLoading = false
                isRefreshing = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
                isRefreshing = false
            }
        )
    }

    fun refreshNotifications() {
        loadNotifications(forceRefresh = true)
    }

    fun markAsRead(notificationId: String) {
        if (notificationId.isBlank()) return

        val current = notifications
        notifications = current.map { n ->
            if (n.notificationID == notificationId) n.copy(markedAsRead = true) else n
        }

        repository.markAsRead(
            notificationId = notificationId,
            onSuccess = {},
            onError = { error ->
                errorMessage = error
                notifications = current
            }
        )
    }

    fun markAllAsRead() {
        val unreadIds = notifications.filter { !it.markedAsRead }.map { it.notificationID }
        if (unreadIds.isEmpty()) return

        val previous = notifications
        notifications = notifications.map { it.copy(markedAsRead = true) }

        unreadIds.forEach { id ->
            repository.markAsRead(
                notificationId = id,
                onSuccess = {},
                onError = { error ->
                    errorMessage = error
                    notifications = previous
                }
            )
        }
    }

    fun deleteNotification(notificationId: String) {
        if (notificationId.isBlank()) return

        val previous = notifications
        notifications = notifications.filterNot { it.notificationID == notificationId }

        repository.deleteNotification(
            notificationId = notificationId,
            onSuccess = {},
            onError = { error ->
                errorMessage = error
                notifications = previous
            }
        )
    }

    fun clearHistory() {
        val ids = notifications.map { it.notificationID }
        if (ids.isEmpty()) return

        val previous = notifications
        notifications = emptyList()

        ids.forEach { id ->
            repository.deleteNotification(
                notificationId = id,
                onSuccess = {},
                onError = { error ->
                    errorMessage = error
                    notifications = previous
                }
            )
        }
    }

    fun unreadCount(): Int {
        return notifications.count { !it.markedAsRead }
    }

    fun clearError() {
        errorMessage = null
    }
}