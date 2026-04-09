package com.example.gifthub.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gifthub.models.AddressDto
import com.example.gifthub.models.PaymentMethodDto
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.viewmodel.AddressViewModel
import com.example.gifthub.viewmodel.CartViewModel
import com.example.gifthub.viewmodel.OrderViewModel
import com.example.gifthub.viewmodel.PaymentMethodViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CheckoutScreen(
    onNavigate: (String) -> Unit,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel = viewModel(),
    addressViewModel: AddressViewModel = viewModel(),
    paymentMethodViewModel: PaymentMethodViewModel = viewModel()
) {
    val cart = cartViewModel.cart
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var selectedAddressId by remember { mutableStateOf("") }
    var selectedPaymentId by remember { mutableStateOf("") }

    var useManualAddress by remember { mutableStateOf(false) }
    var useManualPayment by remember { mutableStateOf(false) }

    var manualAddress by remember { mutableStateOf("") }
    var manualPaymentMethod by remember { mutableStateOf("Cash on Delivery") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var placedOrderId by remember { mutableStateOf("") }

    val addresses = addressViewModel.addresses
    val paymentMethods = paymentMethodViewModel.paymentMethods

    val selectedAddress = addresses.firstOrNull { it.idAddress == selectedAddressId }
    val selectedPayment = paymentMethods.firstOrNull { it.transactionId == selectedPaymentId }

    val finalAddress = if (useManualAddress || addresses.isEmpty()) {
        manualAddress.trim()
    } else {
        selectedAddress?.let { "${it.street}, ${it.city}, ${it.zipcode}" }.orEmpty()
    }

    val finalPaymentMethod = if (useManualPayment || paymentMethods.isEmpty()) {
        manualPaymentMethod.trim()
    } else {
        selectedPayment?.method.orEmpty()
    }

    val canPlaceOrder = cart.items.isNotEmpty() &&
            !orderViewModel.isLoading &&
            finalAddress.isNotBlank() &&
            finalPaymentMethod.isNotBlank()

    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
        addressViewModel.loadAddresses()
        paymentMethodViewModel.loadPaymentMethods()
        orderViewModel.clearError()
        orderViewModel.clearUserMessage()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cartViewModel.loadCart()
                addressViewModel.loadAddresses()
                paymentMethodViewModel.loadPaymentMethods()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(addresses) {
        if (addresses.isEmpty()) {
            useManualAddress = true
        } else if (!useManualAddress && selectedAddressId.isBlank()) {
            selectedAddressId = addresses.first().idAddress
        }
    }

    LaunchedEffect(paymentMethods) {
        if (paymentMethods.isEmpty()) {
            useManualPayment = true
        } else if (!useManualPayment && selectedPaymentId.isBlank()) {
            selectedPaymentId = paymentMethods.first().transactionId
        }
    }

    LaunchedEffect(cartViewModel.userMessage) {
        cartViewModel.userMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            cartViewModel.clearUserMessage()
        }
    }

    LaunchedEffect(orderViewModel.errorMessage) {
        orderViewModel.errorMessage?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    LaunchedEffect(orderViewModel.userMessage) {
        orderViewModel.userMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Order placed successfully") },
            text = {
                Text(
                    "Your order #${placedOrderId.take(8).uppercase()} has been placed successfully. " +
                            "You can track it anytime from Order History."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    onNavigate(GiftHubDestinations.ORDER_HISTORY)
                }) { Text("View Orders") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onNavigate(GiftHubDestinations.PRODUCTS)
                }) { Text("Continue Shopping") }
            }
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
                    IconButton(onClick = { onNavigate(GiftHubDestinations.CART) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Column {
                        Text(
                            text = "Checkout",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Review, confirm and place your order",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (cart.items.isEmpty() && !cartViewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Your cart is empty",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Add products before continuing to checkout.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { onNavigate(GiftHubDestinations.PRODUCTS) },
                                shape = RoundedCornerShape(16.dp)
                            ) { Text("Go to Products") }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    HeroCheckoutCard(total = cart.temporaryValue)
                    SectionTitle("Delivery Address")

                    if (addresses.isNotEmpty()) {
                        addresses.forEach { address ->
                            AddressSelectionCard(
                                address = address,
                                isSelected = !useManualAddress && selectedAddressId == address.idAddress,
                                onClick = {
                                    selectedAddressId = address.idAddress
                                    useManualAddress = false
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    useManualAddress = !useManualAddress
                                    if (!useManualAddress && selectedAddressId.isBlank() && addresses.isNotEmpty()) {
                                        selectedAddressId = addresses.first().idAddress
                                    }
                                }
                            ) {
                                Text(
                                    if (useManualAddress) "Use saved address"
                                    else "Use another address"
                                )
                            }

                            TextButton(
                                onClick = { onNavigate(GiftHubDestinations.MANAGE_ADDRESS) }
                            ) {
                                Text("Manage addresses")
                            }
                        }
                    }

                    if (useManualAddress || addresses.isEmpty()) {
                        OutlinedTextField(
                            value = manualAddress,
                            onValueChange = { manualAddress = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Enter delivery address") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3
                        )
                    }

                    SectionTitle("Payment Method")

                    if (paymentMethods.isNotEmpty()) {
                        paymentMethods.forEach { payment ->
                            PaymentSelectionCard(
                                payment = payment,
                                isSelected = !useManualPayment && selectedPaymentId == payment.transactionId,
                                onClick = {
                                    selectedPaymentId = payment.transactionId
                                    useManualPayment = false
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    useManualPayment = !useManualPayment
                                    if (!useManualPayment && selectedPaymentId.isBlank() && paymentMethods.isNotEmpty()) {
                                        selectedPaymentId = paymentMethods.first().transactionId
                                    }
                                }
                            ) {
                                Text(
                                    if (useManualPayment) "Use saved payment"
                                    else "Use another payment"
                                )
                            }

                            TextButton(
                                onClick = { onNavigate(GiftHubDestinations.SAVED_PAYMENTS) }
                            ) {
                                Text("Manage payments")
                            }
                        }
                    }

                    if (useManualPayment || paymentMethods.isEmpty()) {
                        OutlinedTextField(
                            value = manualPaymentMethod,
                            onValueChange = { manualPaymentMethod = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Payment method") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }

                    SectionTitle("Order Summary")

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            cart.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "Qty: ${item.quantity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        "$${String.format(Locale.US, "%.2f", item.price * item.quantity)}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "$${String.format(Locale.US, "%.2f", cart.temporaryValue)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "Confirmation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                if (finalAddress.isBlank())
                                    "Delivery address not selected yet."
                                else
                                    "Address: $finalAddress",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                if (finalPaymentMethod.isBlank())
                                    "Payment method not selected yet."
                                else
                                    "Payment: $finalPaymentMethod",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigate(GiftHubDestinations.CART) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Back") }

                        Button(
                            onClick = {
                                orderViewModel.placeOrder(
                                    cart = cart,
                                    address = finalAddress,
                                    paymentMethod = finalPaymentMethod,
                                    onSuccess = { orderId ->
                                        cartViewModel.loadCart()
                                        placedOrderId = orderId
                                        showSuccessDialog = true
                                    },
                                    onError = { error ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = error,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = canPlaceOrder,
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            if (orderViewModel.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Placing...")
                            } else {
                                Text(
                                    "Place Order",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroCheckoutCard(total: Double) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Almost done",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Review your details and confirm the order",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total to pay",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$${String.format(Locale.US, "%.2f", total)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun AddressSelectionCard(
    address: AddressDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Address",
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    address.street,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${address.city}, ${address.zipcode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PaymentSelectionCard(
    payment: PaymentMethodDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = "Payment",
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.method.ifBlank { "Unknown method" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = payment.paymentStatus.ifBlank { "Saved" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}