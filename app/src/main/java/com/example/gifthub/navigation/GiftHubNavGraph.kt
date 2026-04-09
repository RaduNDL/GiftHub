package com.example.gifthub.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.gifthub.notifications.PushNotificationManager
import com.example.gifthub.notifications.PushTokenManager
import com.example.gifthub.repositories.NotificationRepository
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
import com.example.gifthub.viewmodel.AuthViewModel
import com.example.gifthub.viewmodel.CartViewModel
import com.example.gifthub.viewmodel.ProductViewModel

@Composable
fun GiftHubNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val normalizedCurrentRoute = currentBackStackEntry?.destination.normalizedRoute()

    val startDestination = if (authViewModel.isAuthenticated) {
        GiftHubDestinations.HOME
    } else {
        GiftHubDestinations.LOGIN
    }

    HandlePushNavigation(
        navController = navController,
        isAuthenticated = authViewModel.isAuthenticated
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ========== AUTH SCREENS ==========
        composable(GiftHubDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    PushTokenManager.syncCurrentToken()
                    cartViewModel.loadCart()

                    navController.navigate(GiftHubDestinations.HOME) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onGoToRegister = {
                    navController.navigate(GiftHubDestinations.REGISTER) {
                        launchSingleTop = true
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(GiftHubDestinations.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    PushTokenManager.syncCurrentToken()
                    cartViewModel.loadCart()

                    navController.navigate(GiftHubDestinations.HOME) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onGoToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        // ========== HOME SCREEN ==========
        composable(GiftHubDestinations.HOME) {
            HomeScreen(
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                },
                authViewModel = authViewModel
            )
        }

        // ========== PRODUCTS SCREENS ==========
        composable(GiftHubDestinations.PRODUCTS) {
            ProductsScreen(
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                },
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
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                },
                authViewModel = authViewModel,
                selectedCategoryId = categoryId,
                selectedCategoryName = categoryName
            )
        }

        composable(GiftHubDestinations.ADD_PRODUCT) {
            AddProductScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = GiftHubDestinations.PRODUCT_DETAILS,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
            val productViewModel: ProductViewModel = viewModel()

            ProductDetailsScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                viewModel = productViewModel,
                cartViewModel = cartViewModel,
                onGoToCart = {
                    cartViewModel.loadCart()
                    navigateToTopLevel(navController, GiftHubDestinations.CART)
                }
            )
        }

        composable(
            route = GiftHubDestinations.EDIT_PRODUCT,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
            val productViewModel: ProductViewModel = viewModel()

            EditProductScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                viewModel = productViewModel
            )
        }

        // ========== CATEGORIES ==========
        composable(GiftHubDestinations.MANAGE_CATEGORIES) {
            ManageCategoriesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ========== CART ==========
        composable(GiftHubDestinations.CART) {
            CartScreen(
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                },
                viewModel = cartViewModel
            )
        }

        // ========== CHECKOUT ==========
        composable(GiftHubDestinations.CHECKOUT) {
            CheckoutScreen(
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                },
                cartViewModel = cartViewModel
            )
        }

        // ========== ORDER HISTORY ==========
        composable(GiftHubDestinations.ORDER_HISTORY) {
            OrderHistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                }
            )
        }

        // ========== FAVORITES ==========
        composable(GiftHubDestinations.FAVORITES) {
            FavoritesScreen(
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                },
                authViewModel = authViewModel
            )
        }

        // ========== PROFILE ==========
        composable(GiftHubDestinations.PROFILE) {
            ProfileScreen(
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                }
            )
        }

        // ========== NOTIFICATIONS ==========
        composable(GiftHubDestinations.NOTIFICATIONS) {
            NotificationsScreen(
                onNavigate = { destination ->
                    handleNavigation(
                        navController = navController,
                        currentRoute = normalizedCurrentRoute,
                        destination = destination,
                        authViewModel = authViewModel
                    )
                },
                currentRoute = TODO(),
                viewModel = TODO()
            )
        }

        // ========== ADDRESS MANAGEMENT ==========
        composable(GiftHubDestinations.MANAGE_ADDRESS) {
            ManageAddressScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ========== PAYMENTS MANAGEMENT ==========
        composable(GiftHubDestinations.SAVED_PAYMENTS) {
            SavedPaymentsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Handles navigation triggered by push notifications
 */
@Composable
private fun HandlePushNavigation(
    navController: NavHostController,
    isAuthenticated: Boolean
) {
    val pendingNavigation = PushNotificationManager.pendingNavigation

    LaunchedEffect(pendingNavigation, isAuthenticated) {
        if (!isAuthenticated) return@LaunchedEffect

        val navigation = PushNotificationManager.consumePendingNavigation()
            ?: return@LaunchedEffect

        val targetRoute = navigation.route.ifBlank { GiftHubDestinations.ORDER_HISTORY }

        when (targetRoute) {
            GiftHubDestinations.HOME,
            GiftHubDestinations.PRODUCTS,
            GiftHubDestinations.CART,
            GiftHubDestinations.FAVORITES,
            GiftHubDestinations.PROFILE,
            GiftHubDestinations.NOTIFICATIONS,
            GiftHubDestinations.ORDER_HISTORY -> {
                navigateToTopLevel(navController, targetRoute)
            }

            else -> {
                navController.navigate(targetRoute) {
                    launchSingleTop = true
                }
            }
        }

        // Mark notification as read and delete it
        navigation.notificationId
            ?.takeIf { it.isNotBlank() }
            ?.let { notificationId ->
                NotificationRepository().deleteNotification(
                    notificationId = notificationId,
                    onSuccess = {},
                    onError = {}
                )
            }
    }
}

/**
 * Handles regular navigation between screens
 */
private fun handleNavigation(
    navController: NavHostController,
    currentRoute: String?,
    destination: String,
    authViewModel: AuthViewModel
) {
    // Avoid re-navigation to the same route
    if (currentRoute == destination) return

    when (destination) {
        GiftHubDestinations.LOGIN -> {
            authViewModel.logout()
            navController.navigate(GiftHubDestinations.LOGIN) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }

        GiftHubDestinations.HOME,
        GiftHubDestinations.PRODUCTS,
        GiftHubDestinations.CART,
        GiftHubDestinations.FAVORITES,
        GiftHubDestinations.PROFILE,
        GiftHubDestinations.NOTIFICATIONS,
        GiftHubDestinations.ORDER_HISTORY -> {
            navigateToTopLevel(navController, destination)
        }

        else -> {
            navController.navigate(destination) {
                launchSingleTop = true
            }
        }
    }
}

/**
 * Navigates to a top-level destination while saving/restoring state
 */
private fun navigateToTopLevel(
    navController: NavHostController,
    route: String
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Normalizes the current route to handle nested navigation routes
 * For example:
 * - "product_details/xyz" → "products"
 * - "products_by_category/x/y" → "products"
 * - "edit_product/abc" → "products"
 */
private fun NavDestination?.normalizedRoute(): String? {
    val route = this?.route ?: return null

    return when {
        route == GiftHubDestinations.PRODUCTS_BY_CATEGORY ||
                route.startsWith("products_by_category/") -> GiftHubDestinations.PRODUCTS

        route == GiftHubDestinations.PRODUCT_DETAILS ||
                route.startsWith("product_details/") -> GiftHubDestinations.PRODUCTS

        route == GiftHubDestinations.EDIT_PRODUCT ||
                route.startsWith("edit_product/") -> GiftHubDestinations.PRODUCTS

        else -> route
    }
}