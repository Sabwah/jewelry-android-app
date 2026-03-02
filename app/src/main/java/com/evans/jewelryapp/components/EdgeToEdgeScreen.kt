package com.evans.jewelryapp.components

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun EdgeToEdgeScreen(
    lightStatusBar: Boolean = true, // true = dark icons on light status bar
    lightNavigationBar: Boolean = true, // true = dark icons on light navigation bar
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    SideEffect {
        val window = view.findActivity()?.window
        if (window != null) {
            // Don't call setDecorFitsSystemWindows here - it's already set globally in MainActivity
            // Only control the appearance of system bar icons
            val insetsController = WindowInsetsControllerCompat(window, view)
            insetsController.isAppearanceLightStatusBars = lightStatusBar
            insetsController.isAppearanceLightNavigationBars = lightNavigationBar
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}

// Extension function to find the parent Activity from a View
fun View.findActivity(): Activity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}