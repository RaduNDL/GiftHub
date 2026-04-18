@file:Suppress("SpellCheckingInspection")

package com.example.gifthub.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
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
                                        cardNumber = parsed.last4
                                        expiry = parsed.expiry.filter { it.isDigit() }
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
        CardFormDialog(
            isEditing = editingMethod != null,
            cardholderName = cardholderName,
            onCardholderNameChange = { cardholderName = it.take(40) },
            cardNumber = cardNumber,
            onCardNumberChange = { cardNumber = it.filter { ch -> ch.isDigit() }.take(16) },
            expiry = expiry,
            onExpiryChange = { expiry = it.filter { ch -> ch.isDigit() }.take(4) },
            cvv = cvv,
            onCvvChange = { cvv = it.filter { ch -> ch.isDigit() }.take(4) },
            nickname = nickname,
            onNicknameChange = { nickname = it.take(30) },
            onDismiss = { showDialog = false },
            onSave = {
                val brand = detectCardBrand(cardNumber)
                val last4 = cardNumber.takeLast(4)
                val formattedExpiry = formatExpiryForDisplay(expiry)
                val builtMethod = buildSavedMethodString(
                    brand = brand,
                    last4 = last4,
                    cardholderName = cardholderName,
                    expiry = formattedExpiry,
                    nickname = nickname
                )

                viewModel.savePaymentMethod(
                    transactionId = editingMethod?.transactionId.orEmpty(),
                    method = builtMethod,
                    paymentStatus = "Active"
                )
                showDialog = false
            }
        )
    }
}

@Composable
private fun CardFormDialog(
    isEditing: Boolean,
    cardholderName: String,
    onCardholderNameChange: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    expiry: String,
    onExpiryChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val cardBrand = detectCardBrand(cardNumber)
    val isValid = validateCardForm(cardholderName, cardNumber, expiry, cvv)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Card" else "Add New Card",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardPreview(
                    brand = cardBrand,
                    cardNumberDigits = cardNumber,
                    cardholderName = cardholderName,
                    expiryDigits = expiry
                )

                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = onCardholderNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cardholder Name") },
                    placeholder = { Text("John Doe") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    colors = cardFieldColors()
                )

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = onCardNumberChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Card Number") },
                    placeholder = { Text("1234 5678 9012 3456") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = CardNumberVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = cardFieldColors()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = onExpiryChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Expiry") },
                        placeholder = { Text("MM/YY") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = ExpiryVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Next
                        ),
                        colors = cardFieldColors()
                    )

                    OutlinedTextField(
                        value = cvv,
                        onValueChange = onCvvChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Next
                        ),
                        colors = cardFieldColors()
                    )
                }

                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nickname (optional)") },
                    placeholder = { Text("Personal / Work") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    colors = cardFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onSave()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(text = if (isEditing) "Update Card" else "Save Card", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun cardFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun CardPreview(
    brand: String,
    cardNumberDigits: String,
    cardholderName: String,
    expiryDigits: String
) {
    val maskedDisplay = buildMaskedDisplay(cardNumberDigits)
    val expiryDisplay = if (expiryDigits.length >= 3) {
        "${expiryDigits.take(2)}/${expiryDigits.drop(2)}"
    } else if (expiryDigits.isNotEmpty()) {
        expiryDigits
    } else {
        "MM/YY"
    }

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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = "Card",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = brand,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.size(22.dp))
                Text(
                    text = maskedDisplay,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (cardholderName.isBlank()) "CARDHOLDER NAME" else cardholderName.uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = expiryDisplay,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun buildMaskedDisplay(digits: String): String {
    val padded = digits.padEnd(16, '•')
    val groups = padded.chunked(4)
    if (digits.length < 12) {
        return groups.joinToString("  ") { group ->
            group.map { ch -> if (ch.isDigit()) ch else '•' }.joinToString("")
        }
    }
    val last4 = digits.takeLast(4)
    return "••••  ••••  ••••  $last4"
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

private class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(16)
        val formatted = buildString {
            trimmed.forEachIndexed { index, c ->
                if (index > 0 && index % 4 == 0) append(' ')
                append(c)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val spaces = (offset - 1) / 4
                return (offset + spaces).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val spaces = offset / 5
                return (offset - spaces).coerceIn(0, trimmed.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

private class ExpiryVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(4)
        val formatted = when {
            trimmed.length <= 2 -> trimmed
            else -> "${trimmed.substring(0, 2)}/${trimmed.substring(2)}"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    else -> (offset + 1).coerceAtMost(formatted.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    else -> (offset - 1).coerceIn(0, trimmed.length)
                }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

private data class ParsedCard(
    val brand: String,
    val last4: String,
    val cardholderName: String,
    val expiry: String,
    val nickname: String,
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
        rawNumber.startsWith("6") -> "DISCOVER"
        else -> "CARD"
    }
}

private fun formatExpiryForDisplay(digits: String): String {
    val clean = digits.filter { it.isDigit() }.take(4)
    return when {
        clean.length <= 2 -> clean
        else -> "${clean.substring(0, 2)}/${clean.substring(2)}"
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
    if (expiry.length != 4) return false

    val month = expiry.substring(0, 2).toIntOrNull() ?: return false
    if (month !in 1..12) return false

    val year = expiry.substring(2, 4).toIntOrNull() ?: return false
    if (year !in 0..99) return false

    if (cvv.length !in 3..4) return false
    return true
}