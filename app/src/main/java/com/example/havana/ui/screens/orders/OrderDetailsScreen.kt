package com.example.havana.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.havana.data.model.Order
import com.example.havana.data.model.OrderItem
import com.example.havana.data.model.statusColor
import com.example.havana.data.model.statusLabel
import com.example.havana.data.model.statusEmoji
import com.example.havana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    order: Order?,
    onBackClick: () -> Unit = {},
) {
    if (order == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Order not found", color = TextSecondary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        order.orderNumber,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== STATUS CARD =====
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(order.statusEmoji(), fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = order.statusColor().copy(alpha = 0.12f)
                        ) {
                            Text(
                                order.statusLabel(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = order.statusColor(),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Ordered on ${order.createdAt}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ===== STATUS TIMELINE =====
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Order Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        val statuses = listOf("pending", "confirmed", "preparing", "out_for_delivery", "delivered")
                        val currentIndex = statuses.indexOf(order.status)
                        val isCancelled = order.status == "cancelled"

                        statuses.forEachIndexed { index, status ->
                            val isCompleted = !isCancelled && index <= currentIndex
                            val isCurrent = !isCancelled && index == currentIndex

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Dot
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isCancelled -> Color(0xFFE5E5E5)
                                        isCurrent -> order.statusColor()
                                        isCompleted -> Success
                                        else -> Color(0xFFE5E5E5)
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            when {
                                                isCancelled && index == 0 -> "❌"
                                                isCompleted -> "✓"
                                                isCurrent -> "●"
                                                else -> ""
                                            },
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    when (status) {
                                        "pending" -> "Order Placed"
                                        "confirmed" -> "Confirmed"
                                        "preparing" -> "Preparing"
                                        "out_for_delivery" -> "Out for Delivery"
                                        "delivered" -> "Delivered"
                                        else -> status
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                    color = when {
                                        isCancelled -> TextSecondary
                                        isCurrent -> TextPrimary
                                        isCompleted -> TextPrimary
                                        else -> TextSecondary.copy(alpha = 0.5f)
                                    }
                                )
                            }

                            // Connector line
                            if (index < statuses.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 11.dp)
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(
                                            if (!isCancelled && isCompleted && index < currentIndex) Success
                                            else Color(0xFFE5E5E5)
                                        )
                                )
                            }
                        }

                        if (isCancelled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "This order was cancelled",
                                fontSize = 13.sp,
                                color = Error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ===== ITEMS =====
            item {
                Text("Items", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(order.items) { item ->
                OrderDetailItemCard(item)
            }

            // ===== TOTALS =====
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 13.sp, color = TextSecondary)
                            Text("KD ${String.format("%.3f", order.subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", fontSize = 13.sp, color = TextSecondary)
                            Text("KD ${String.format("%.3f", order.deliveryFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFE5E5E5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("KD ${String.format("%.3f", order.total)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Maroon)
                        }
                    }
                }
            }

            // ===== DELIVERY ADDRESS =====
            item {
                Text("Delivery Address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Maroon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                order.deliveryAddress.fullAddress,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Lat: ${String.format("%.6f", order.deliveryAddress.latitude)}, Lon: ${String.format("%.6f", order.deliveryAddress.longitude)}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // ===== CONTACT INFO =====
            item {
                Text("Contact Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        InfoRow("Name", order.customerName)
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoRow("Phone", order.phone)
                        if (order.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            InfoRow("Notes", order.notes)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoRow("Payment", "Cash on Delivery")
                    }
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun OrderDetailItemCard(item: OrderItem) {
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

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

private fun OrderItem.categoryEmoji(): String {
    return when (category.lowercase()) {
        "roses" -> "🌹"
        "bouquets" -> "💐"
        "arrangements" -> "🌺"
        "gifts" -> "🎁"
        "plants" -> "🪴"
        else -> "🌸"
    }
}