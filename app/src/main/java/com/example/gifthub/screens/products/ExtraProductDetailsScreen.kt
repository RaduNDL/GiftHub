package com.example.gifthub.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gifthub.viewmodel.ProductViewModel
import java.util.Locale

private val AccentOrange = Color(0xFFFF6B35)
private val AccentAmber = Color(0xFFFFB347)
private val DarkBg = Color(0xFF0F0F23)
private val DarkSurface = Color(0xFF1A1A2E)
private val CardSurface = Color(0xFF16213E)
private val CardAlt = Color(0xFF1E1E3A)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFFB0B0C0)
private val AccentCyan = Color(0xFF00F0FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraProductDetailsScreen(
    productId: String,
    onBack: () -> Unit,
    productViewModel: ProductViewModel = viewModel()
) {
    LaunchedEffect(productId) {
        productViewModel.loadProductById(productId)
    }

    val product = productViewModel.selectedProduct
    val isLoading = productViewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Full Product Details",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentOrange)
                }
            }

            product == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "❓", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Product not found.",
                            color = Color(0xFFFF6B6B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The scanned QR does not match\nany product in the catalog.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
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
                                        Brush.linearGradient(listOf(Color(0xFF1E1E3A), Color(0xFF252545)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎁", fontSize = 72.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.Transparent,
                                            1.0f to DarkBg
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "📷 Scanned",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = product.name,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(AccentOrange, AccentAmber))
                                    )
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", product.price)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (product.stock > 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                    )
                            )

                            Text(
                                text = if (product.stock > 0) "${product.stock} in stock" else "Out of stock",
                                fontSize = 13.sp,
                                color = if (product.stock > 0) Color(0xFF81C784) else Color(0xFFFF8A80),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (product.description.isNotBlank()) {
                            DetailSection(
                                icon = Icons.Default.Info,
                                iconTint = AccentCyan,
                                title = "About this product"
                            ) {
                                Text(
                                    text = product.description,
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        DetailSection(
                            icon = Icons.Default.Tune,
                            iconTint = AccentAmber,
                            title = "Specifications"
                        ) {
                            SpecRow(label = "Product ID", value = product.idProduct.takeLast(12).uppercase())
                            SpecRow(label = "Price", value = "$${String.format(Locale.US, "%.2f", product.price)}")
                            SpecRow(
                                label = "Availability",
                                value = if (product.stock > 0) "In stock (${product.stock} units)" else "Out of stock"
                            )
                            SpecRow(
                                label = "Customizable",
                                value = if (product.customizable) "Yes ✦" else "No"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DetailSection(
                            icon = Icons.Default.CardGiftcard,
                            iconTint = AccentOrange,
                            title = "Gift options"
                        ) {
                            if (product.customizable) {
                                GiftChip(text = "✦ Customization available")
                            }
                            GiftChip(text = "🎀 Gift wrapping included")
                            GiftChip(text = "💌 Personalized message")
                            GiftChip(text = "🛍️ Premium gift bag")
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "All products are delivered in luxury packaging, perfect for gifting.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DetailSection(
                            icon = Icons.Default.LocalShipping,
                            iconTint = Color(0xFF81C784),
                            title = "Shipping & availability"
                        ) {
                            SpecRow(label = "Standard delivery", value = "3–5 business days")
                            SpecRow(label = "Express delivery", value = "1–2 business days")
                            SpecRow(label = "Free shipping", value = "On orders over $50")
                            SpecRow(label = "Returns", value = "30 days from delivery")
                            Spacer(modifier = Modifier.height(8.dp))
                            if (product.stock > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1B3A2A))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "✅ Product available — order now!",
                                        color = Color(0xFF81C784),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF3A1B1B))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "⚠️ Currently out of stock.",
                                        color = Color(0xFFFF8A80),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DetailSection(
                            icon = Icons.Default.StarRate,
                            iconTint = AccentAmber,
                            title = "Product rating"
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "⭐", fontSize = 36.sp)
                                Column {
                                    Text(
                                        text = "Verified reviews",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Based on real buyers of this product.",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        content()
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = Color(0xFF252545), thickness = 0.5.dp)
}

@Composable
private fun GiftChip(text: String) {
    Box(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CardAlt)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = TextPrimary, fontSize = 13.sp)
    }
}