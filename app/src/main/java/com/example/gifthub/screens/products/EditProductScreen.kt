package com.example.gifthub.screens.products

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.screens.notifications.NotificationHelper
import com.example.gifthub.viewmodel.CategoryViewModel
import com.example.gifthub.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    onBack: () -> Unit,
    viewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }

    var baselinePrice by remember { mutableDoubleStateOf(Double.NaN) }
    var baselineName by remember { mutableStateOf("") }

    val product = viewModel.selectedProduct
    val categories = categoryViewModel.categoriesList

    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()
    }

    LaunchedEffect(productId) {
        viewModel.loadProductById(productId)
    }

    LaunchedEffect(product?.idProduct) {
        product?.let {
            name = it.name
            description = it.description
            priceStr = it.price.toString()
            stockStr = it.stock.toString()
            imageUrl = it.imageUrl
            selectedCategoryId = it.categoryId
            baselinePrice = it.price
            baselineName = it.name
        }
    }

    LaunchedEffect(categories, selectedCategoryId) {
        val currentCategory = categories.firstOrNull { it.categoryId == selectedCategoryId }
        selectedCategoryName = currentCategory?.name.orEmpty()
    }

    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (viewModel.isLoading && product == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF6B35))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 32.dp)) {
                        IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column {
                            Text(
                                text = "Edit Product",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Update product details",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Product Name") },
                        leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Outlined.Label, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                        leadingIcon = { Icon(imageVector = Icons.Outlined.Description, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Image URL") },
                        leadingIcon = { Icon(imageVector = Icons.Outlined.Image, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Price ($)") },
                            leadingIcon = { Icon(imageVector = Icons.Outlined.AttachMoney, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Stock (qty)") },
                            leadingIcon = { Icon(imageVector = Icons.Outlined.Inventory2, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            label = { Text("Category") },
                            leadingIcon = { Icon(imageVector = Icons.Outlined.Category, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            shape = RoundedCornerShape(16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.categoryId
                                        selectedCategoryName = category.name
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    viewModel.errorMessage?.let { message ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val oldPrice = if (baselinePrice.isNaN()) null else baselinePrice
                            val newPrice = priceStr.toDoubleOrNull()
                            val originalProductName = baselineName.ifBlank { name }

                            viewModel.updateProduct(
                                productId = productId,
                                name = name,
                                description = description,
                                priceStr = priceStr,
                                stockStr = stockStr,
                                categoryIdStr = selectedCategoryId,
                                imageUrl = imageUrl
                            ) {
                                if (oldPrice != null && newPrice != null && oldPrice != newPrice) {
                                    val direction = if (newPrice < oldPrice) "dropped" else "updated"
                                    val emoji = if (newPrice < oldPrice) "🔥" else "📦"
                                    NotificationHelper.show(
                                        context = context,
                                        channelId = NotificationHelper.CHANNEL_ID_PRODUCTS,
                                        title = "$emoji Price $direction!",
                                        message = "\"$originalProductName\" is now $${"%.2f".format(newPrice)} (was $${"%.2f".format(oldPrice)})",
                                        targetRoute = GiftHubDestinations.PRODUCTS,
                                        notificationId = ("price_change_$productId").hashCode()
                                    )
                                }
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !viewModel.isLoading && name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Updating...", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        } else {
                            Text(
                                "Update Product",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}