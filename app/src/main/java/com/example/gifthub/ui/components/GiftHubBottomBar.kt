package com.example.gifthub.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.gifthub.navigation.GiftHubDestinations

@Composable
fun GiftHubBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        val items = listOf(
            BottomNavItem("Home", GiftHubDestinations.HOME, Icons.Default.Home),
            BottomNavItem("Products", GiftHubDestinations.PRODUCTS, Icons.Default.ShoppingBag),
            BottomNavItem("Cart", GiftHubDestinations.CART, Icons.Default.ShoppingCart),
            BottomNavItem("Favorites", GiftHubDestinations.FAVORITES, Icons.Default.FavoriteBorder),
            BottomNavItem("Profile", GiftHubDestinations.PROFILE, Icons.Default.Person)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)