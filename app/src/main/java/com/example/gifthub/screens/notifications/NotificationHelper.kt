package com.example.gifthub.screens.notifications


import android.content.Context

object NotificationHelper {

    fun notifyOrderPlaced(context: Context, orderId: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🎁 GiftHub",
            message = "Your order was placed successfully!",
            notificationId = ("order_placed_$orderId").hashCode()
        )
    }

    fun notifyOrderCancelled(context: Context, orderId: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "GiftHub",
            message = "Your order has been cancelled.",
            notificationId = ("order_cancel_$orderId").hashCode()
        )
    }

    fun notifyOrderStatusUpdated(context: Context, orderId: String, status: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "GiftHub",
            message = "Order status updated: $status",
            notificationId = ("order_status_$orderId").hashCode()
        )
    }

    fun notifyCartAdded(context: Context, productName: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🛒 Cart",
            message = "$productName was added to the cart.",
            notificationId = ("cart_add_$productName").hashCode()
        )
    }

    fun notifyCartRemoved(context: Context, productName: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🛒 Cart",
            message = "The product was removed from the cart.",
            notificationId = ("cart_remove_$productName").hashCode()
        )
    }

    fun notifyCartQuantityUpdated(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🛒 Cart",
            message = "Product quantity has been updated.",
            notificationId = "cart_qty".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyCartCleared(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🛒 Cart",
            message = "The cart has been cleared.",
            notificationId = "cart_cleared".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyFavoriteAdded(context: Context, productName: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "❤️ Favorites",
            message = "$productName was added to favorites.",
            notificationId = ("fav_add_$productName").hashCode()
        )
    }

    fun notifyFavoriteRemoved(context: Context, productName: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "💔 Favorites",
            message = "$productName was removed from favorites.",
            notificationId = ("fav_remove_$productName").hashCode()
        )
    }

    fun notifyAddressAdded(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "📍 Addresses",
            message = "Address added successfully.",
            notificationId = "address_add".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyAddressUpdated(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "📍 Addresses",
            message = "Address updated successfully.",
            notificationId = "address_update".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyAddressDeleted(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "📍 Addresses",
            message = "Address deleted.",
            notificationId = "address_delete".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyPaymentAdded(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "💳 Payments",
            message = "Payment method added.",
            notificationId = "payment_add".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyPaymentUpdated(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "💳 Payments",
            message = "Payment method updated.",
            notificationId = "payment_update".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyPaymentDeleted(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "💳 Payments",
            message = "Payment method deleted.",
            notificationId = "payment_delete".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyProductAdded(context: Context, productName: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "📦 Products",
            message = "Product $productName was added to the catalog.",
            notificationId = ("product_add_$productName").hashCode()
        )
    }

    fun notifyProductUpdated(context: Context, productName: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "📦 Products",
            message = "Product $productName was updated.",
            notificationId = ("product_update_$productName").hashCode()
        )
    }

    fun notifyProductDeleted(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "📦 Products",
            message = "Product was deleted from the catalog.",
            notificationId = "product_delete".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyCategoryAdded(context: Context, name: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🗂️ Categories",
            message = "Category $name was added.",
            notificationId = ("cat_add_$name").hashCode()
        )
    }

    fun notifyCategoryUpdated(context: Context, name: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🗂️ Categories",
            message = "Category $name was updated.",
            notificationId = ("cat_update_$name").hashCode()
        )
    }

    fun notifyCategoryDeleted(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🗂️ Categories",
            message = "Category was deleted.",
            notificationId = "cat_delete".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyReviewPosted(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "⭐ Reviews",
            message = "Your review has been posted.",
            notificationId = "review_post".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyReviewDeleted(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "⭐ Reviews",
            message = "The review was deleted.",
            notificationId = "review_delete".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyRegister(context: Context, firstName: String) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🎉 Welcome!",
            message = "Your GiftHub account has been created, $firstName!",
            notificationId = "register".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyLogin(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "👋 GiftHub",
            message = "Welcome back!",
            notificationId = "login".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyPasswordResetSent(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "🔒 GiftHub",
            message = "Password reset email has been sent.",
            notificationId = "pwd_reset".hashCode() + System.currentTimeMillis().toInt()
        )
    }

    fun notifyLogout(context: Context) {
        GiftHubMessagingService.showLocalNotification(
            context = context,
            title = "👋 GiftHub",
            message = "You have successfully logged out.",
            notificationId = "logout".hashCode() + System.currentTimeMillis().toInt()
        )
    }
}