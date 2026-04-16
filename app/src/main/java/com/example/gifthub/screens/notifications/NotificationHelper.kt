package com.example.gifthub.screens.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.gifthub.R
import kotlin.random.Random

object NotificationHelper {
    const val CHANNEL_ID_GENERAL = "gifthub_general"
    const val CHANNEL_ID_PRODUCTS = "gifthub_products"
    const val CHANNEL_ID_ORDERS = "gifthub_orders"
    const val CHANNEL_ID_PROMOTIONS = "gifthub_promotions"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channels = listOf(
                NotificationChannel(CHANNEL_ID_GENERAL, "General", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_PRODUCTS, "Products", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_ORDERS, "Orders", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_PROMOTIONS, "Promotions", NotificationManager.IMPORTANCE_HIGH)
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }

    fun notifyProductAdded(context: Context, productName: String) {
        show(context, CHANNEL_ID_PRODUCTS, "New product added", "$productName is now available")
    }

    fun notifyProductUpdated(context: Context, productName: String) {
        show(context, CHANNEL_ID_PRODUCTS, "Product updated", "$productName was updated")
    }

    fun notifyProductDeleted(context: Context, productName: String) {
        show(context, CHANNEL_ID_PRODUCTS, "Product removed", "$productName was removed")
    }

    fun notifyOrderPlaced(context: Context, orderId: String) {
        show(context, CHANNEL_ID_ORDERS, "Order placed", "Your order #$orderId was placed successfully")
    }

    fun notifyOrderStatusChanged(context: Context, orderId: String, status: String) {
        show(context, CHANNEL_ID_ORDERS, "Order update", "Order #$orderId is now $status")
    }

    fun notifyOrderStatusUpdated(context: Context, orderId: String, status: String) {
        show(context, CHANNEL_ID_ORDERS, "Order status updated", "Order #$orderId is now $status")
    }

    fun notifyOrderCancelled(context: Context, orderId: String) {
        show(context, CHANNEL_ID_ORDERS, "Order cancelled", "Order #$orderId was cancelled")
    }

    fun notifyPromotion(context: Context, title: String, message: String) {
        show(context, CHANNEL_ID_PROMOTIONS, title, message)
    }

    fun notifyAddressAdded(context: Context, addressLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Address added", "$addressLabel was added")
    }

    fun notifyAddressAdded(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Address added", "Address added successfully")
    }

    fun notifyAddressUpdated(context: Context, addressLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Address updated", "$addressLabel was updated")
    }

    fun notifyAddressUpdated(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Address updated", "Address updated successfully")
    }

    fun notifyAddressDeleted(context: Context, addressLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Address removed", "$addressLabel was removed")
    }

    fun notifyAddressDeleted(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Address removed", "Address deleted successfully")
    }

    fun notifyRegister(context: Context, firstName: String) {
        show(context, CHANNEL_ID_GENERAL, "Welcome", "Account created successfully, $firstName")
    }

    fun notifyRegister(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Welcome", "Account created successfully")
    }

    fun notifyLogin(context: Context, firstName: String) {
        show(context, CHANNEL_ID_GENERAL, "Welcome back", "Logged in successfully, $firstName")
    }

    fun notifyLogin(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Welcome back", "Logged in successfully")
    }

    fun notifyPasswordResetSent(context: Context, email: String) {
        show(context, CHANNEL_ID_GENERAL, "Password reset", "Reset link sent to $email")
    }

    fun notifyPasswordResetSent(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Password reset", "Reset link sent successfully")
    }

    fun notifyLogout(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Logged out", "You have been logged out")
    }

    fun notifyCartAdded(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Added to cart", "$productName was added to cart")
    }

    fun notifyCartAdded(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Added to cart", "Item added to cart")
    }

    fun notifyCartQuantityUpdated(context: Context, productName: String, quantity: Int) {
        show(context, CHANNEL_ID_GENERAL, "Cart updated", "$productName quantity is now $quantity")
    }

    fun notifyCartQuantityUpdated(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Cart updated", "Cart quantity updated")
    }

    fun notifyCartRemoved(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Removed from cart", "$productName was removed from cart")
    }

    fun notifyCartRemoved(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Removed from cart", "Item removed from cart")
    }

    fun notifyCartCleared(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Cart cleared", "All items were removed from cart")
    }

    fun notifyCategoryAdded(context: Context, categoryName: String) {
        show(context, CHANNEL_ID_GENERAL, "Category added", "$categoryName was added")
    }

    fun notifyCategoryAdded(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Category added", "Category added successfully")
    }

    fun notifyCategoryUpdated(context: Context, categoryName: String) {
        show(context, CHANNEL_ID_GENERAL, "Category updated", "$categoryName was updated")
    }

    fun notifyCategoryUpdated(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Category updated", "Category updated successfully")
    }

    fun notifyCategoryDeleted(context: Context, categoryName: String) {
        show(context, CHANNEL_ID_GENERAL, "Category removed", "$categoryName was removed")
    }

    fun notifyCategoryDeleted(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Category removed", "Category deleted successfully")
    }

    fun notifyFavoriteAdded(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Added to favorites", "$productName was added to favorites")
    }

    fun notifyFavoriteRemoved(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Removed from favorites", "$productName was removed from favorites")
    }

    fun notifyPaymentAdded(context: Context, paymentLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Payment method added", "$paymentLabel was added")
    }

    fun notifyPaymentAdded(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Payment method added", "Payment method added successfully")
    }

    fun notifyPaymentUpdated(context: Context, paymentLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Payment method updated", "$paymentLabel was updated")
    }

    fun notifyPaymentUpdated(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Payment method updated", "Payment method updated successfully")
    }

    fun notifyPaymentDeleted(context: Context, paymentLabel: String) {
        show(context, CHANNEL_ID_GENERAL, "Payment method removed", "$paymentLabel was removed")
    }

    fun notifyPaymentDeleted(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Payment method removed", "Payment method deleted successfully")
    }

    fun notifyReviewPosted(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Review posted", "Your review for $productName was posted")
    }

    fun notifyReviewPosted(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Review posted", "Your review was posted")
    }

    fun notifyReviewDeleted(context: Context, productName: String) {
        show(context, CHANNEL_ID_GENERAL, "Review deleted", "Your review for $productName was removed")
    }

    fun notifyReviewDeleted(context: Context) {
        show(context, CHANNEL_ID_GENERAL, "Review deleted", "Your review was removed")
    }

    fun extractTargetRoute(intent: Intent?): String? {
        if (intent == null) return null
        return intent.getStringExtra("targetRoute")
            ?: intent.extras?.getString("targetRoute")
            ?: intent.data?.getQueryParameter("targetRoute")
    }
}