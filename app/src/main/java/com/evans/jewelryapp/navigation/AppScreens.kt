package com.evans.jewelryapp.navigation

sealed class AppScreens(val route: String) {
    object Splash : AppScreens("splash")
    object Login : AppScreens("login")
    object Register : AppScreens("register")
    object Home : AppScreens("home")
    object Admin : AppScreens("admin")
    object Cart : AppScreens("cart")
    object Checkout : AppScreens("checkout")
    object AdminOrders : AppScreens("admin_orders") // ✅ Added
}
