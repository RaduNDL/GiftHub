package com.example.gifthub.screens.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
<<<<<<< HEAD
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
=======
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
>>>>>>> 674696e (update navigation + all screens working)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MyWishlistScreen(
    onBack: () -> Unit
) {
    val wishlistItems = remember {
        listOf(
<<<<<<< HEAD
            WishlistUi("Luxury Watch Box", "$59.99"),
            WishlistUi("Custom Teddy Bear", "$34.50"),
            WishlistUi("Elegant Flower Set", "$27.90"),
            WishlistUi("Gift Surprise Package", "$74.00")
=======
            WishlistUi("Custom Photo Frame", "$24.99"),
            WishlistUi("Romantic Gift Basket", "$59.90"),
            WishlistUi("Luxury Chocolate Box", "$19.50"),
            WishlistUi("Birthday Surprise Set", "$42.00")
>>>>>>> 674696e (update navigation + all screens working)
        )
    }

    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "My Wishlist",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

<<<<<<< HEAD
                Spacer(modifier = Modifier.size(20.dp))
=======
                Spacer(modifier = Modifier.size(18.dp))
>>>>>>> 674696e (update navigation + all screens working)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(wishlistItems) { item ->
                        WishlistCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistCard(item: WishlistUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
<<<<<<< HEAD
                    .size(84.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(22.dp)
=======
                    .size(86.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
>>>>>>> 674696e (update navigation + all screens working)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = item.title,
<<<<<<< HEAD
                    modifier = Modifier.size(34.dp),
=======
>>>>>>> 674696e (update navigation + all screens working)
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.size(6.dp))

                Text(
                    text = item.price,
<<<<<<< HEAD
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
=======
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
>>>>>>> 674696e (update navigation + all screens working)
                )
            }

            Icon(
                imageVector = Icons.Default.Favorite,
<<<<<<< HEAD
                contentDescription = "Favorite",
=======
                contentDescription = "Wishlist",
>>>>>>> 674696e (update navigation + all screens working)
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private data class WishlistUi(
    val title: String,
    val price: String
)