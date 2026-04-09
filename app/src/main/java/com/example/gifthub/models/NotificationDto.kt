package com.example.gifthub.models

data class NotificationDto(
    val notificationID: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val createdDate: Long = 0L,
    val markedAsRead: Boolean = false,
    val type: String = "giftHubNotification",
    val targetRoute: String = "order_history",
    val orderId: String = ""
)