package com.example.gifthub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gifthub.screens.address.ManageAddressScreen
import com.example.gifthub.screens.cart.CartScreen
import com.example.gifthub.screens.favorites.FavoritesScreen
import com.example.gifthub.screens.home.HomeScreen
import com.example.gifthub.screens.orders.OrderHistoryScreen
import com.example.gifthub.screens.payments.SavedPaymentsScreen
import com.example.gifthub.screens.products.ProductsScreen
import com.example.gifthub.screens.profile.ProfileScreen
import com.example.gifthub.screens.wishlist.MyWishlistScreen

@Composable
fun GiftHubNavGraph() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: GiftHubDestinations.HOME

    NavHost(
        navController = navController,
        startDestination = GiftHubDestinations.HOME
    ) {
        composable(GiftHubDestinations.HOME) {
            HomeScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateSmart(navController, route) }
            )
        }

        composable(GiftHubDestinations.PRODUCTS) {
            ProductsScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateSmart(navController, route) }
            )
        }

        composable(GiftHubDestinations.CART) {
            CartScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateSmart(navController, route) }
            )
        }

        composable(GiftHubDestinations.FAVORITES) {
            FavoritesScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateSmart(navController, route) }
            )
        }

        composable(GiftHubDestinations.PROFILE) {
            ProfileScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateSmart(navController, route) }
            )
        }


        composable(GiftHubDestinations.ORDER_HISTORY) {
            OrderHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(GiftHubDestinations.MY_WISHLIST) {
            MyWishlistScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(GiftHubDestinations.MANAGE_ADDRESS) {
            ManageAddressScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(GiftHubDestinations.SAVED_PAYMENTS) {
            SavedPaymentsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun navigateSmart(navController: NavHostController, route: String) {
    if (isTopLevelRoute(route)) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    } else {
        navController.navigate(route)
    }
}

private fun isTopLevelRoute(route: String): Boolean {
    return route == GiftHubDestinations.HOME ||
            route == GiftHubDestinations.PRODUCTS ||
            route == GiftHubDestinations.CART ||
            route == GiftHubDestinations.FAVORITES ||
            route == GiftHubDestinations.PROFILE ||
            route == GiftHubDestinations.NOTIFICATIONS
}