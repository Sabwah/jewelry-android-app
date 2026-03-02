package com.evans.jewelryapp.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.evans.jewelryapp.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ItemCard(item: Item, onItemAddedToCart: () -> Unit) {
    var quantity by remember { mutableStateOf(1) }
    var message by remember { mutableStateOf<String?>(null) }
    var isAddingToCart by remember { mutableStateOf(false) }

    // Clear message after 3 seconds
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(3000)
            message = null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp) // ✅ Balanced height
            .padding(6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image
            if (item.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(130.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                // Placeholder when no image
                Box(
                    modifier = Modifier
                        .height(130.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Title & Price
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Ksh ${item.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Stock
            if (item.stock == 0) {
                Text(
                    text = "Out of Stock",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            } else {
                Text(
                    text = "Stock: ${item.stock}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quantity + Add button
            if (item.stock > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = "$quantity",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(
                        onClick = { if (quantity < item.stock) quantity++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Button(
                    onClick = {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            message = "User not logged in"
                            return@Button
                        }

                        isAddingToCart = true
                        val cartItem = hashMapOf(
                            "itemId" to item.id,
                            "name" to item.title,
                            "imageUrl" to item.imageUrl,
                            "price" to item.price,
                            "quantity" to quantity
                        )

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.uid)
                            .collection("cart")
                            .document(item.id) // ✅ One item, one cart entry
                            .set(cartItem)
                            .addOnSuccessListener {
                                isAddingToCart = false
                                message = "✅ Added to cart"
                                onItemAddedToCart()
                            }
                            .addOnFailureListener { e ->
                                isAddingToCart = false
                                Log.e("AddToCart", "Error: ${e.message}", e)
                                message = "❌ Failed: ${e.localizedMessage}"
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp), // ✅ Enough height for button text
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isAddingToCart
                ) {
                    if (isAddingToCart) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Add to Cart")
                    }
                }
            }

            // Message
            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (it.startsWith("✅")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}