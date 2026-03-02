package com.evans.jewelryapp.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.evans.jewelryapp.model.CartItem
import com.evans.jewelryapp.model.toMap
import com.google.accompanist.permissions.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val hasLocationPermission = permissionState.status == PermissionStatus.Granted

    val nameState = remember { mutableStateOf(TextFieldValue()) }
    val phoneState = remember { mutableStateOf(TextFieldValue()) }
    val addressState = remember { mutableStateOf(TextFieldValue()) }

    val coroutineScope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isLocationLoading by remember { mutableStateOf(false) }
    var isMapExpanded by remember { mutableStateOf(false) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
    }

    // ✅ Request location when permission is granted
    @SuppressLint("MissingPermission")
    fun requestLocation() {
        isLocationLoading = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                message = "📍 Location found successfully"
            } else {
                message = "❌ Unable to get location. Please try again."
            }
            isLocationLoading = false
        }.addOnFailureListener {
            message = "❌ Failed to get location: ${it.message}"
            isLocationLoading = false
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            requestLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!hasLocationPermission) {
            // Permission Request Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Location Permission Required",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "We need your location to confirm the delivery address and provide accurate service.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { permissionState.launchPermissionRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Allow Location Access")
                }
            }
        } else {
            // Main Checkout Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
            ) {
                // Customer Information Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Customer Information",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedTextField(
                            value = nameState.value,
                            onValueChange = { nameState.value = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., Evans Sabwa!") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = phoneState.value,
                            onValueChange = { phoneState.value = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., 0722123456") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = addressState.value,
                            onValueChange = { addressState.value = it },
                            label = { Text("Delivery Address") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., Near Nakuru Law Courts, opposite Equity Bank") },
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Location Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Delivery Location",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (latitude != null && longitude != null) {
                                IconButton(
                                    onClick = { isMapExpanded = !isMapExpanded }
                                ) {
                                    Icon(
                                        if (isMapExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isMapExpanded) "Collapse map" else "Expand map"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when {
                            isLocationLoading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator()
                                        Text(
                                            "📍 Getting your location...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            latitude != null && longitude != null -> {
                                // Location found - show map
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isMapExpanded) 300.dp else 160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    OpenStreetMapView(
                                        context = context,
                                        latitude = latitude!!,
                                        longitude = longitude!!
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Location coordinates info
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Location Confirmed",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = "Coordinates: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            else -> {
                                // Location failed
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            "❌ Location not available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Button(
                                            onClick = { requestLocation() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Retry Location")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Message Display
                if (message.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                message.contains("success", true) -> MaterialTheme.colorScheme.primaryContainer
                                message.contains("found successfully", true) -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                message.contains("success", true) -> MaterialTheme.colorScheme.onPrimaryContainer
                                message.contains("found successfully", true) -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }

                // Confirm Order Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Button(
                        onClick = {
                            if (nameState.value.text.isBlank() || phoneState.value.text.isBlank() || addressState.value.text.isBlank()) {
                                message = "❌ Please fill in all fields."
                                return@Button
                            }

                            if (latitude == null || longitude == null) {
                                message = "❌ Location not available. Please wait or retry location."
                                return@Button
                            }

                            userId?.let { uid ->
                                coroutineScope.launch {
                                    isSubmitting = true
                                    message = "⏳ Submitting order..."

                                    try {
                                        val cartSnapshot = db.collection("users").document(uid).collection("cart").get().await()
                                        val cartItems = cartSnapshot.documents.mapNotNull { it.toObject(CartItem::class.java) }

                                        if (cartItems.isEmpty()) {
                                            message = "❌ Your cart is empty."
                                            isSubmitting = false
                                            return@launch
                                        }

                                        val total = cartItems.sumOf { it.price * it.quantity }

                                        val currentTime = Date()
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                                        val order = hashMapOf(
                                            "userId" to uid,
                                            "name" to nameState.value.text,
                                            "phone" to phoneState.value.text,
                                            "address" to addressState.value.text,
                                            "latitude" to latitude,
                                            "longitude" to longitude,
                                            "items" to cartItems.map { it.toMap() },
                                            "total" to total,
                                            "date" to dateFormat.format(currentTime),
                                            "time" to timeFormat.format(currentTime),
                                            "timestamp" to Timestamp.now()
                                        )

                                        db.collection("orders")
                                            .add(order)
                                            .addOnSuccessListener {
                                                // Clear cart after successful order
                                                db.collection("users").document(uid).collection("cart").get()
                                                    .addOnSuccessListener { snapshot ->
                                                        for (doc in snapshot.documents) {
                                                            doc.reference.delete()
                                                        }
                                                        message = "✅ Order placed successfully!"

                                                        // Navigate back to home after a delay
                                                        coroutineScope.launch {
                                                            kotlinx.coroutines.delay(2000)
                                                            navController.popBackStack("home", inclusive = false)
                                                        }
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                message = "❌ Failed to place order: ${e.message}"
                                            }
                                            .addOnCompleteListener {
                                                isSubmitting = false
                                            }
                                    } catch (e: Exception) {
                                        message = "❌ Error: ${e.message}"
                                        isSubmitting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(16.dp),
                        enabled = !isSubmitting && latitude != null && longitude != null,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        } else {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (isSubmitting) "Placing Order..." else "Confirm Order",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom spacing for better scrolling
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun OpenStreetMapView(
    context: Context,
    latitude: Double,
    longitude: Double
) {
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)

                val userLocation = GeoPoint(latitude, longitude)
                controller.setCenter(userLocation)

                // Add marker for user location
                val marker = Marker(this)
                marker.position = userLocation
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Your Location"
                marker.snippet = "Order will be delivered here"
                marker.icon = context.getDrawable(android.R.drawable.ic_dialog_map)

                overlays.add(marker)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { mapView ->
        // Update map when coordinates change
        val userLocation = GeoPoint(latitude, longitude)
        mapView.controller.setCenter(userLocation)

        // Clear existing markers and add new one
        mapView.overlays.clear()
        val marker = Marker(mapView)
        marker.position = userLocation
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Your Location"
        marker.snippet = "Order will be delivered here"
        marker.icon = context.getDrawable(android.R.drawable.ic_dialog_map)

        mapView.overlays.add(marker)
        mapView.invalidate()
    }
}