package com.example.havana.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.havana.data.model.Order
import com.example.havana.data.model.OrderItem
import com.example.havana.ui.theme.*

@Composable
fun OrderConfirmationScreen(
    order: Order?,
    onViewOrders: () -> Unit = {},
    onContinueShopping: () -> Unit = {},
) {
    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Order not found", color = TextSecondary) }
        return
    }

    Scaffold(containerColor = CreamBg) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // SUCCESS HEADER
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Success.copy(alpha = 0.1f)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(52.dp)) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Order Confirmed!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Thank you for your order", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = Maroon.copy(alpha = 0.08f)) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Order Number", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(order.orderNumber, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Maroon)
                            if (order.createdAt.isNotBlank()) { Spacer(modifier = Modifier.height(2.dp)); Text("Placed on ${order.createdAt}", fontSize = 11.sp, color = TextSecondary) }
                        }
                    }
                }
            }
            // ORDER ITEMS
            item { Text("Order Items", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
            items(order.items) { item -> ConfirmationItemCard(item) }
            // TOTALS
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Subtotal", fontSize = 13.sp, color = TextSecondary); Text("KD ${String.format("%.3f", order.subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Delivery Fee", fontSize = 13.sp, color = TextSecondary); Text("KD ${String.format("%.3f", order.deliveryFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary) }
                        Spacer(modifier = Modifier.height(8.dp)); HorizontalDivider(color = Color(0xFFE5E5E5)); Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("KD ${String.format("%.3f", order.total)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Maroon) }
                    }
                }
            }
            // RECEIPT DIVIDER
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(4.dp)); HorizontalDivider(color = Color(0xFFE5E5E5)); Spacer(modifier = Modifier.height(4.dp))
                    Text("RECEIPT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp)); HorizontalDivider(color = Color(0xFFE5E5E5))
                }
            }
            // DELIVERY ADDRESS
            item { Text("Delivery Address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Maroon, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) { Text(order.deliveryAddress.fullAddress, fontSize = 13.sp, color = TextPrimary, lineHeight = 19.sp) }
                        }
                        val addr = order.deliveryAddress
                        val hasDetails = addr.block.isNotBlank() || addr.street.isNotBlank() || addr.building.isNotBlank() || addr.floor.isNotBlank() || addr.apartment.isNotBlank()
                        if (hasDetails) { Spacer(modifier = Modifier.height(10.dp)); HorizontalDivider(color = Color(0xFFF0F0F0)); Spacer(modifier = Modifier.height(10.dp)) }
                        if (addr.block.isNotBlank()) { ReceiptInfoRow("Block", addr.block); Spacer(modifier = Modifier.height(4.dp)) }
                        if (addr.street.isNotBlank()) { ReceiptInfoRow("Street", addr.street); Spacer(modifier = Modifier.height(4.dp)) }
                        if (addr.building.isNotBlank()) { ReceiptInfoRow("Building", addr.building); Spacer(modifier = Modifier.height(4.dp)) }
                        if (addr.floor.isNotBlank()) { ReceiptInfoRow("Floor", addr.floor); Spacer(modifier = Modifier.height(4.dp)) }
                        if (addr.apartment.isNotBlank()) { ReceiptInfoRow("Apartment", addr.apartment); Spacer(modifier = Modifier.height(4.dp)) }
                    }
                }
            }
            // CUSTOMER INFO
            item { Text("Customer Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        ReceiptInfoRow("Name", order.customerName); Spacer(modifier = Modifier.height(6.dp))
                        ReceiptInfoRow("Phone", order.phone)
                        if (order.notes.isNotBlank()) { Spacer(modifier = Modifier.height(6.dp)); ReceiptInfoRow("Notes", order.notes) }
                        Spacer(modifier = Modifier.height(6.dp)); ReceiptInfoRow("Payment", "Cash on Delivery")
                    }
                }
            }
            // ACTION BUTTONS
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onViewOrders, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Maroon), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("View My Orders", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onContinueShopping, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Maroon), border = androidx.compose.foundation.BorderStroke(1.dp, Maroon), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Continue Shopping", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ConfirmationItemCard(item: OrderItem) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(CreamBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text(item.categoryEmoji(), fontSize = 18.sp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("Qty: ${item.quantity} x KD ${String.format("%.3f", item.price)}", fontSize = 12.sp, color = TextSecondary) }
            Text("KD ${String.format("%.3f", item.price * item.quantity)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Maroon)
        }
    }
}

@Composable
private fun ReceiptInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) { Text("$label: ", fontSize = 13.sp, color = TextSecondary); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary) }
}

private fun OrderItem.categoryEmoji(): String {
    return when (category.lowercase()) { "roses" -> "🌹"; "bouquets" -> "💐"; "arrangements" -> "🌺"; "gifts" -> "🎁"; "plants" -> "🪴"; else -> "🌸" }
}