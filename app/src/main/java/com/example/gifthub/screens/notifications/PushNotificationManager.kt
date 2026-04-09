package com.example.gifthub.notifications

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.gifthub.navigation.GiftHubDestinations

data class PendingPushNavigation(
    val route: String,
    val notificationId: String? = null,
    val orderId: String? = null,
    val type: String? = null
)

object PushNotificationManager {

    const val EXTRA_FROM_PUSH = "giftHub_from_push"
    const val EXTRA_TARGET_ROUTE = "giftHub_target_route"
    const val EXTRA_NOTIFICATION_ID = "giftHub_notification_id"
    const val EXTRA_ORDER_ID = "giftHub_order_id"
    const val EXTRA_NOTIFICATION_TYPE = "giftHub_notification_type"

    var pendingNavigation by mutableStateOf<PendingPushNavigation?>(null)
        private set

    fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_FROM_PUSH, false) != true) return

        val route = intent.getStringExtra(EXTRA_TARGET_ROUTE)
            .orEmpty()
            .ifBlank { GiftHubDestinations.ORDER_HISTORY }

        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID)
        val type = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE)

        pendingNavigation = PendingPushNavigation(
            route = route,
            notificationId = notificationId,
            orderId = orderId,
            type = type
        )

        intent.removeExtra(EXTRA_FROM_PUSH)
        intent.removeExtra(EXTRA_TARGET_ROUTE)
        intent.removeExtra(EXTRA_NOTIFICATION_ID)
        intent.removeExtra(EXTRA_ORDER_ID)
        intent.removeExtra(EXTRA_NOTIFICATION_TYPE)
    }

    fun consumePendingNavigation(): PendingPushNavigation? {
        val current = pendingNavigation
        pendingNavigation = null
        return current
    }
}