package com.evans.jewelryapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.evans.jewelryapp.model.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(userId) {
        userId?.let {
            val result = FirebaseFirestore.getInstance()
                .collection("users")
                .document(it)
                .collection("cart")
                .get()
                .await()

            val items = result.documents.mapNotNull { doc ->
                doc.toObject(CartItem::class.java)?.copy(itemId = doc.id)
            }
            cartItems.clear()
            cartItems.addAll(items)
            isLoading = false
        }
    }

    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("cart") { inclusive = true }
                    }
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Beautiful gradient background with blur effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2),
                            Color(0xFFf093fb),
                            Color(0xFFf5576c),
                            Color(0xFF4facfe),
                            Color(0xFF00f2fe)
                        )
                    )
                )
                .blur(50.dp)
        )

        // Semi-transparent overlay for better content visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("🛒 Your Cart", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.3f),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    if (cartItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Your cart is empty.",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .weight(1f)
                            ) {
                                items(cartItems) { item ->
                                    CartItemCard(item, userId ?: "", onUpdate = {
                                        cartItems.clear()
                                        cartItems.addAll(it)
                                    })
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Total: Ksh $totalPrice",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.End)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        userId?.let { uid ->
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(uid)
                                                .collection("cart")
                                                .get()
                                                .addOnSuccessListener { snapshot ->
                                                    for (doc in snapshot.documents) {
                                                        doc.reference.delete()
                                                    }
                                                    cartItems.clear()
                                                }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Black.copy(alpha = 0.7f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Clear Cart", color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { navController.navigate("checkout") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Black.copy(alpha = 0.7f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Proceed to Checkout", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, userId: String, onUpdate: (List<CartItem>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val cartRef = db.collection("users").document(userId).collection("cart")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl),
                    contentDescription = item.itemId,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 12.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                Text(text = "Price: Ksh ${item.price}", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                Text(text = "Quantity: ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                Text(text = "Subtotal: Ksh ${item.price * item.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (item.quantity > 1) {
                            cartRef.document(item.itemId).update("quantity", item.quantity - 1)
                                .addOnSuccessListener {
                                    onUpdate(cartRef.get().result?.documents?.mapNotNull { it.toObject(CartItem::class.java)?.copy(itemId = it.id) } ?: emptyList())
                                }
                        }
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.Black)
                    }
                    Text("${item.quantity}", color = Color.Black)
                    IconButton(onClick = {
                        cartRef.document(item.itemId).update("quantity", item.quantity + 1)
                            .addOnSuccessListener {
                                onUpdate(cartRef.get().result?.documents?.mapNotNull { it.toObject(CartItem::class.java)?.copy(itemId = it.id) } ?: emptyList())
                            }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.Black)
                    }
                }
            }

            IconButton(onClick = {
                cartRef.document(item.itemId).delete().addOnSuccessListener {
                    cartRef.get().addOnSuccessListener { snapshot ->
                        val updatedItems = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(CartItem::class.java)?.copy(itemId = doc.id)
                        }
                        onUpdate(updatedItems)
                    }
                }
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Remove item", tint = Color.Red)
            }
        }
    }
}