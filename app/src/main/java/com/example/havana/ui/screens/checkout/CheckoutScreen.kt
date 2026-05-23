package com.example.havana.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havana.data.cart.CartManager
import com.example.havana.data.model.CartItem
import com.example.havana.data.model.CheckoutState
import com.example.havana.data.model.DeliveryAddress
import com.example.havana.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun CheckoutScreen(
    onBackClick: () -> Unit = {},
    onOrderSuccess: (String) -> Unit = {},
    onPickOnMap: () -> Unit = {},
    savedAddress: DeliveryAddress? = null,
    viewModel: CheckoutViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    val cartItems by viewModel.cartItems.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val deliveryAddress by viewModel.deliveryAddress.collectAsState()

    // Sync saved address from MapPicker
    LaunchedEffect(savedAddress) {
        if (savedAddress != null) {
            viewModel.setDeliveryAddress(savedAddress)
        }
    }

    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val deliveryFee = 1.500
    val total = subtotal + deliveryFee

    // Handle success
    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            val order = (checkoutState as CheckoutState.Success).order
            onOrderSuccess(order.orderNumber)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Checkout",
                        fontSize = 20.sp,
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
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("KD ${String.format("%.3f", total)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Maroon)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.placeOrder(customerName, phone, notes)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = checkoutState !is CheckoutState.Loading
                        ) {
                            if (checkoutState is CheckoutState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            } else {
                                Text("Place Order  •  Cash on Delivery", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        },
        containerColor = CreamBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== ERROR BANNER =====
            if (checkoutState is CheckoutState.Error) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                (checkoutState as CheckoutState.Error).message,
                                color = Color(0xFFD32F2F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ===== ORDER SUMMARY =====
            item {
                Text("Order Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(cartItems) { item ->
                CheckoutItemCard(item)
            }

            // Totals
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 13.sp, color = TextSecondary)
                            Text("KD ${String.format("%.3f", subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", fontSize = 13.sp, color = TextSecondary)
                            Text("KD ${String.format("%.3f", deliveryFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFE5E5E5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("KD ${String.format("%.3f", total)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Maroon)
                        }
                    }
                }
            }

            // ===== DELIVERY ADDRESS =====
            item {
                Text("Delivery Address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item {
                if (deliveryAddress != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Maroon.copy(alpha = 0.05f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Maroon, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    deliveryAddress!!.fullAddress,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 19.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Lat: ${String.format("%.4f", deliveryAddress!!.latitude)}, Lon: ${String.format("%.4f", deliveryAddress!!.longitude)}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onPickOnMap,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Maroon),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Maroon),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (deliveryAddress != null) "Change Location" else "Pick Location on Map",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ===== CUSTOMER INFO =====
            item {
                Text("Contact Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Maroon,
                        focusedLabelColor = Maroon,
                        cursorColor = Maroon
                    ),
                    enabled = checkoutState !is CheckoutState.Loading
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Number * (+965)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Maroon,
                        focusedLabelColor = Maroon,
                        cursorColor = Maroon
                    ),
                    enabled = checkoutState !is CheckoutState.Loading
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Order Notes (optional)") },
                    placeholder = { Text("e.g. Ring doorbell, leave at gate") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Maroon,
                        focusedLabelColor = Maroon,
                        cursorColor = Maroon
                    ),
                    enabled = checkoutState !is CheckoutState.Loading
                )
            }

            // ===== PAYMENT METHOD =====
            item {
                Text("Payment Method", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Maroon.copy(alpha = 0.05f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💵", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Cash on Delivery", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Pay when your order arrives", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun CheckoutItemCard(item: CartItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(CreamBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.categoryEmoji(), fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Qty: ${item.quantity} x KD ${String.format("%.3f", item.price)}", fontSize = 11.sp, color = TextSecondary)
            }
            Text("KD ${String.format("%.3f", item.price * item.quantity)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Maroon)
        }
    }
}

private fun CartItem.categoryEmoji(): String {
    return when (category.lowercase()) {
        "roses" -> "🌹"
        "bouquets" -> "💐"
        "arrangements" -> "🌺"
        "gifts" -> "🎁"
        "plants" -> "🪴"
        else -> "🌸"
    }
}