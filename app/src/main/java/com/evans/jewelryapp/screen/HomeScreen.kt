package com.evans.jewelryapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.evans.jewelryapp.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.shape.RoundedCornerShape


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf("") }
    val itemList = remember { mutableStateListOf<Item>() }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val cartItemCount = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadCartItemCount(cartItemCount)
    }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory.isNotBlank()) {
            coroutineScope.launch {
                loadItemsByCategory(selectedCategory, itemList) { loading, errorMessage ->
                    isLoading = loading
                    error = errorMessage
                }
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Main container with blue gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3), // Bright blue
                        Color(0xFF1976D2), // Medium blue
                        Color(0xFF0D47A1), // Deep blue
                        Color(0xFF0A2E5C)  // Dark blue
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Jewelry${if (selectedCategory.isNotBlank()) ": $selectedCategory" else ""}",
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        if (selectedCategory.isNotBlank()) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    loadItemsByCategory(selectedCategory, itemList) { loading, errorMessage ->
                                        isLoading = loading
                                        error = errorMessage
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Red)
                            }
                        }

                        CartBadgeButton(
                            count = cartItemCount.value,
                            onClick = { navController.navigate("cart") }
                        )

                        IconButton(onClick = {
                            showLogoutDialog = true
                        }) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            // Content area with normal background
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            ) {
                CategorySelector(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        error = null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                error?.let { errorMessage ->
                    ErrorCard(errorMessage = errorMessage, onRetry = {
                        coroutineScope.launch {
                            loadItemsByCategory(selectedCategory, itemList) { loading, errorMsg ->
                                isLoading = loading
                                error = errorMsg
                            }
                        }
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when {
                    selectedCategory.isBlank() -> {
                        WelcomeCard()
                    }

                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text("Loading $selectedCategory items...")
                            }
                        }
                    }

                    itemList.isEmpty() -> {
                        EmptyStateCard(category = selectedCategory, onRetry = {
                            coroutineScope.launch {
                                loadItemsByCategory(selectedCategory, itemList) { loading, errorMsg ->
                                    isLoading = loading
                                    error = errorMsg
                                }
                            }
                        })
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(itemList, key = { it.id }) { item ->
                                ItemCard(
                                    item = item,
                                    onItemAddedToCart = {
                                        cartItemCount.value += 1
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartBadgeButton(count: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(onClick = onClick) {
            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.Red)
        }

        if (count > 0) {
            Badge(
                containerColor = Color.Red,
                modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
            ) {
                Text(if (count > 99) "99+" else "$count", color = Color.White)
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to Jewelry Vault", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Select a category above to browse our collection", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EmptyStateCard(category: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No items found", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("We don't have any $category items at the moment.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Refresh")
            }
        }
    }
}

@Composable
fun ErrorCard(errorMessage: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private suspend fun loadCartItemCount(cartItemCount: MutableState<Int>) {
    try {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val result = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .collection("cart")
                .get()
                .await()
            cartItemCount.value = result.size()
        }
    } catch (e: Exception) {
        cartItemCount.value = 0
    }
}

private suspend fun loadItemsByCategory(
    category: String,
    itemList: MutableList<Item>,
    onStateChange: (isLoading: Boolean, error: String?) -> Unit
) {
    try {
        onStateChange(true, null)
        val result = FirebaseFirestore.getInstance()
            .collection("items")
            .whereEqualTo("category", category)
            .get()
            .await()

        itemList.clear()
        result.forEach { document ->
            val item = document.toObject(Item::class.java).copy(id = document.id)
            itemList.add(item)
        }
        onStateChange(false, null)
    } catch (e: Exception) {
        onStateChange(false, "Failed to load items: ${e.message}")
    }
}