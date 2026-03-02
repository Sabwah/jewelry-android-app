package com.evans.jewelryapp.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.evans.jewelryapp.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        delay(2500)

        val nextRoute = when {
            currentUser == null -> AppScreens.Login.route
            currentUser.email == "admin@gmail.com" -> AppScreens.Admin.route
            else -> AppScreens.Home.route
        }

        navController.navigate(nextRoute) {
            popUpTo(AppScreens.Splash.route) { inclusive = true }
        }
    }

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val iconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "icon_rotation"
    )

    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F4C75)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative icons
        JewelryIconsBackground(iconRotation)

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Logo section with animated background
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x33FFD700),
                                Color(0x11FFD700),
                                Color.Transparent
                            ),
                            radius = 150f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Jewelry Vault Logo",
                    tint = Color(0xFFFFD700), // Gold color
                    modifier = Modifier.size(64.dp)
                )
            }

            // Brand name
            Text(
                text = "Jewelry Vault",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = Color.White,
                modifier = Modifier.alpha(textAlpha)
            )

            // Subtitle
            Text(
                text = "Luxury • Elegance • Timeless",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFFBDBDBD),
                textAlign = TextAlign.Center
            )

            // Promotional text
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .alpha(textAlpha),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x22FFD700)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "✨ 25% Off Exclusive Pieces! ✨",
                    fontSize = 16.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Modern progress indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(3.dp),
                    color = Color(0xFFFFD700),
                    trackColor = Color(0x33FFFFFF)
                )

                Text(
                    text = "Loading your treasures...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E),
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
private fun JewelryIconsBackground(rotation: Float) {
    val icons = listOf(
        Icons.Default.Watch to Pair(50.dp, 100.dp),
        Icons.Default.Favorite to Pair(300.dp, 150.dp), // Ring substitute
        Icons.Default.LocalMall to Pair(40.dp, 450.dp), // Handbag
        Icons.Default.Star to Pair(320.dp, 500.dp), // Jewelry accent
        Icons.Default.Circle to Pair(280.dp, 80.dp), // Ring substitute
        Icons.Default.Hexagon to Pair(80.dp, 520.dp) // Gem shape
    )

    icons.forEach { (icon, position) ->
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0x11FFFFFF),
            modifier = Modifier
                .size(32.dp)
                .offset(x = position.first, y = position.second)
                .rotate(rotation * 0.5f)
        )
    }
}