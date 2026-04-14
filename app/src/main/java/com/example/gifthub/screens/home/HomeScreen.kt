package com.example.gifthub.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gifthub.models.CategoryDto
import com.example.gifthub.models.ProductDto
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.ui.components.GiftHubBottomBar
import com.example.gifthub.viewmodel.AuthViewModel
import com.example.gifthub.viewmodel.CategoryViewModel
import com.example.gifthub.viewmodel.ProductViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    val isEmployee = authViewModel.currentUserRole.equals("employee", ignoreCase = true)
    val categories = categoryViewModel.categoriesList
    val featuredProducts = productViewModel.productsList.take(3)

    var profileMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()
        productViewModel.loadProducts()
    }

    Scaffold(
        bottomBar = {
            GiftHubBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
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
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with user menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(onClick = { profileMenuExpanded = true }) {
                            Icon(Icons.Default.PersonOutline, contentDescription = "Profile")
                        }

                        DropdownMenu(
                            expanded = profileMenuExpanded,
                            onDismissRequest = { profileMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile Page") },
                                onClick = {
                                    profileMenuExpanded = false
                                    onNavigate(GiftHubDestinations.PROFILE)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    profileMenuExpanded = false
                                    authViewModel.logout()
                                    onNavigate(GiftHubDestinations.LOGIN)
                                }
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { onNavigate(GiftHubDestinations.FAVORITES) }) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorites")
                        }

                        IconButton(onClick = { onNavigate(GiftHubDestinations.CART) }) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = "Cart")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Categories Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (isEmployee) {
                        TextButton(onClick = { onNavigate(GiftHubDestinations.MANAGE_CATEGORIES) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Manage Categories",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Categories Loading / Display
                if (categoryViewModel.isLoading && categories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (categories.isEmpty()) {
                    Text(
                        text = "No categories available.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            CategoryItem(
                                category = category,
                                onClick = {
                                    onNavigate(
                                        GiftHubDestinations.productsByCategory(
                                            categoryId = category.categoryId,
                                            categoryName = category.name
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Featured Products Section
                Text(
                    text = "Featured Products",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (productViewModel.isLoading && featuredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (featuredProducts.isEmpty()) {
                    Text(
                        text = "No products available.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(featuredProducts) { product ->
                            FeaturedProductCard(
                                product = product,
                                onClick = {
                                    onNavigate(
                                        GiftHubDestinations.productDetails(product.idProduct)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // All Products Section
                Text(
                    text = "All Products",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (productViewModel.isLoading && productViewModel.productsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (productViewModel.productsList.isEmpty()) {
                    Text(
                        text = "No products available.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(productViewModel.productsList) { product ->
                            ProductListItem(
                                product = product,
                                onClick = {
                                    onNavigate(
                                        GiftHubDestinations.productDetails(product.idProduct)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: CategoryDto,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
    }
}

@Composable
private fun FeaturedProductCard(
    product: ProductDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 140.dp, height = 180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = String.format(Locale.US, "$%.2f", product.price),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProductListItem(
    product: ProductDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Category ID: ${product.categoryId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format(Locale.US, "$%.2f", product.price),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.stock > 0) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}