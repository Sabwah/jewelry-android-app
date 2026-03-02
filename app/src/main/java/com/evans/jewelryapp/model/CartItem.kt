package com.evans.jewelryapp.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CartItem(
    val itemId: String = "",
    val name: String = "",            // ✅ Added field for product name
    val imageUrl: String = "",
    val price: Double = 0.0,
    var quantity: Int = 1
)

fun CartItem.toMap(): Map<String, Any> = mapOf(
    "itemId" to itemId,
    "name" to name,                   // ✅ Included in Firestore map
    "imageUrl" to imageUrl,
    "price" to price,
    "quantity" to quantity
)
