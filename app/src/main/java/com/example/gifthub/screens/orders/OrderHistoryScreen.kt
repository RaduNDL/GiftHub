package com.example.gifthub.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gifthub.models.CartItemDto
import com.example.gifthub.models.OrderDto
import com.example.gifthub.viewmodel.OrderViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ALL_STATUSES = listOf("Pending", "Processing", "Delivered", "Cancelled")

@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    isEmployee: Boolean = false,
    orderViewModel: OrderViewModel = viewModel()
) {
    val orders = orderViewModel.orders
    val isLoading = orderViewModel.isLoading
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedOrder by remember { mutableStateOf<OrderDto?>(null) }
    var showOrderDetails by remember { mutableStateOf(false) }

    LaunchedEffect(isEmployee) {
        orderViewModel.loadOrders(isEmployee = isEmployee)
    }

    LaunchedEffect(orderViewModel.errorMessage) {
        orderViewModel.errorMessage?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = error, duration = SnackbarDuration.Long)
            }
            orderViewModel.clearError()
        }
    }

    LaunchedEffect(orderViewModel.userMessage) {
        orderViewModel.userMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            }
            orderViewModel.clearUserMessage()
        }
    }

    if (showOrderDetails && selectedOrder != null) {
        OrderDetailsDialog(
            order = selectedOrder!!,
            onDismiss = { showOrderDetails = false; selectedOrder = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = if (isEmployee) "All Orders" else "Order History",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEmployee) "Manage and update order statuses" else "View and track your orders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                isLoading && orders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                orders.isEmpty() -> EmptyOrdersState()

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
                    ) {
                        item { OrdersSummaryCard(orderCount = orders.size, isEmployee = isEmployee) }

                        items(orders, key = { it.orderId }) { order ->
                            OrderCard(
                                order = order,
                                isEmployee = isEmployee,
                                isUpdating = orderViewModel.isLoading,
                                onViewDetails = { selectedOrder = order; showOrderDetails = true },
                                onStatusChange = { newStatus ->
                                    orderViewModel.updateOrderStatus(order.userId, order.orderId, newStatus)
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.size(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailsScreen(
    orderId: String,
    onBack: () -> Unit,
    orderViewModel: OrderViewModel = viewModel(),
    isEmployee: Boolean = false
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showStatusUpdateDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf("") }

    val order: OrderDto? = orderViewModel.orders.firstOrNull { it.orderId == orderId }

    LaunchedEffect(orderId, isEmployee) {
        if (order == null || orderViewModel.orders.isEmpty()) {
            orderViewModel.loadOrders(isEmployee = isEmployee)
        }
    }

    LaunchedEffect(orderViewModel.errorMessage) {
        orderViewModel.errorMessage?.let { error ->
            coroutineScope.launch { snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Long) }
            orderViewModel.clearError()
        }
    }

    LaunchedEffect(orderViewModel.userMessage) {
        orderViewModel.userMessage?.let { message ->
            coroutineScope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
            orderViewModel.clearUserMessage()
        }
    }

    if (showStatusUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showStatusUpdateDialog = false },
            title = { Text("Update Order Status", fontWeight = FontWeight.Bold) },
            text = {
                Text("Change order #${order?.orderId?.take(8)?.uppercase()} status to \"$pendingStatus\"?\n\nThe customer will receive a push notification about this change.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (order != null) {
                            orderViewModel.updateOrderStatus(order.userId, orderId, pendingStatus)
                        }
                        showStatusUpdateDialog = false
                    }
                ) { Text("Confirm & Notify") }
            },
            dismissButton = {
                TextButton(onClick = { showStatusUpdateDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel this order? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { showCancelDialog = false; orderViewModel.cancelOrder(orderId) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Yes, Cancel") }
            },
            dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("Keep Order") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text("Order Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (order != null) {
                            Text("#${order.orderId.take(8).uppercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize().padding(paddingValues), color = MaterialTheme.colorScheme.background) {
            if (orderViewModel.isLoading && order == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (order == null) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Order not found.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val statusColors = statusBannerColors(order.status)
                Column(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().background(statusColors.first, RoundedCornerShape(16.dp)).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(order.status, color = statusColors.second, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailInfoRow("Order ID", order.orderId.take(8).uppercase())
                            DetailInfoRow("Date", formatDate(order.createdAt))
                            DetailInfoRow("Status", order.status)
                        }
                    }

                    Text("Items (${order.items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    order.items.forEach { item -> DetailItemCard(item = item) }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                                Text("$${String.format(Locale.US, "%.2f", order.subtotal)}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("$${String.format(Locale.US, "%.2f", order.totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Column {
                                    Text("Shipping Address", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text(order.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            HorizontalDivider()
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Payment, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Column {
                                    Text("Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text(order.paymentMethod, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (isEmployee) {
                        EmployeeStatusPanel(
                            currentStatus = order.status,
                            isUpdating = orderViewModel.isLoading,
                            onStatusChange = { newStatus ->
                                pendingStatus = newStatus
                                showStatusUpdateDialog = true
                            }
                        )
                    }

                    if (order.status.equals("Pending", ignoreCase = true) && !isEmployee) {
                        Button(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            enabled = !orderViewModel.isLoading
                        ) {
                            Text("Cancel Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmployeeStatusPanel(
    currentStatus: String,
    isUpdating: Boolean,
    onStatusChange: (String) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf("") }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Update Status", fontWeight = FontWeight.Bold) },
            text = { Text("Change order status to \"$pendingStatus\"?") },
            confirmButton = {
                Button(onClick = {
                    onStatusChange(pendingStatus)
                    showConfirmDialog = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Update Status",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ALL_STATUSES.forEach { status ->
                val isSelected = status.equals(currentStatus, ignoreCase = true)
                val (bgColor, textColor) = statusBannerColors(status)

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected && !isUpdating) {
                            pendingStatus = status
                            showConfirmDialog = true
                        }
                    },
                    label = {
                        Text(
                            text = status,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    enabled = !isUpdating,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = bgColor,
                        selectedLabelColor = textColor,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = textColor.copy(alpha = 0.5f),
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }

            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp).align(Alignment.CenterVertically),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

private fun statusBannerColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
    "pending" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
    "processing" -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
    "delivered" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    "cancelled" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
    else -> Pair(Color(0xFFF5F5F5), Color(0xFF616161))
}

private fun formatDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "Unknown date"
    val date = Date(timestamp.seconds * 1000)
    return SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
}

@Composable
private fun OrdersSummaryCard(orderCount: Int, isEmployee: Boolean) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.History, "Orders", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.size(14.dp))
            Column {
                Text(
                    text = if (isEmployee) "Total Orders (All Users)" else "Total Orders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    orderCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderDto,
    isEmployee: Boolean,
    isUpdating: Boolean,
    onViewDetails: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Order #${order.orderId.take(8).uppercase()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(formatDate(order.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HistoryStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.size(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HistoryDetailItem(Icons.Default.LocationOn, "Items", "${order.items.size}")
                HistoryDetailItem(Icons.Default.Payment, "Amount", "$${String.format(Locale.US, "%.2f", order.totalAmount)}")
                HistoryDetailItem(Icons.Outlined.LocalShipping, "Method", order.paymentMethod.take(10))
            }

            Spacer(modifier = Modifier.size(12.dp))

            if (order.items.isNotEmpty()) {
                Text(
                    "Items (${order.items.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                order.items.take(2).forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("• ${item.name}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(
                            "x${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (order.items.size > 2) {
                    Text(
                        "+${order.items.size - 2} more items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
            }

            if (isEmployee) {
                EmployeeStatusPanel(
                    currentStatus = order.status,
                    isUpdating = isUpdating,
                    onStatusChange = onStatusChange
                )
                Spacer(modifier = Modifier.size(10.dp))
            }

            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("View Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun HistoryStatusBadge(status: String) {
    val colors = statusBannerColors(status)
    val icon: ImageVector = when (status.lowercase()) {
        "delivered" -> Icons.Default.CheckCircle
        else -> Icons.Outlined.LocalShipping
    }
    AssistChip(
        onClick = {},
        label = { Text(status, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
        colors = AssistChipDefaults.assistChipColors(containerColor = colors.first, labelColor = colors.second),
        leadingIcon = { Icon(icon, status, Modifier.size(16.dp), tint = colors.second) },
        shape = RoundedCornerShape(8.dp),
        border = null
    )
}

@Composable
private fun HistoryDetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.size(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OrderDetailsDialog(order: OrderDto, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Order Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Order ID: ${order.orderId.take(8).uppercase()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text("Date: ${formatDate(order.createdAt)}", style = MaterialTheme.typography.labelMedium)
                        Text("Status: ${order.status}", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Text("Items (${order.items.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

                order.items.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "$${String.format(Locale.US, "%.2f", item.price)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("x${item.quantity}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "$${String.format(Locale.US, "%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Shipping Address", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
                    Text(order.address, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
                    Text(order.paymentMethod, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun EmptyOrdersState() {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.size(16.dp))
            Text("No Orders Yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Start shopping to see your order history here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailItemCard(item: CartItemDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text("$${String.format(Locale.US, "%.2f", item.price)} each", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (item.customText.isNotBlank()) {
                    Text("Message: \"${item.customText}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("x${item.quantity}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("$${String.format(Locale.US, "%.2f", item.lineTotalPrice)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}