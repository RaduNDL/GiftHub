package com.example.gifthub.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gifthub.viewmodel.CartViewModel
import com.example.gifthub.viewmodel.ProductCustomizationViewModel

@Composable
fun ProductCustomizationScreen(
    productId: String,
    viewModel: ProductCustomizationViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onAddedToCart: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    if (state.successAdded) {
        LaunchedEffect(Unit) {
            onAddedToCart()
        }
    }

    // ✅ ERROR STATE - Show error dialog instead of crashing
    if (state.error != null && state.product == null) {
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Error Loading Product", style = MaterialTheme.typography.titleLarge) },
            text = { Text(state.error ?: "Unknown error occurred") },
            confirmButton = {
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        )
        return
    }

    if (state.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val product = state.product
    if (product == null) {
        // ✅ Fallback UI if product is null (should rarely happen now)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Product not found", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text("Customize Product", style = MaterialTheme.typography.headlineMedium)
        }

        Text(product.name, style = MaterialTheme.typography.headlineMedium)
        Text(product.description)
        Text("Base Price: $${String.format("%.2f", product.price)}")

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(product.customizationOptions) { option ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(option.name, style = MaterialTheme.typography.titleMedium)
                        val selected = state.selectedByOption[option.id].orEmpty()

                        option.values.forEach { value ->
                            val isChecked = value.id in selected
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (option.maxSelection > 1) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            viewModel.onToggleValue(option.id, value.id, true)
                                        }
                                    )
                                } else {
                                    RadioButton(
                                        selected = isChecked,
                                        onClick = {
                                            viewModel.onToggleValue(option.id, value.id, false)
                                        }
                                    )
                                }
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(value.label)
                                    if (value.extraPrice > 0) {
                                        Text("+${String.format("%.2f", value.extraPrice)} lei", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { viewModel.setQuantity(state.quantity - 1) }) { Text("-") }
            Text("Quantity: ${state.quantity}")
            TextButton(onClick = { viewModel.setQuantity(state.quantity + 1) }) { Text("+") }
        }

        Text("Total: $${String.format("%.2f", state.totalPrice)}", style = MaterialTheme.typography.headlineSmall)

        // ✅ Show loading or error state for add to cart button
        if (state.error != null && state.product != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    state.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = { viewModel.addToCartWithCartViewModel(cartViewModel) },
            enabled = !cartViewModel.isLoading && state.error == null,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (cartViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adding...")
            } else {
                Text("Add to Cart")
            }
        }
    }
}