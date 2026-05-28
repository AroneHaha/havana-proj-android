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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.havana.data.model.Order
import com.example.havana.R
import com.example.havana.data.model.OrderItem
import com.example.havana.ui.theme.*

@Composable
fun OrderConfirmationScreen(
    order: Order?,
    onViewOrders: () -> Unit = {},
    onContinueShopping: () -> Unit = {},
) {
    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight
    val dividerColor = if (isDark) DividerDark else DividerLight

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.order_not_found), color = colorScheme.onSurfaceVariant)
        }
        return
    }

    Scaffold(containerColor = colorScheme.background) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SUCCESS HEADER
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Success.copy(alpha = 0.1f)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(52.dp)) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.confirmation_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.confirmation_thank_you), fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = colorScheme.primary.copy(alpha = 0.08f)) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.confirmation_order_number), fontSize = 11.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(order.orderNumber, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                            if (order.createdAt.isNotBlank()) { Spacer(modifier = Modifier.height(2.dp)); Text(stringResource(R.string.confirmation_placed_on, order.createdAt), fontSize = 11.sp, color = colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
            // ORDER ITEMS
            item { Text(stringResource(R.string.confirmation_order_items), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground) }
            items(order.items) { item -> ConfirmationItemCard(item) }
            // TOTALS
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(stringResource(R.string.subtotal), fontSize = 13.sp, color = colorScheme.onSurfaceVariant); Text("KD ${String.format("%.3f", order.subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(stringResource(R.string.delivery_fee), fontSize = 13.sp, color = colorScheme.onSurfaceVariant); Text("KD ${String.format("%.3f", order.deliveryFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground) }
                        Spacer(modifier = Modifier.height(8.dp)); HorizontalDivider(color = dividerColor); Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(stringResource(R.string.total), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground); Text("KD ${String.format("%.3f", order.total)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary) }
                    }
                }
            }
            // RECEIPT DIVIDER
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(4.dp)); HorizontalDivider(color = dividerColor); Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.confirmation_receipt), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp)); HorizontalDivider(color = dividerColor)
                }
            }
            // DELIVERY ADDRESS
            item { Text(stringResource(R.string.confirmation_delivery_address), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground) }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) { Text(order.deliveryAddress.fullAddress, fontSize = 13.sp, color = colorScheme.onBackground, lineHeight = 19.sp) }
                        }
                        val addr = order.deliveryAddress
                        val hasDetails = addr.block.isNotBlank() || addr.street.isNotBlank() || addr.building.isNotBlank() || addr.floor.isNotBlank() || addr.apartment.isNotBlank()
                        if (hasDetails) { Spacer(modifier = Modifier.height(10.dp)); HorizontalDivider(color = dividerColor); Spacer(modifier = Modifier.height(10.dp)) }
                        if (addr.block.isNotBlank()) { ReceiptInfoRow(stringResource(R.string.order_block), addr.block); Spacer(modifier = Modifier.height(4.dp)) }
                        if (addr.street.isNotBlank()) { ReceiptInfoRow(stringResource(R.string.order_street), addr.street); Spacer(modifier = Modifier.height(4.dp)) }
                        if (addr.building.isNotBlank()) { ReceiptInfoRow(stringResource(R.string.order_building), addr.building); Spacer(modifier = Modifier.height(4.dp)) }
                        if (addr.floor.isNotBlank()) { ReceiptInfoRow(stringResource(R.string.order_floor), addr.floor); Spacer(modifier = Modifier.height(4.dp)) }
                        if (addr.apartment.isNotBlank()) { ReceiptInfoRow(stringResource(R.string.order_apartment), addr.apartment); Spacer(modifier = Modifier.height(4.dp)) }
                    }
                }
            }
            // CUSTOMER INFO
            item { Text(stringResource(R.string.confirmation_customer_info), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground) }
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        ReceiptInfoRow(stringResource(R.string.order_name), order.customerName); Spacer(modifier = Modifier.height(6.dp))
                        ReceiptInfoRow(stringResource(R.string.order_phone), order.phone)
                        if (order.notes.isNotBlank()) { Spacer(modifier = Modifier.height(6.dp)); ReceiptInfoRow(stringResource(R.string.order_notes), order.notes) }
                        Spacer(modifier = Modifier.height(6.dp)); ReceiptInfoRow(stringResource(R.string.order_payment), stringResource(R.string.order_cash_on_delivery))
                    }
                }
            }
            // ACTION BUTTONS
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onViewOrders, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text(stringResource(R.string.confirmation_view_orders), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onPrimary) }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onContinueShopping, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary), border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.primary), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text(stringResource(R.string.confirmation_continue_shopping), fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ConfirmationItemCard(item: OrderItem) {
    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text(item.categoryEmoji(), fontSize = 18.sp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${stringResource(R.string.qty)}: ${item.quantity} x KD ${String.format("%.3f", item.price)}", fontSize = 12.sp, color = colorScheme.onSurfaceVariant) }
            Text("KD ${String.format("%.3f", item.price * item.quantity)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
        }
    }
}

@Composable
private fun ReceiptInfoRow(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth()) { Text("$label: ", fontSize = 13.sp, color = colorScheme.onSurfaceVariant); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground) }
}

private fun OrderItem.categoryEmoji(): String {
    return when (category.lowercase()) { "roses" -> "\uD83C\uDF39"; "bouquets" -> "\uD83D\uDC90"; "arrangements" -> "\uD83C\uDF3A"; "gifts" -> "\uD83C\uDF81"; "plants" -> "\uD83E\uDEB4"; else -> "\uD83C\uDF38" }
}
