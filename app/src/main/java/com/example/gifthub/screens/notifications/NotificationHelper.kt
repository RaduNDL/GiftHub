package com.example.gifthub.screens.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.gifthub.MainActivity
import com.example.gifthub.R
import com.example.gifthub.navigation.GiftHubDestinations
import kotlin.random.Random

object NotificationHelper {
    const val CHANNEL_ID_GENERAL = "gifthub_general"
    const val CHANNEL_ID_PRODUCTS = "gifthub_products"
    const val CHANNEL_ID_ORDERS = "gifthub_orders"
    const val EXTRA_TARGET_ROUTE = "targetRoute"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channels = listOf(
                NotificationChannel(CHANNEL_ID_GENERAL, "General", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_PRODUCTS, "Products", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_ORDERS, "Orders", NotificationManager.IMPORTANCE_HIGH)
            )
            channels.forEach { channel ->
                channel.description = channel.name.toString()
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun show(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        targetRoute: String? = null,
        notificationId: Int = Random.nextInt(100000, 999999)
    ) {
        ensureChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            targetRoute?.let { putExtra(EXTRA_TARGET_ROUTE, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }

    fun notifyProductAdded(context: Context, productName: String) {
        show(context, CHANNEL_ID_PRODUCTS, "New product added", "$productName is now available", GiftHubDestinations.PRODUCTS)
    }

    fun notifyProductUpdated(context: Context, productName: String) {
        show(context, CHANNEL_ID_PRODUCTS, "Product updated", "$productName was updated", GiftHubDestinations.PRODUCTS)
    }

    fun notifyProductDeleted(context: Context, productName: String) {
        show(context, CHANNEL_ID_PRODUCTS, "Product removed", "$productName was removed", GiftHubDestinations.PRODUCTS)
    }

    fun notifyOrderPlaced(context: Context, orderId: String) {
        show(context, CHANNEL_ID_ORDERS, "Order placed", "Your order #$orderId was placed successfully", GiftHubDestinations.ORDER_HISTORY)
    }

    fun notifyOrderStatusUpdated(context: Context, orderId: String, status: String) {
        show(context, CHANNEL_ID_ORDERS, "Order status updated", "Order #$orderId is now $status", GiftHubDestinations.ORDER_HISTORY)
    }

    fun notifyOrderCancelled(context: Context, orderId: String) {
        show(context, CHANNEL_ID_ORDERS, "Order cancelled", "Order #$orderId was cancelled", GiftHubDestinations.ORDER_HISTORY)
    }

    fun notifyAddressAdded(context: Context, addressLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Address added", "$addressLabel was added", GiftHubDestinations.MANAGE_ADDRESS)
    }

    fun notifyAddressUpdated(context: Context, addressLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Address updated", "$addressLabel was updated", GiftHubDestinations.MANAGE_ADDRESS)
    }

    fun notifyAddressDeleted(context: Context, addressLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Address removed", "$addressLabel was removed", GiftHubDestinations.MANAGE_ADDRESS)
    }

    fun notifyPasswordResetSent(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Password reset", "Reset link sent successfully", GiftHubDestinations.LOGIN)
    }

    fun notifyCartAdded(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Added to cart", "$productName was added to cart", GiftHubDestinations.CART)
    }

    fun notifyCartQuantityUpdated(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Cart updated", "Cart quantity updated", GiftHubDestinations.CART)
    }

    fun notifyCartRemoved(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Removed from cart", "$productName was removed from cart", GiftHubDestinations.CART)
    }

    fun notifyCartCleared(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Cart cleared", "All items were removed from cart", GiftHubDestinations.CART)
    }

    fun notifyCategoryAdded(context: Context, categoryName: String) {
        show(context, CHANNEL_ID_GENERAL, "Category added", "$categoryName was added", GiftHubDestinations.PRODUCTS)
    }

    fun notifyCategoryUpdated(context: Context, categoryName: String) {
        show(context, CHANNEL_ID_GENERAL, "Category updated", "$categoryName was updated", GiftHubDestinations.PRODUCTS)
    }

    fun notifyCategoryDeleted(context: Context, categoryName: String) {
        show(context, CHANNEL_ID_GENERAL, "Category removed", "$categoryName was removed", GiftHubDestinations.PRODUCTS)
    }

    fun notifyFavoriteAdded(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Added to favorites", "$productName was added to favorites", GiftHubDestinations.FAVORITES)
    }

    fun notifyFavoriteRemoved(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Removed from favorites", "$productName was removed from favorites", GiftHubDestinations.FAVORITES)
    }

    fun notifyPaymentAdded(context: Context, paymentLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Payment method added", "$paymentLabel was added", GiftHubDestinations.SAVED_PAYMENTS)
    }

    fun notifyPaymentUpdated(context: Context, paymentLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Payment method updated", "$paymentLabel was updated", GiftHubDestinations.SAVED_PAYMENTS)
    }

    fun notifyPaymentDeleted(context: Context, paymentLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Payment method removed", "$paymentLabel was removed", GiftHubDestinations.SAVED_PAYMENTS)
    }

    fun notifyReviewPosted(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Review posted", "Your review was posted", GiftHubDestinations.PRODUCTS)
    }

    fun notifyReviewDeleted(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Review deleted", "Your review was removed", GiftHubDestinations.PRODUCTS)
    }

    fun extractTargetRoute(intent: Intent?): String? {
        if (intent == null) return null
        return intent.getStringExtra(EXTRA_TARGET_ROUTE)
            ?: intent.extras?.getString(EXTRA_TARGET_ROUTE)
            ?: intent.data?.getQueryParameter(EXTRA_TARGET_ROUTE)
    }
}