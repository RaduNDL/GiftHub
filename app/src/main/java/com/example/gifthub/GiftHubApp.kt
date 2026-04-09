package com.example.gifthub

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class GiftHubApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GIFT_HUB_CHANNEL_ID,
                "GiftHub Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Order updates and app notifications"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val GIFT_HUB_CHANNEL_ID = "gifthub_notifications"
    }
}