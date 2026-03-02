package com.evans.jewelryapp.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Item(
    val id: String = "",              // ✅ Needed for itemList key
    val title: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val category: String = "",
    val imageUrl: String = ""
)
