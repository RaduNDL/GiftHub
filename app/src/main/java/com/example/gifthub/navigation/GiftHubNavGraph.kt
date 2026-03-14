package com.example.gifthub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gifthub.screens.cart.CartScreen
import com.example.gifthub.screens.favorites.FavoritesScreen
import com.example.gifthub.screens.home.HomeScreen
import com.example.gifthub.screens.products.ProductsScreen
import com.example.gifthub.screens.profile.ProfileScreen

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
                onNavigate = { route -> navigateToTopLevel(navController, route) }
            )
        }

        composable(GiftHubDestinations.PRODUCTS) {
            ProductsScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateToTopLevel(navController, route) }
            )
        }

        composable(GiftHubDestinations.CART) {
            CartScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateToTopLevel(navController, route) }
            )
        }

        composable(GiftHubDestinations.FAVORITES) {
            FavoritesScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateToTopLevel(navController, route) }
            )
        }

        composable(GiftHubDestinations.PROFILE) {
            ProfileScreen(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateToTopLevel(navController, route) }
            )
        }
    }
}

private fun navigateToTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}