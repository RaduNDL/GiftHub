package com.example.gifthub.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    if (state.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val product = state.product ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        Text(product.name, style = MaterialTheme.typography.headlineMedium)
        Text(product.description)
        Text("Base Price: ${product.price}")

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
                                        Text("+${value.extraPrice} lei", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            TextButton(onClick = { viewModel.setQuantity(state.quantity - 1) }) { Text("-") }
            Text("Quantity: ${state.quantity}")
            TextButton(onClick = { viewModel.setQuantity(state.quantity + 1) }) { Text("+") }
        }

        Text("Total: ${state.totalPrice}", style = MaterialTheme.typography.headlineSmall)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.addToCartWithCartViewModel(cartViewModel) }
        ) {
            Text("Add to Cart")
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
