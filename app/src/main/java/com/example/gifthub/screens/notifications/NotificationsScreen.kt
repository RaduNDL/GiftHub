package com.example.gifthub.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gifthub.models.NotificationDto
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.ui.components.GiftHubBottomBar
import com.example.gifthub.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    viewModel: NotificationViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        bottomBar = {
            GiftHubBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notification History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        IconButton(onClick = { viewModel.loadNotifications() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                        }

                        IconButton(onClick = { onNavigate(GiftHubDestinations.HOME) }) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        }
                    }
                }

                viewModel.errorMessage?.let { message ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                when {
                    viewModel.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    viewModel.notifications.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notifications yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 16.dp),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = viewModel.notifications,
                                key = { it.notificationID }
                            ) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onClick = {
                                        if (!notification.markedAsRead) {
                                            viewModel.markAsRead(notification.notificationID)
                                        }

                                        val destination = resolveNotificationDestination(notification)
                                        onNavigate(destination)
                                    },
                                    onDelete = {
                                        viewModel.deleteNotification(notification.notificationID)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resolveNotificationDestination(notification: NotificationDto): String {
    val route = notification.targetRoute.trim()
    if (route.isNotEmpty()) {
        return when (route) {
            GiftHubDestinations.LOGIN,
            GiftHubDestinations.REGISTER,
            GiftHubDestinations.HOME,
            GiftHubDestinations.PRODUCTS,
            GiftHubDestinations.CART,
            GiftHubDestinations.CHECKOUT,
            GiftHubDestinations.ORDER_HISTORY,
            GiftHubDestinations.FAVORITES,
            GiftHubDestinations.PROFILE,
            GiftHubDestinations.NOTIFICATIONS,
            GiftHubDestinations.MANAGE_ADDRESS,
            GiftHubDestinations.SAVED_PAYMENTS,
            GiftHubDestinations.ADD_PRODUCT,
            GiftHubDestinations.MANAGE_CATEGORIES -> route
            else -> {
                when {
                    route.startsWith("product_details/") -> route
                    route.startsWith("edit_product/") -> route
                    route.startsWith("products_by_category/") -> route
                    route.startsWith("order_details/") -> route
                    route.startsWith("product_customization/") -> route
                    else -> fallbackDestinationByType(notification.type)
                }
            }
        }
    }

    return fallbackDestinationByType(notification.type)
}

private fun fallbackDestinationByType(type: String): String {
    return when (type.trim().lowercase()) {
        "favorite_update", "favorite_added", "favorite_removed" -> GiftHubDestinations.FAVORITES
        "order_update", "order_placed", "order_status", "order_cancelled" -> GiftHubDestinations.ORDER_HISTORY
        "cart_update", "cart_added", "cart_removed" -> GiftHubDestinations.CART
        "product_update", "product_added", "product_deleted", "review_added", "review_deleted" -> GiftHubDestinations.PRODUCTS
        "payment_update", "payment_added", "payment_deleted" -> GiftHubDestinations.SAVED_PAYMENTS
        "address_update", "address_added", "address_deleted" -> GiftHubDestinations.MANAGE_ADDRESS
        "auth_login", "auth_register" -> GiftHubDestinations.HOME
        else -> GiftHubDestinations.NOTIFICATIONS
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(notification.createdDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.markedAsRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = {
                            Text(if (notification.markedAsRead) "Read" else "Unread")
                        },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                }

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}