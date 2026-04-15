package com.example.gifthub.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gifthub.models.ProductDto
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.ui.components.GiftHubBottomBar
import com.example.gifthub.viewmodel.AuthViewModel
import com.example.gifthub.viewmodel.CategoryViewModel
import com.example.gifthub.viewmodel.FavoriteViewModel
import com.example.gifthub.viewmodel.ProductViewModel
import com.example.gifthub.viewmodel.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import kotlin.math.floor

private val AccentOrange = Color(0xFFFF6B35)
private val AccentAmber = Color(0xFFFFB347)
private val DarkSurface = Color(0xFF1A1A2E)
private val CardSurface = Color(0xFF16213E)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFFB0B0C0)
private val NeonCyan = Color(0xFF00F0FF)
private val StarGold = Color(0xFFFFD700)
private val ElectricBlue = Color(0xFF0055FF)

private fun roundToHalf(value: Double): Double {
    return floor(value * 2) / 2.0
}

private fun meetsRatingFilter(productRating: Double, selectedRating: Int): Boolean {
    if (selectedRating == 0) return true
    val roundedRating = roundToHalf(productRating)
    return roundedRating >= selectedRating
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel,
    selectedCategoryId: String? = null,
    selectedCategoryName: String? = null,
    productViewModel: ProductViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel()
) {
    var searchText by remember { mutableStateOf("") }
    var activeCategoryId by remember(selectedCategoryId) { mutableStateOf(selectedCategoryId ?: "") }
    var showCategories by remember { mutableStateOf(false) }
    var profileMenuExpanded by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableIntStateOf(0) }
    var showRatingFilter by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val isEmployee = authViewModel.currentUserRole == "employee"

    val products = productViewModel.productsList
    val categories = categoryViewModel.categoriesList
    val favoriteIds = favoriteViewModel.favoriteProductIds

    // Încarcă produse, categorii ȘI toate recenziile la intrarea pe ecran.
    // IMPORTANT: folosim fetchAllReviews() (o singură interogare) în loc de
    // a apela fetchReviews(productId) într-un loop, fiindcă acela suprascria
    // reviewsList la fiecare produs și pierdeam datele.
    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
        categoryViewModel.loadCategories()
        reviewViewModel.fetchAllReviews()
    }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            favoriteViewModel.loadFavoriteIds(userId)
        }
    }

    // Calculăm ratingul mediu pentru fiecare produs din LISTA GLOBALĂ allReviews.
    val productRatings = remember(products, reviewViewModel.allReviews) {
        val grouped = reviewViewModel.allReviews.groupBy { it.productId }
        products.associate { product ->
            val reviewsForProduct = grouped[product.idProduct].orEmpty()
            val averageRating = if (reviewsForProduct.isNotEmpty()) {
                roundToHalf(reviewsForProduct.map { it.rating }.average())
            } else {
                0.0
            }
            product.idProduct to averageRating
        }
    }

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.name.contains(searchText, ignoreCase = true)
        val matchesCategory = activeCategoryId.isBlank() || product.categoryId == activeCategoryId
        val productRating = productRatings[product.idProduct] ?: 0.0
        val matchesRating = meetsRatingFilter(productRating, selectedRating)
        matchesSearch && matchesCategory && matchesRating
    }

    Scaffold(
        containerColor = Color(0xFF05050A),
        bottomBar = {
            GiftHubBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F0F23),
                            DarkSurface
                        )
                    )
                )
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 32.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            IconButton(
                                onClick = { profileMenuExpanded = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF252540).copy(alpha = 0.6f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Profile",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = profileMenuExpanded,
                                onDismissRequest = { profileMenuExpanded = false },
                                modifier = Modifier.background(CardSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile Page", color = TextPrimary) },
                                    onClick = {
                                        profileMenuExpanded = false
                                        onNavigate(GiftHubDestinations.PROFILE)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout", color = Color(0xFFFF0055)) },
                                    onClick = {
                                        profileMenuExpanded = false
                                        authViewModel.logout()
                                        onNavigate(GiftHubDestinations.LOGIN)
                                    }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(
                                onClick = { onNavigate(GiftHubDestinations.FAVORITES) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF252540).copy(alpha = 0.6f))
                                    .border(1.dp, AccentOrange.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorites",
                                    tint = AccentOrange
                                )
                            }

                            IconButton(
                                onClick = { onNavigate(GiftHubDestinations.CART) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF252540).copy(alpha = 0.6f))
                                    .border(1.dp, AccentAmber.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = AccentAmber
                                )
                            }
                        }
                    }
                }

                // Title Section
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            text = if (activeCategoryId.isBlank()) "Explore Gifts" else (selectedCategoryName ?: "Category"),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "${filteredProducts.size} items found",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        // Search Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF16213E).copy(alpha = 0.7f))
                                .border(1.dp, Color(0xFF353560), RoundedCornerShape(24.dp))
                        ) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "Search gifts...",
                                        color = TextSecondary,
                                        fontSize = 15.sp
                                    )
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = AccentOrange
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Filter Chips Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Filter
                            GiftCategoryExpandableChip(
                                label = if (activeCategoryId.isBlank()) "All Categories" else categories.firstOrNull { it.categoryId == activeCategoryId }?.name ?: "All Categories",
                                isExpanded = showCategories,
                                onClick = { showCategories = !showCategories }
                            )

                            if (showCategories) {
                                if (activeCategoryId.isNotBlank()) {
                                    GiftCategoryChip(
                                        label = "Clear Filter",
                                        onClick = {
                                            activeCategoryId = ""
                                            showCategories = false
                                        }
                                    )
                                }

                                categories.filter { it.categoryId != activeCategoryId }.forEach { category ->
                                    GiftCategoryChip(
                                        label = category.name,
                                        onClick = {
                                            activeCategoryId = category.categoryId
                                            showCategories = false
                                        }
                                    )
                                }
                            }

                            // Rating Filter Chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (showRatingFilter)
                                            Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                                        else
                                            SolidColor(Color(0xFF16213E).copy(alpha = 0.8f))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (showRatingFilter) Color.Transparent else Color(0xFF353560),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable { showRatingFilter = !showRatingFilter }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = StarGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (selectedRating == 0) "Rating" else "$selectedRating+ Stars",
                                        color = if (showRatingFilter) Color.White else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (showRatingFilter) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0, 1, 2, 3, 4, 5).forEach { rating ->
                                    RatingFilterChip(
                                        rating = rating,
                                        isSelected = selectedRating == rating,
                                        onClick = {
                                            selectedRating = rating
                                            showRatingFilter = false
                                        }
                                    )
                                }
                            }
                        }

                        if (isEmployee) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { onNavigate(GiftHubDestinations.ADD_PRODUCT) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = AccentOrange),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.horizontalGradient(listOf(AccentOrange, AccentAmber))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Product",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add New Product", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Error/Success Messages
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        productViewModel.errorMessage?.let { err ->
                            Text(
                                text = err,
                                color = Color(0xFFFF0055),
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        favoriteViewModel.userMessage?.let { message ->
                            Text(
                                text = message,
                                color = NeonCyan,
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Loading/Empty State
                if (productViewModel.isLoading && filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonCyan)
                        }
                    }
                } else if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🕳️", fontSize = 56.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "The void is empty",
                                    color = TextSecondary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    items(filteredProducts) { product ->
                        val isFavorite = favoriteIds.contains(product.idProduct)
                        val productRating = productRatings[product.idProduct] ?: 0.0

                        ProductGridCard(
                            product = product,
                            isFavorite = isFavorite,
                            isEmployee = isEmployee,
                            averageRating = productRating,
                            onFavoriteClick = {
                                favoriteViewModel.toggleFavorite(userId, product)
                            },
                            onClick = {
                                val safeId = product.idProduct.trim()
                                if (safeId.isNotBlank()) {
                                    onNavigate(GiftHubDestinations.productDetails(safeId))
                                } else {
                                    productViewModel.errorMessage = "Product ID missing for ${product.name}"
                                }
                            },
                            onEdit = {
                                onNavigate(GiftHubDestinations.editProduct(product.idProduct))
                            },
                            onDelete = {
                                productViewModel.deleteProduct(product.idProduct)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingFilterChip(
    rating: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) StarGold else Color(0xFF16213E).copy(alpha = 0.8f))
            .border(
                width = 1.5.dp,
                color = if (isSelected) StarGold else Color(0xFF353560),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (rating > 0) {
                repeat(rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else StarGold,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = "& Up",
                    color = if (isSelected) Color.Black else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "All",
                    color = if (isSelected) Color.Black else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GiftCategoryExpandableChip(
    label: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(AccentOrange, AccentAmber)))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isExpanded) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (!isExpanded) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GiftCategoryChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF16213E).copy(alpha = 0.8f))
            .border(
                width = 1.dp,
                color = Color(0xFF353560),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProductGridCard(
    product: ProductDto,
    isFavorite: Boolean,
    isEmployee: Boolean,
    averageRating: Double,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(CardSurface)
            .border(1.dp, AccentOrange.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
            .clickable { onClick() }
            .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = AccentOrange.copy(alpha = 0.3f), clip = false)
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
                    modifier = Modifier.size(42.dp),
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
                            0.45f to Color.Transparent,
                            1.0f to Color(0xFB05050A)
                        )
                    )
                )
        )

        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x9905050A))
                .border(1.dp, if (isFavorite) Color(0xFFFF4D6D) else Color(0xFF353560), CircleShape)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) Color(0xFFFF4D6D) else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEmployee) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xCC1A1A2E))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(50)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Color(0xFF353560))
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF0055),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (product.stock in 1..5) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentOrange.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Only ${product.stock}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else if (product.stock == 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCCFF0055))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Sold out",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Rating Badge - Display Decimal Ratings
        if (averageRating > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(listOf(StarGold, AccentAmber))
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", averageRating),
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$${String.format(Locale.US, "%.2f", product.price)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan
            )
        }
    }
}