package com.example.gifthub.navigation

object GiftHubDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PRODUCTS = "products"
    const val PRODUCTS_BY_CATEGORY = "products_by_category/{categoryId}/{categoryName}"
    const val ADD_PRODUCT = "add_product"
    const val PRODUCT_DETAILS = "product_details/{productId}"
    const val EDIT_PRODUCT = "edit_product/{productId}"
    const val MANAGE_CATEGORIES = "manage_categories"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDER_HISTORY = "order_history"
    const val ORDER_DETAILS = "order_details/{orderId}"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
    const val MANAGE_ADDRESS = "manage_address"
    const val SAVED_PAYMENTS = "saved_payments"
    const val EXTRA_PRODUCT_DETAILS = "extra_product_details/{productId}"

    fun extraProductDetails(productId: String) = "extra_product_details/$productId"

    fun productsByCategory(categoryId: String, categoryName: String): String {
        return "products_by_category/$categoryId/$categoryName"
    }

    fun productDetails(productId: String): String {
        return "product_details/$productId"
    }

    fun editProduct(productId: String): String {
        return "edit_product/$productId"
    }

    fun orderDetails(orderId: String): String {
        return "order_details/$orderId"
    }


}