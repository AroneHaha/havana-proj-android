package com.example.havana.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Person
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
import com.example.havana.data.model.CartItem
import com.example.havana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
) {
    val viewModel: CartViewModel = viewModel()
    val cartItems by viewModel.cartItems.collectAsState()
    val total by viewModel.total.collectAsState()
    val itemCount by viewModel.itemCount.collectAsState()

    var showRemoveDialog by remember { mutableStateOf(false) }
    var itemToRemove by remember { mutableStateOf<CartItem?>(null) }

    if (showRemoveDialog && itemToRemove != null) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false; itemToRemove = null },
            title = { Text("Remove Item", fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${itemToRemove?.name}\" from your cart?") },
            confirmButton = {
                TextButton(onClick = {
                    itemToRemove?.let { viewModel.removeItem(it.productId) }
                    showRemoveDialog = false
                    itemToRemove = null
                }) { Text("Remove", color = Error, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false; itemToRemove = null }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                Surface(shadowElevation = 16.dp, color = Color.White) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total ($itemCount items)", fontSize = 14.sp, color = TextSecondary)
                            Text("KD ${String.format("%.3f", total)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Maroon)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onCheckoutClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) { Text("Proceed to Checkout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
                    }
                }
            } else {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    NavigationBarItem(selected = false, onClick = onHomeClick, icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") }, label = { Text("Home", fontSize = 11.sp) }, colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary))
                    NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart") }, label = { Text("Cart", fontSize = 11.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Maroon, selectedTextColor = Maroon, indicatorColor = Maroon.copy(alpha = 0.1f)))
                    NavigationBarItem(selected = false, onClick = onOrdersClick, icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = "Orders") }, label = { Text("Orders", fontSize = 11.sp) }, colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary))
                    NavigationBarItem(selected = false, onClick = onProfileClick, icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") }, label = { Text("Profile", fontSize = 11.sp) }, colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary))
                }
            }
        },
        containerColor = CreamBg
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your cart is empty", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Browse our collection and add some beautiful flowers!", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 40.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(onClick = onHomeClick, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Maroon), border = androidx.compose.foundation.BorderStroke(1.dp, Maroon)) { Text("Browse Products", fontWeight = FontWeight.SemiBold) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cartItems, key = { it.productId }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { viewModel.increaseQuantity(item.productId) },
                        onDecrease = { viewModel.decreaseQuantity(item.productId) },
                        onRemove = { itemToRemove = item; showRemoveDialog = true }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFE5E5E5))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 14.sp, color = TextSecondary)
                        Text("KD ${String.format("%.3f", total)}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Fee", fontSize = 14.sp, color = TextSecondary)
                        Text("KD 1.500", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color(0xFFE5E5E5))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Grand Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("KD ${String.format("%.3f", total + 1.500)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Maroon)
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(80.dp).background(CreamBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Text(item.categoryEmoji(), fontSize = 32.sp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text("KD ${String.format("%.3f", item.price)} each", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDecrease, shape = CircleShape, modifier = Modifier.size(30.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Maroon), border = androidx.compose.foundation.BorderStroke(1.dp, Maroon.copy(alpha = 0.4f))) { Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                    Text("${item.quantity}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedButton(onClick = onIncrease, shape = CircleShape, modifier = Modifier.size(30.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Maroon), border = androidx.compose.foundation.BorderStroke(1.dp, Maroon.copy(alpha = 0.4f))) { Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("KD ${String.format("%.3f", item.price * item.quantity)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Maroon)
            }
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