package com.evans.jewelryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.evans.jewelryapp.ui.theme.JewelryAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Enable edge-to-edge fullscreen layout GLOBALLY for entire app
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ✅ Make status bar transparent to allow gradient to extend behind it
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            JewelryAppTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
