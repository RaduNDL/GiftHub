package com.example.gifthub.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gifthub.viewmodel.CartViewModel
import com.example.gifthub.viewmodel.ProductViewModel

@Composable
fun ProductDetailsScreen(
    productId: String,
    onBack: () -> Unit,
    viewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    onGoToCart: () -> Unit,
    onCustomize: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadProductById(productId)
    }

    val product = uiState.selectedProduct

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
            return@Column
        }

        if (product == null) {
            Text("Product not found")
            return@Column
        }

        Text(product.name)
        Text(product.description)
        Text("Price: ${product.price}")
        Text("Stock: ${product.stock}")

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                cartViewModel.addToCart(product)
            }
        ) {
            Text("Add to cart")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCustomize,
            enabled = product.customizable
        ) {
            Text("Customize gift")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToCart
        ) {
            Text("Go to cart")
        }

        uiState.error?.let { Text(it) }
    }
}