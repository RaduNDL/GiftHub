package com.example.gifthub.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gifthub.navigation.GiftHubDestinations
import com.example.gifthub.ui.components.GiftHubBottomBar

@Composable
fun ProductsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val searchText = remember { mutableStateOf("") }
    val products = listOf("Product 1", "Product 2", "Product 3", "Product 4")

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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { onNavigate(GiftHubDestinations.CART) }) {
                        Icon(Icons.Default.ShoppingCartCheckout, contentDescription = "Cart")
                    }
                    IconButton(onClick = { onNavigate(GiftHubDestinations.FAVORITES) }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorites")
                    }
                    IconButton(onClick = {onNavigate(GiftHubDestinations.NOTIFICATIONS) }) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { onNavigate(GiftHubDestinations.PROFILE) }) {
                        Icon(Icons.Default.PersonOutline, contentDescription = "Profile")
                    }
                }

                OutlinedTextField(
                    value = searchText.value,
                    onValueChange = { searchText.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search") },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors()
                )

                Text(
                    text = "Filtered",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 18.dp, bottom = 18.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(products) { product ->
                        ProductGridCard(title = product)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductGridCard(title: String) {
    Column {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 10.dp, start = 4.dp)
        )
    }
}