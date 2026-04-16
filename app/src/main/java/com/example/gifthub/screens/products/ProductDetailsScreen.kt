package com.example.gifthub.screens.products

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gifthub.models.ReviewDto
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.viewmodel.CartViewModel
import com.example.gifthub.viewmodel.ProductViewModel
import com.example.gifthub.viewmodel.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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
    cartViewModel: CartViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel()
) {
    var quantity by remember { mutableStateOf(1) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    LaunchedEffect(productId) {
        productViewModel.loadProductById(productId)
        reviewViewModel.fetchReviews(productId)
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
                val voucherCode = remember(product.idProduct) { "GH-${product.idProduct.takeLast(8).uppercase()}" }
                val qrPayload = remember(product.idProduct, voucherCode) {
                    "GIFTHUB|PRODUCT:${product.idProduct}|VOUCHER:$voucherCode"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
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

                            Spacer(modifier = Modifier.height(24.dp))

                            ProductVoucherQrSection(
                                voucherCode = voucherCode,
                                qrPayload = qrPayload
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF252545))
                            )

                            Spacer(modifier = Modifier.height(24.dp))

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

                            Button(
                                onClick = { onNavigate(GiftHubDestinations.REDEEM_GIFT) },
                                enabled = product.stock > 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF252545),
                                    disabledContainerColor = Color(0xFF252545)
                                )
                            ) {
                                Text(
                                    text = "🎟 Redeem with QR",
                                    fontWeight = FontWeight.Bold,
                                    color = if (product.stock > 0) Color(0xFF00F0FF) else Color(0xFF606080)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

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

                            cartViewModel.userMessage?.let { message ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1B3A2A))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        message,
                                        color = Color(0xFF81C784),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            ReviewsSection(
                                productId = productId,
                                currentUserId = currentUserId,
                                reviewViewModel = reviewViewModel
                            )

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductVoucherQrSection(
    voucherCode: String,
    qrPayload: String
) {
    val qrBitmap by remember(qrPayload) { mutableStateOf(generateQrBitmapOrNull(qrPayload, 700)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, Color(0xFF2D3A60), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Voucher QR", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Code: $voucherCode", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(12.dp))

        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "Voucher QR",
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(8.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF252545)),
                contentAlignment = Alignment.Center
            ) {
                Text("QR unavailable", color = Color(0xFFFF8A80), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Scanează acest QR în ecranul Redeem",
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

private fun generateQrBitmapOrNull(content: String, size: Int): Bitmap? {
    return try {
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ReviewsSection(
    productId: String,
    currentUserId: String,
    reviewViewModel: ReviewViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var reviewToEdit by remember { mutableStateOf<ReviewDto?>(null) }

    val reviews = reviewViewModel.reviewsList
    val userReview = reviews.find { it.userId == currentUserId }
    val averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF16213E))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Customer Reviews",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF5F5F5)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB347),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", averageRating)} (${reviews.size})",
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0C0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (userReview == null && currentUserId.isNotBlank()) {
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252545)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Write Review", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                Text("No reviews yet. Be the first!", color = Color(0xFFB0B0C0), fontSize = 14.sp)
            }
        } else {
            reviews.forEach { review ->
                ReviewCard(
                    review = review,
                    isCurrentUser = review.userId == currentUserId,
                    onEdit = {
                        reviewToEdit = review
                        showDialog = true
                    },
                    onDelete = { reviewViewModel.deleteReview(review.idReview, productId) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showDialog) {
        ReviewDialog(
            initialReview = reviewToEdit,
            onDismiss = {
                showDialog = false
                reviewToEdit = null
            },
            onSubmit = { rating, comment ->
                reviewViewModel.saveReview(productId, currentUserId, rating, comment, reviewToEdit?.idReview)
                showDialog = false
                reviewToEdit = null
            }
        )
    }
}

@Composable
fun ReviewCard(
    review: ReviewDto,
    isCurrentUser: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E3A))
            .border(1.dp, if (isCurrentUser) Color(0xFF00F0FF).copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = if (isCurrentUser) "You" else "Verified Buyer",
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) Color(0xFF00F0FF) else Color(0xFFF5F5F5),
                    fontSize = 14.sp
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFB347),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (isCurrentUser) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onEdit)
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF0055),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onDelete)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = review.comment,
            color = Color(0xFFB0B0C0),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ReviewDialog(
    initialReview: ReviewDto?,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(initialReview?.rating ?: 5) }
    var comment by remember { mutableStateOf(initialReview?.comment ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        title = {
            Text(
                text = if (initialReview == null) "Write a Review" else "Edit Review",
                color = Color(0xFFF5F5F5),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFB347),
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                                .clickable { rating = index + 1 }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFF5F5F5),
                        unfocusedTextColor = Color(0xFFF5F5F5),
                        focusedBorderColor = Color(0xFF00F0FF),
                        unfocusedBorderColor = Color(0xFF353560)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
            ) {
                Text("Submit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFB0B0C0))
            }
        }
    )
}