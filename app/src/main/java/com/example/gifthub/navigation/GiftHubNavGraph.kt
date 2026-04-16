package com.example.gifthub.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gifthub.screens.address.ManageAddressScreen
import com.example.gifthub.screens.auth.LoginScreen
import com.example.gifthub.screens.auth.RegisterScreen
import com.example.gifthub.screens.cart.CartScreen
import com.example.gifthub.screens.categories.ManageCategoriesScreen
import com.example.gifthub.screens.checkout.CheckoutScreen
import com.example.gifthub.screens.favorites.FavoritesScreen
import com.example.gifthub.screens.home.HomeScreen
import com.example.gifthub.screens.notifications.NotificationsScreen
import com.example.gifthub.screens.orders.OrderHistoryScreen
import com.example.gifthub.screens.payments.SavedPaymentsScreen
import com.example.gifthub.screens.products.AddProductScreen
import com.example.gifthub.screens.products.EditProductScreen
import com.example.gifthub.screens.products.ProductDetailsScreen
import com.example.gifthub.screens.products.ProductsScreen
import com.example.gifthub.screens.profile.ProfileScreen
import com.example.gifthub.ui.redeem.RedeemGiftRoute
import com.example.gifthub.viewmodel.AuthViewModel
import com.example.gifthub.viewmodel.CartViewModel
import com.example.gifthub.viewmodel.NotificationViewModel
import com.example.gifthub.viewmodel.OrderViewModel
import com.example.gifthub.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun GiftHubNavGraph(notificationRouteFlow: SharedFlow<String>) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val normalizedCurrentRoute = currentBackStackEntry?.destination.normalizedRoute()

    val startDestination = if (authViewModel.isAuthenticated) GiftHubDestinations.HOME else GiftHubDestinations.LOGIN

    val scope = rememberCoroutineScope()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        notificationRouteFlow.collect { route ->
            if (authViewModel.isAuthenticated) {
                navigateToTopLevel(navController, route)
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(GiftHubDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    cartViewModel.loadCart()
                    navController.navigate(GiftHubDestinations.HOME) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoToRegister = {
                    navController.navigate(GiftHubDestinations.REGISTER) { launchSingleTop = true }
                },
                viewModel = authViewModel
            )
        }

        composable(GiftHubDestinations.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    cartViewModel.loadCart()
                    navController.navigate(GiftHubDestinations.HOME) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoToLogin = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        composable(GiftHubDestinations.HOME) {
            HomeScreen(
                currentRoute = normalizedCurrentRoute ?: GiftHubDestinations.HOME,
                onNavigate = { destination -> handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel) },
                authViewModel = authViewModel
            )
        }

        composable(GiftHubDestinations.PRODUCTS) {
            ProductsScreen(
                currentRoute = normalizedCurrentRoute ?: GiftHubDestinations.PRODUCTS,
                onNavigate = { destination -> handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel) },
                authViewModel = authViewModel
            )
        }

        composable(
            route = GiftHubDestinations.PRODUCTS_BY_CATEGORY,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
            val encodedCategoryName = backStackEntry.arguments?.getString("categoryName").orEmpty()
            val categoryName = Uri.decode(encodedCategoryName)

            ProductsScreen(
                currentRoute = GiftHubDestinations.PRODUCTS,
                onNavigate = { destination -> handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel) },
                authViewModel = authViewModel,
                selectedCategoryId = categoryId,
                selectedCategoryName = categoryName
            )
        }

        composable(GiftHubDestinations.ADD_PRODUCT) {
            AddProductScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = GiftHubDestinations.PRODUCT_DETAILS,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
            ProductDetailsScreen(
                productId = productId,
                onNavigate = { destination ->
                    if (destination == GiftHubDestinations.PRODUCTS) navController.popBackStack()
                    else handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel)
                },
                productViewModel = productViewModel,
                cartViewModel = cartViewModel
            )
        }

        composable(GiftHubDestinations.REDEEM_GIFT) {
            RedeemGiftRoute(onBack = { navController.popBackStack() })
        }

        composable(
            route = GiftHubDestinations.EDIT_PRODUCT,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
            EditProductScreen(productId = productId, onBack = { navController.popBackStack() }, viewModel = productViewModel)
        }

        composable(GiftHubDestinations.MANAGE_CATEGORIES) {
            ManageCategoriesScreen(onBack = { navController.popBackStack() })
        }

        composable(GiftHubDestinations.CART) {
            CartScreen(
                currentRoute = normalizedCurrentRoute ?: GiftHubDestinations.CART,
                onNavigate = { destination -> handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel) },
                viewModel = cartViewModel
            )
        }

        composable(GiftHubDestinations.CHECKOUT) {
            CheckoutScreen(
                onNavigate = { destination -> handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel) },
                onBack = {
                    val popped = navController.popBackStack()
                    if (!popped) navController.navigate(GiftHubDestinations.CART) { launchSingleTop = true }
                },
                cartViewModel = cartViewModel,
                orderViewModel = orderViewModel
            )
        }

        composable(GiftHubDestinations.ORDER_HISTORY) {
            OrderHistoryScreen(onBack = { navController.popBackStack() }, orderViewModel = orderViewModel)
        }

        composable(
            route = GiftHubDestinations.ORDER_DETAILS,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) {
            navController.popBackStack()
        }

        composable(GiftHubDestinations.FAVORITES) {
            FavoritesScreen(
                currentRoute = normalizedCurrentRoute ?: GiftHubDestinations.FAVORITES,
                onNavigate = { destination -> handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel) },
                authViewModel = authViewModel
            )
        }

        composable(GiftHubDestinations.PROFILE) {
            ProfileScreen(
                currentRoute = normalizedCurrentRoute ?: GiftHubDestinations.PROFILE,
                onNavigate = { destination -> handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel) }
            )
        }

        composable(GiftHubDestinations.NOTIFICATIONS) {
            NotificationsScreen(
                currentRoute = normalizedCurrentRoute ?: GiftHubDestinations.NOTIFICATIONS,
                onNavigate = { destination -> handleNavigation(navController, normalizedCurrentRoute, destination, authViewModel) },
                viewModel = notificationViewModel
            )
        }

        composable(GiftHubDestinations.MANAGE_ADDRESS) {
            ManageAddressScreen(onBack = { navController.popBackStack() })
        }

        composable(GiftHubDestinations.SAVED_PAYMENTS) {
            SavedPaymentsScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun handleNavigation(
    navController: NavHostController,
    currentRoute: String?,
    destination: String,
    authViewModel: AuthViewModel
) {
    if (destination.isBlank()) return
    if (currentRoute == destination) return
    if (destination.endsWith("/") || destination.trimEnd().endsWith("/")) return

    when (destination) {
        GiftHubDestinations.LOGIN -> {
            authViewModel.logout()
            navController.navigate(GiftHubDestinations.LOGIN) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
        GiftHubDestinations.HOME,
        GiftHubDestinations.PRODUCTS,
        GiftHubDestinations.CART,
        GiftHubDestinations.FAVORITES,
        GiftHubDestinations.PROFILE,
        GiftHubDestinations.NOTIFICATIONS,
        GiftHubDestinations.ORDER_HISTORY,
        GiftHubDestinations.MANAGE_ADDRESS,
        GiftHubDestinations.SAVED_PAYMENTS -> navigateToTopLevel(navController, destination)
        else -> navController.navigate(destination) { launchSingleTop = true }
    }
}

private fun navigateToTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.normalizedRoute(): String? {
    val route = this?.route ?: return null
    return when {
        route == GiftHubDestinations.PRODUCTS_BY_CATEGORY || route.startsWith("products_by_category/") -> GiftHubDestinations.PRODUCTS
        route == GiftHubDestinations.PRODUCT_DETAILS || route.startsWith("product_details/") -> GiftHubDestinations.PRODUCTS
        route == GiftHubDestinations.EDIT_PRODUCT || route.startsWith("edit_product/") -> GiftHubDestinations.PRODUCTS
        route == GiftHubDestinations.ORDER_DETAILS || route.startsWith("order_details/") -> GiftHubDestinations.ORDER_HISTORY
        else -> route
    }
}