package com.example.gifthub.screens.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
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

object NotificationHelper {
    private const val CHANNEL_ID = "gifthub_events"
    private const val CHANNEL_NAME = "GiftHub Events"
    private const val CHANNEL_DESCRIPTION = "Order and favorites updates"
    private const val EXTRA_TARGET_ROUTE = "target_route"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun show(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        targetRoute: String
    ) {
        ensureChannel(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_ROUTE, targetRoute)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notifySafely(context, notificationId, notification)
    }

    @SuppressLint("MissingPermission")
    private fun notifySafely(context: Context, notificationId: Int, notification: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }

    fun notifyOrderPlaced(context: Context, orderId: String) {
        show(
            context,
            ("order_placed_$orderId").hashCode(),
            "Order placed",
            "Your order was placed successfully.",
            GiftHubDestinations.ORDER_HISTORY
        )
    }

    fun notifyOrderStatusUpdated(context: Context, orderId: String, status: String) {
        show(
            context,
            ("order_status_$orderId$status").hashCode(),
            "Order updated",
            "Order status changed to $status.",
            GiftHubDestinations.ORDER_HISTORY
        )
    }

    fun notifyOrderCancelled(context: Context, orderId: String) {
        show(
            context,
            ("order_cancelled_$orderId").hashCode(),
            "Order cancelled",
            "Your order was cancelled.",
            GiftHubDestinations.ORDER_HISTORY
        )
    }

    fun notifyFavoriteAdded(context: Context, productName: String) {
        show(
            context,
            ("favorite_added_$productName").hashCode(),
            "Added to favorites",
            "$productName was added to favorites.",
            GiftHubDestinations.FAVORITES
        )
    }

    fun notifyFavoriteRemoved(context: Context, productName: String) {
        show(
            context,
            ("favorite_removed_$productName").hashCode(),
            "Removed from favorites",
            "$productName was removed from favorites.",
            GiftHubDestinations.FAVORITES
        )
    }

    fun notifyAddressAdded(context: Context) {
        show(
            context,
            "address_added".hashCode(),
            "Address added",
            "A new address was saved.",
            GiftHubDestinations.MANAGE_ADDRESS
        )
    }

    fun notifyAddressUpdated(context: Context) {
        show(
            context,
            "address_updated".hashCode(),
            "Address updated",
            "Your address was updated.",
            GiftHubDestinations.MANAGE_ADDRESS
        )
    }

    fun notifyAddressDeleted(context: Context) {
        show(
            context,
            "address_deleted".hashCode(),
            "Address deleted",
            "An address was removed.",
            GiftHubDestinations.MANAGE_ADDRESS
        )
    }

    fun notifyRegister(context: Context, firstName: String) {
        show(
            context,
            "register_$firstName".hashCode(),
            "Welcome",
            "Account created successfully.",
            GiftHubDestinations.HOME
        )
    }

    fun notifyLogin(context: Context) {
        show(
            context,
            "login".hashCode(),
            "Login successful",
            "Welcome back.",
            GiftHubDestinations.HOME
        )
    }

    fun notifyPasswordResetSent(context: Context) {
        show(
            context,
            "password_reset".hashCode(),
            "Reset email sent",
            "Check your inbox for reset instructions.",
            GiftHubDestinations.LOGIN
        )
    }

    fun notifyLogout(context: Context) {
        show(
            context,
            "logout".hashCode(),
            "Logged out",
            "You have been logged out.",
            GiftHubDestinations.LOGIN
        )
    }

    fun notifyCartAdded(context: Context, productName: String) {
        show(
            context,
            ("cart_added_$productName").hashCode(),
            "Cart updated",
            "$productName was added to cart.",
            GiftHubDestinations.CART
        )
    }

    fun notifyCartQuantityUpdated(context: Context) {
        show(
            context,
            "cart_qty_updated".hashCode(),
            "Cart updated",
            "Product quantity was updated.",
            GiftHubDestinations.CART
        )
    }

    fun notifyCartRemoved(context: Context, productName: String) {
        show(
            context,
            ("cart_removed_$productName").hashCode(),
            "Cart updated",
            "$productName was removed from cart.",
            GiftHubDestinations.CART
        )
    }

    fun notifyCartCleared(context: Context) {
        show(
            context,
            "cart_cleared".hashCode(),
            "Cart cleared",
            "All items were removed from cart.",
            GiftHubDestinations.CART
        )
    }

    fun notifyCategoryAdded(context: Context, categoryName: String) {
        show(
            context,
            ("category_added_$categoryName").hashCode(),
            "Category added",
            "$categoryName was added.",
            GiftHubDestinations.PRODUCTS
        )
    }

    fun notifyCategoryUpdated(context: Context, categoryName: String) {
        show(
            context,
            ("category_updated_$categoryName").hashCode(),
            "Category updated",
            "$categoryName was updated.",
            GiftHubDestinations.PRODUCTS
        )
    }

    fun notifyCategoryDeleted(context: Context) {
        show(
            context,
            "category_deleted".hashCode(),
            "Category deleted",
            "A category was removed.",
            GiftHubDestinations.PRODUCTS
        )
    }

    fun notifyPaymentAdded(context: Context) {
        show(
            context,
            "payment_added".hashCode(),
            "Payment method added",
            "A payment method was saved.",
            GiftHubDestinations.SAVED_PAYMENTS
        )
    }

    fun notifyPaymentUpdated(context: Context) {
        show(
            context,
            "payment_updated".hashCode(),
            "Payment method updated",
            "A payment method was updated.",
            GiftHubDestinations.SAVED_PAYMENTS
        )
    }

    fun notifyPaymentDeleted(context: Context) {
        show(
            context,
            "payment_deleted".hashCode(),
            "Payment method deleted",
            "A payment method was removed.",
            GiftHubDestinations.SAVED_PAYMENTS
        )
    }

    fun notifyProductAdded(context: Context, productName: String) {
        show(
            context,
            ("product_added_$productName").hashCode(),
            "Product added",
            "$productName was added.",
            GiftHubDestinations.PRODUCTS
        )
    }

    fun notifyProductUpdated(context: Context, productName: String) {
        show(
            context,
            ("product_updated_$productName").hashCode(),
            "Product updated",
            "$productName was updated.",
            GiftHubDestinations.PRODUCTS
        )
    }

    fun notifyProductDeleted(context: Context, productName: String) {
        show(
            context,
            ("product_deleted_$productName").hashCode(),
            "Product deleted",
            "$productName was deleted.",
            GiftHubDestinations.PRODUCTS
        )
    }

    fun notifyReviewPosted(context: Context) {
        show(
            context,
            "review_posted".hashCode(),
            "Review posted",
            "Your review was submitted.",
            GiftHubDestinations.PRODUCTS
        )
    }

    fun notifyReviewDeleted(context: Context) {
        show(
            context,
            "review_deleted".hashCode(),
            "Review deleted",
            "A review was removed.",
            GiftHubDestinations.PRODUCTS
        )
    }

    fun extractTargetRoute(intent: Intent?): String? {
        return intent?.getStringExtra(EXTRA_TARGET_ROUTE)
    }
}