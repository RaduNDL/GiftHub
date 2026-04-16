package com.example.gifthub.screens.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gifthub.repositories.ProductRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val productCount = getProductCount()
            val messages = buildList {
                add("🎁 Don't forget to check out our latest gifts!")
                add("✨ New products just arrived — take a look!")
                add("🛍️ Find the perfect gift for someone special today.")
                if (productCount > 0) {
                    add("🔥 We have $productCount amazing gifts waiting for you!")
                }
            }

            val message = messages.random()
            GiftHubMessagingService.showLocalNotification(
                context = context,
                title = "GiftHub",
                message = message,
                notificationId = NOTIFICATION_ID
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun getProductCount(): Int {
        return suspendCancellableCoroutine { continuation ->
            ProductRepository().getAllProducts(
                onSuccess = { products ->
                    continuation.resume(products.size)
                },
                onError = {
                    continuation.resume(0)
                }
            )
        }
    }

    companion object {
        const val WORK_NAME = "gifthub_periodic_notification"
        const val NOTIFICATION_ID = 1001
    }
}