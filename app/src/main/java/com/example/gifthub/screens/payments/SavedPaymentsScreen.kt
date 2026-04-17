package com.example.gifthub.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gifthub.models.PaymentMethodDto
import com.example.gifthub.viewmodel.PaymentMethodViewModel

@Composable
fun SavedPaymentsScreen(
    onBack: () -> Unit,
    viewModel: PaymentMethodViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(false) }
    var editingMethod by remember { mutableStateOf<PaymentMethodDto?>(null) }

    var cardholderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadPaymentMethods()
    }

    LaunchedEffect(viewModel.userMessage) {
        viewModel.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingMethod = null
                    cardholderName = ""
                    cardNumber = ""
                    expiry = ""
                    cvv = ""
                    nickname = ""
                    showDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add card") },
                text = { Text("Add Card") }
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
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text("Saved Cards", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Manage your payment methods", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))

                viewModel.errorMessage?.let { message ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                when {
                    viewModel.isLoading && viewModel.paymentMethods.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    viewModel.paymentMethods.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No cards saved yet.\nTap Add Card to get started.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(viewModel.paymentMethods, key = { it.transactionId }) { payment ->
                                PaymentCard(
                                    payment = payment,
                                    onEdit = {
                                        editingMethod = payment
                                        val parsed = parseSavedMethod(payment.method)
                                        cardholderName = parsed.cardholderName
                                        cardNumber = parsed.cardNumberRaw
                                        expiry = parsed.expiry
                                        cvv = ""
                                        nickname = parsed.nickname
                                        showDialog = true
                                    },
                                    onDelete = { viewModel.deletePaymentMethod(payment.transactionId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        val cardBrand = detectCardBrand(cardNumber)
        val maskedPreview = maskCardNumber(formatCardNumber(cardNumber))
        val isValid = validateCardForm(cardholderName, cardNumber, expiry, cvv)

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(if (editingMethod == null) "Add New Card" else "Edit Card")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = "Card",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = cardBrand,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (cardNumber.isBlank()) "•••• •••• •••• ••••" else maskedPreview,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (cardholderName.isBlank()) "CARDHOLDER NAME" else cardholderName.uppercase(),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = if (expiry.isBlank()) "MM/YY" else expiry,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cardholderName,
                        onValueChange = { cardholderName = it.take(40) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Cardholder Name") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = formatCardNumber(cardNumber),
                        onValueChange = { input ->
                            cardNumber = input.filter { it.isDigit() }.take(16)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Card Number") },
                        placeholder = { Text("1234 5678 9012 3456") },
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = expiry,
                            onValueChange = { input ->
                                expiry = formatExpiry(input)
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Expiry") },
                            placeholder = { Text("MM/YY") },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { input ->
                                cvv = input.filter { it.isDigit() }.take(4)
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("CVV") },
                            placeholder = { Text("123") },
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it.take(30) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nickname (optional)") },
                        placeholder = { Text("Personal / Work") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = isValid,
                    onClick = {
                        val brand = detectCardBrand(cardNumber)
                        val last4 = cardNumber.takeLast(4)
                        val builtMethod = buildSavedMethodString(
                            brand = brand,
                            last4 = last4,
                            cardholderName = cardholderName,
                            expiry = expiry,
                            nickname = nickname
                        )

                        viewModel.savePaymentMethod(
                            transactionId = editingMethod?.transactionId.orEmpty(),
                            method = builtMethod,
                            paymentStatus = "Active"
                        )
                        showDialog = false
                    }
                ) {
                    Text("Save Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PaymentCard(
    payment: PaymentMethodDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val display = parseSavedMethod(payment.method)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CreditCard,
                contentDescription = payment.method,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = display.title.ifBlank { "Saved card" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = display.subtitle.ifBlank { payment.paymentStatus.ifBlank { "No status" } },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private data class ParsedCard(
    val brand: String,
    val last4: String,
    val cardholderName: String,
    val expiry: String,
    val nickname: String,
    val cardNumberRaw: String,
    val title: String,
    val subtitle: String
)

private fun parseSavedMethod(method: String): ParsedCard {
    val parts = method.split("|").map { it.trim() }
    if (parts.size >= 5 && parts[0].startsWith("CARD")) {
        val brand = parts[1]
        val last4 = parts[2]
        val name = parts[3]
        val expiry = parts[4]
        val nickname = if (parts.size >= 6) parts[5] else ""
        val title = "$brand •••• $last4"
        val subtitle = listOf(name, expiry, nickname).filter { it.isNotBlank() }.joinToString(" • ")
        return ParsedCard(
            brand = brand,
            last4 = last4,
            cardholderName = name,
            expiry = expiry,
            nickname = nickname,
            cardNumberRaw = "",
            title = title,
            subtitle = subtitle
        )
    }

    return ParsedCard(
        brand = "",
        last4 = "",
        cardholderName = "",
        expiry = "",
        nickname = "",
        cardNumberRaw = "",
        title = method,
        subtitle = ""
    )
}

private fun buildSavedMethodString(
    brand: String,
    last4: String,
    cardholderName: String,
    expiry: String,
    nickname: String
): String {
    return listOf(
        "CARD",
        brand,
        last4,
        cardholderName.trim(),
        expiry.trim(),
        nickname.trim()
    ).joinToString("|")
}

private fun detectCardBrand(rawNumber: String): String {
    return when {
        rawNumber.startsWith("4") -> "VISA"
        rawNumber.startsWith("5") -> "MASTERCARD"
        rawNumber.startsWith("34") || rawNumber.startsWith("37") -> "AMEX"
        rawNumber.length >= 4 -> "CARD"
        else -> "CARD"
    }
}

private fun formatCardNumber(raw: String): String {
    return raw.filter { it.isDigit() }
        .chunked(4)
        .joinToString(" ")
}

private fun maskCardNumber(formatted: String): String {
    val digits = formatted.filter { it.isDigit() }
    if (digits.length < 4) return "•••• •••• •••• ••••"
    val last4 = digits.takeLast(4)
    return "•••• •••• •••• $last4"
}

private fun formatExpiry(input: String): String {
    val digits = input.filter { it.isDigit() }.take(4)
    return when {
        digits.length <= 2 -> digits
        else -> "${digits.take(2)}/${digits.drop(2)}"
    }
}

private fun validateCardForm(
    cardholderName: String,
    cardNumber: String,
    expiry: String,
    cvv: String
): Boolean {
    if (cardholderName.trim().length < 3) return false
    if (cardNumber.length !in 15..16) return false
    if (!Regex("""^\d{2}/\d{2}$""").matches(expiry)) return false

    val month = expiry.take(2).toIntOrNull() ?: return false
    if (month !in 1..12) return false

    if (cvv.length !in 3..4) return false
    return true
}