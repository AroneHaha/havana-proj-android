package com.example.havana.ui.screens.checkout

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.havana.data.model.DeliveryAddress
import com.example.havana.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class SearchResult(
    val name: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
)

private fun nominatimGet(urlString: String): String? {
    return try {
        val url = URL(urlString)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "HavanaApp/1.0 (Android)")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }
        val responseCode = conn.responseCode
        if (responseCode == 200) {
            conn.inputStream.bufferedReader().readText()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    onAddressConfirmed: (DeliveryAddress) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedAddress by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val kuwaitCenter = GeoPoint(29.3759, 47.9774)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Debounced search suggestions
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(500) // debounce 500ms
            withContext(Dispatchers.IO) {
                try {
                    val query = URLEncoder.encode(searchQuery, "UTF-8")
                    val url = "https://nominatim.openstreetmap.org/search?q=$query&format=json&countrycodes=kw&limit=5&addressdetails=1"
                    val response = nominatimGet(url)
                    if (response != null) {
                        val jsonArray = org.json.JSONArray(response)
                        val results = mutableListOf<SearchResult>()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val displayName = item.optString("display_name", "")
                            val shortName = try {
                                val address = item.optJSONObject("address")
                                address?.optString("suburb")
                                    ?: address?.optString("neighbourhood")
                                    ?: address?.optString("city")
                                    ?: address?.optString("town")
                                    ?: displayName.split(",").firstOrNull()?.trim()
                                    ?: ""
                            } catch (_: Exception) {
                                displayName.split(",").firstOrNull()?.trim() ?: ""
                            }
                            results.add(
                                SearchResult(
                                    name = shortName,
                                    displayName = displayName,
                                    latitude = item.getDouble("lat"),
                                    longitude = item.getDouble("lon"),
                                )
                            )
                        }
                        withContext(Dispatchers.Main) {
                            searchResults = results
                            showSuggestions = results.isNotEmpty()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            searchResults = emptyList()
                            showSuggestions = false
                        }
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        searchResults = emptyList()
                        showSuggestions = false
                    }
                }
            }
        } else {
            searchResults = emptyList()
            showSuggestions = false
        }
    }

    fun goToLocation(lat: Double, lon: Double, displayName: String) {
        val point = GeoPoint(lat, lon)
        selectedPoint = point
        selectedAddress = displayName
        showSuggestions = false
        mapViewRef?.let { map ->
            map.controller.animateTo(point, 16.0, 1000)
            map.overlays.removeAll { it is Marker }
            val marker = Marker(map).apply {
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Delivery Location"
            }
            map.overlays.add(marker)
            map.invalidate()
        }
    }

    fun reverseGeocode(point: GeoPoint) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = "https://nominatim.openstreetmap.org/reverse?lat=${point.latitude}&lon=${point.longitude}&format=json&countrycodes=kw&addressdetails=1"
                val response = nominatimGet(url)
                if (response != null) {
                    val json = JSONObject(response)
                    val address = json.optJSONObject("address")
                    val road = address?.optString("road") ?: ""
                    val suburb = address?.optString("suburb") ?: address?.optString("neighbourhood") ?: ""
                    val city = address?.optString("city") ?: address?.optString("town") ?: ""
                    val state = address?.optString("state") ?: ""
                    val country = address?.optString("country") ?: "Kuwait"

                    // Build readable address
                    val parts = mutableListOf<String>()
                    if (road.isNotEmpty()) parts.add(road)
                    if (suburb.isNotEmpty()) parts.add(suburb)
                    if (city.isNotEmpty()) parts.add(city)
                    if (state.isNotEmpty()) parts.add(state)
                    if (country.isNotEmpty()) parts.add(country)

                    withContext(Dispatchers.Main) {
                        selectedAddress = if (parts.isNotEmpty()) parts.joinToString(", ") else json.optString("display_name", "Selected location")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        selectedAddress = "Lat: ${String.format("%.4f", point.latitude)}, Lon: ${String.format("%.4f", point.longitude)}"
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    selectedAddress = "Lat: ${String.format("%.4f", point.latitude)}, Lon: ${String.format("%.4f", point.longitude)}"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pick Delivery Location",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Maroon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBg)
            )
        },
        containerColor = CreamBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ===== SEARCH BAR =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            showSuggestions = it.length >= 2
                        },
                        placeholder = { Text("Search area in Kuwait...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            cursorColor = Maroon,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (searchResults.isNotEmpty()) {
                                val first = searchResults.first()
                                goToLocation(first.latitude, first.longitude, first.displayName)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Go")
                    }
                }

                // ===== SEARCH SUGGESTIONS =====
                if (showSuggestions && searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .heightIn(max = 180.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(searchResults) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = result.name
                                        goToLocation(result.latitude, result.longitude, result.displayName)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📍", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        result.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        result.displayName,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(horizontal = 14.dp))
                        }
                    }
                }

                // ===== MAP =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setCenter(kuwaitCenter)
                                controller.setZoom(12.0)

                                val mapEventsReceiver = object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                        selectedPoint = p
                                        showSuggestions = false
                                        overlays.removeAll { it is Marker }
                                        val marker = Marker(this@apply).apply {
                                            position = p
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            title = "Delivery Location"
                                        }
                                        overlays.add(marker)
                                        invalidate()
                                        reverseGeocode(p)
                                        return true
                                    }
                                    override fun longPressHelper(p: GeoPoint): Boolean = false
                                }
                                overlays.add(MapEventsOverlay(mapEventsReceiver))
                                mapViewRef = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ===== SELECTED ADDRESS PANEL =====
                if (selectedPoint != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Delivery Address",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                selectedAddress,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${String.format("%.6f", selectedPoint!!.latitude)}, ${String.format("%.6f", selectedPoint!!.longitude)}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val address = DeliveryAddress(
                                        fullAddress = selectedAddress,
                                        latitude = selectedPoint!!.latitude,
                                        longitude = selectedPoint!!.longitude
                                    )
                                    onAddressConfirmed(address)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Confirm Address", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 4.dp,
                        color = Color.White
                    ) {
                        Text(
                            "Tap on the map or search to select your delivery location",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}