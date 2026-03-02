package com.evans.jewelryapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.evans.jewelryapp.screen.*
import com.evans.jewelryapp.navigation.AppScreens

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Splash.route
    ) {
        composable(AppScreens.Splash.route) {
            SplashScreen(navController)
        }
        composable(AppScreens.Login.route) {
            LoginScreen(navController)
        }
        composable(AppScreens.Register.route) {
            RegisterScreen(navController)
        }
        composable(AppScreens.Home.route) {
            HomeScreen(navController)
        }
        composable(AppScreens.Admin.route) {
            AdminScreen(navController)
        }
        composable(AppScreens.Cart.route) {
            CartScreen(navController)
        }
        composable("checkout") {
            CheckoutScreen(navController)
        }
        composable("admin_orders") { // ✅ Admin Orders Screen
            AdminOrdersScreen(navController)
        }
    }
}
