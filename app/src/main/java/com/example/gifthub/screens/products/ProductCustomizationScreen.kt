package com.example.gifthub.screens.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gifthub.models.CustomizationOptionDto
import com.example.gifthub.viewmodel.CartViewModel
import com.example.gifthub.viewmodel.ProductCustomizationViewModel
import java.util.Locale

private val AccentOrange = Color(0xFFFF6B35)
private val AccentAmber = Color(0xFFFFB347)
private val DarkSurface = Color(0xFF1A1A2E)
private val CardSurface = Color(0xFF16213E)

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
            CircularProgressIndicator(color = AccentOrange)
        }
        return
    }

    val product = state.product
    if (product == null) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F0F23), DarkSurface)
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "Customize Gift",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Live Preview
                item {
                    PreviewCard(state.previewImageUrl, product.name)
                }

                // Product Info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardSurface)
                            .padding(16.dp)
                    ) {
                        Text(
                            product.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Base Price: $${String.format(Locale.US, "%.2f", product.price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Customization Options
                items(product.customizationOptions) { option ->
                    CustomizationOptionCard(
                        option = option,
                        state = state,
                        onToggleValue = { valueId ->
                            viewModel.onToggleValue(option.id, valueId, option.maxSelection > 1)
                        },
                        onTextChange = { text -> viewModel.setTextInput(option.id, text) },
                        onImageUpload = { imageUrl -> viewModel.setUploadedImage(option.id, imageUrl) }
                    )
                }

                // Quantity
                item {
                    QuantityCard(state.quantity) { viewModel.setQuantity(it) }
                }

                // Total Price
                item {
                    TotalPriceCard(state.totalPrice)
                }

                // Error
                if (state.error != null) {
                    item {
                        Surface(
                            color = Color(0xFFD32F2F).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                state.error ?: "Unknown error",
                                color = Color(0xFFFF8A80),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Add to Cart Button
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = { viewModel.addToCartWithCartViewModel(cartViewModel) },
                        enabled = !cartViewModel.isLoading && state.error == null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(AccentOrange, AccentAmber))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cartViewModel.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Add to Cart - $${String.format(Locale.US, "%.2f", state.totalPrice)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(imageUrl: String, productName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E1E3A), Color(0xFF252545))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF505080)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.7f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Text(
                "Live Preview",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun CustomizationOptionCard(
    option: CustomizationOptionDto,
    state: com.example.gifthub.viewmodel.ProductCustomizationUiState,
    onToggleValue: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onImageUpload: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    option.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (option.required) {
                    Text(
                        " *",
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (option.type) {
                "image" -> {
                    ImageUploadOption(
                        option = option,
                        uploadedImageUrl = state.uploadedImages[option.id] ?: "",
                        onImageUpload = onImageUpload
                    )
                }
                "text" -> {
                    TextInputOption(
                        option = option,
                        currentText = state.textInputs[option.id] ?: "",
                        onTextChange = onTextChange
                    )
                }
                else -> {
                    SelectionOption(
                        option = option,
                        selectedIds = state.selectedByOption[option.id] ?: emptySet(),
                        onToggleValue = onToggleValue
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageUploadOption(
    option: CustomizationOptionDto,
    uploadedImageUrl: String,
    onImageUpload: (String) -> Unit
) {
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageUpload(uri.toString())
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uploadedImageUrl.isNotBlank()) {
            AsyncImage(
                model = uploadedImageUrl,
                contentDescription = option.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Button(
            onClick = { imageLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentOrange.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (uploadedImageUrl.isNotBlank()) "Change Image" else "Upload Image",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TextInputOption(
    option: CustomizationOptionDto,
    currentText: String,
    onTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = currentText,
        onValueChange = onTextChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(option.name) },
        placeholder = { Text("Enter text...") },
        shape = RoundedCornerShape(10.dp),
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = AccentOrange,
            unfocusedBorderColor = Color(0xFF353560)
        )
    )
}

@Composable
private fun SelectionOption(
    option: CustomizationOptionDto,
    selectedIds: Set<String>,
    onToggleValue: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        option.values.forEach { value ->
            val isSelected = value.id in selectedIds

            OutlinedButton(
                onClick = { onToggleValue(value.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) AccentOrange.copy(alpha = 0.2f) else Color.Transparent,
                    contentColor = if (isSelected) AccentOrange else Color.White
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) AccentOrange else Color(0xFF353560)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(value.label, fontWeight = FontWeight.Medium)
                    if (value.extraPrice > 0) {
                        Text(
                            "+$${String.format(Locale.US, "%.2f", value.extraPrice)}",
                            fontSize = 12.sp,
                            color = AccentAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityCard(quantity: Int, onQuantityChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Quantity:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1E3A))
                    .padding(6.dp)
            ) {
                Button(
                    onClick = { onQuantityChange(quantity - 1) },
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Text(
                    quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.width(28.dp)
                )

                Button(
                    onClick = { onQuantityChange(quantity + 1) },
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TotalPriceCard(totalPrice: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, AccentOrange)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Total Price:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "$${String.format(Locale.US, "%.2f", totalPrice)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = AccentAmber
            )
        }
    }
}