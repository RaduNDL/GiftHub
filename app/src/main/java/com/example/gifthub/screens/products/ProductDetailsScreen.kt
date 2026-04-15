package com.example.gifthub.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ImageNotSupported
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
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.viewmodel.CartViewModel
import com.example.gifthub.viewmodel.ProductViewModel
import java.util.Locale

private val AccentOrange = Color(0xFFFF6B35)
private val AccentAmber = Color(0xFFFFB347)
private val DarkBg = Color(0xFF0F0F23)
private val DarkSurface = Color(0xFF1A1A2E)
private val CardSurface = Color(0xFF16213E)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFFB0B0C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: String,
    onNavigate: (String) -> Unit,
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    var quantity by remember { mutableStateOf(1) }

    LaunchedEffect(productId) {
        productViewModel.loadProductById(productId)
    }

    val product = productViewModel.selectedProduct
    val isLoading = productViewModel.isLoading
    val error = productViewModel.errorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentOrange)
                }
            }

            product == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎁", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            error ?: "Product not found",
                            color = Color(0xFFFF6B6B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onNavigate(GiftHubDestinations.PRODUCTS) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Back to products", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero image section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        if (product.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.name,
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
                                    imageVector = Icons.Outlined.ImageNotSupported,
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    tint = Color(0xFF404060)
                                )
                            }
                        }

                        // Top-to-bottom dark gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to DarkBg.copy(alpha = 0.4f),
                                            0.5f to Color.Transparent,
                                            1.0f to DarkSurface
                                        )
                                    )
                                )
                        )

                        // Back button
                        IconButton(
                            onClick = { onNavigate(GiftHubDestinations.PRODUCTS) },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkBg.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        // Customizable badge on image
                        if (product.customizable) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(AccentOrange, AccentAmber))
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✦ Customizable",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Content card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-24).dp)
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp)
                        ) {
                            // Name + price row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = product.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f),
                                    lineHeight = 30.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", product.price)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentAmber
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Stock indicator
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (product.stock > 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (product.stock > 0) "In stock · ${product.stock} available" else "Out of stock",
                                    fontSize = 13.sp,
                                    color = if (product.stock > 0) Color(0xFF81C784) else Color(0xFFFF8A80),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Description
                            if (product.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "About this gift",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = product.description,
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    lineHeight = 22.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF252545))
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Quantity row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Quantity",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Max ${product.stock}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF252545))
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (quantity > 1) AccentOrange.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Minus",
                                            tint = if (quantity > 1) AccentOrange else Color(0xFF505070),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = quantity.toString(),
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )

                                    IconButton(
                                        onClick = { if (quantity < product.stock) quantity++ },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (quantity < product.stock) AccentOrange.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Plus",
                                            tint = if (quantity < product.stock) AccentOrange else Color(0xFF505070),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Total price preview
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF1E1E3A))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", fontSize = 14.sp, color = TextSecondary)
                                Text(
                                    "$${String.format(Locale.US, "%.2f", product.price * quantity)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentAmber
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // CTA Button
                            Button(
                                onClick = {
                                    if (product.customizable) {
                                        onNavigate(GiftHubDestinations.productCustomization(product.idProduct))
                                    } else {
                                        cartViewModel.addToCart(product, quantity)
                                    }
                                },
                                enabled = product.stock > 0 && !cartViewModel.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color(0xFF252545)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            if (product.stock > 0)
                                                Brush.horizontalGradient(listOf(AccentOrange, AccentAmber))
                                            else
                                                Brush.horizontalGradient(listOf(Color(0xFF303050), Color(0xFF303050)))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cartViewModel.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text(
                                            text = when {
                                                product.stock == 0 -> "Out of Stock"
                                                product.customizable -> "✦ Customize & Add to Cart"
                                                else -> "🛒 Add to Cart"
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (product.stock > 0) Color.White else Color(0xFF606080)
                                        )
                                    }
                                }
                            }

                            // Success / error message
                            cartViewModel.userMessage?.let {
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1B3A2A))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        it,
                                        color = Color(0xFF81C784),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}