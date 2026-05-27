package com.example.havana.ui.screens.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havana.data.model.Order
import com.example.havana.data.model.OrderListState
import com.example.havana.data.model.statusColor
import com.example.havana.data.model.statusLabel
import com.example.havana.data.model.statusEmoji
import com.example.havana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val viewModel: OrdersViewModel = viewModel()
    val orderListState by viewModel.orderListState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight
    val navBarColor = if (isDark) NavBarDark else NavBarLight

    val filters = listOf(
        "all" to "All",
        "pending" to "\u23F3 Pending",
        "confirmed" to "\u2705 Confirmed",
        "preparing" to "\uD83D\uDCE6 Preparing",
        "out_for_delivery" to "\uD83D\uDE9A Delivery",
        "delivered" to "\uD83C\uDF89 Done",
        "cancelled" to "\u274C Cancelled",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Orders",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = navBarColor,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = colorScheme.onSurfaceVariant,
                        unselectedTextColor = colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onCartClick,
                    icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = colorScheme.onSurfaceVariant,
                        unselectedTextColor = colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = "Orders") },
                    label = { Text("Orders", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorScheme.primary,
                        selectedTextColor = colorScheme.primary,
                        indicatorColor = colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = colorScheme.onSurfaceVariant,
                        unselectedTextColor = colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { viewModel.setFilter(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary,
                            containerColor = cardColor,
                            labelColor = colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = colorScheme.outline,
                            selectedBorderColor = colorScheme.primary,
                            enabled = true,
                            selected = selectedFilter == key
                        )
                    )
                }
            }

            when (orderListState) {
                is OrderListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                }
                is OrderListState.Success -> {
                    val orders = (orderListState as OrderListState.Success).orders
                    if (orders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("\uD83D\uDCCB", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No orders found", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
                                Text("Your orders will appear here", fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(orders) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = { onOrderClick(order.id) }
                                )
                            }
                        }
                    }
                }
                is OrderListState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((orderListState as OrderListState.Error).message, color = Error)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.orderNumber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = order.statusColor().copy(alpha = 0.12f)
                ) {
                    Text(
                        "${order.statusEmoji()} ${order.statusLabel()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = order.statusColor(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                order.createdAt,
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val itemsPreview = order.items.take(2).joinToString(", ") { "${it.name} x${it.quantity}" }
            val moreCount = order.items.size - 2
            Text(
                if (moreCount > 0) "$itemsPreview +$moreCount more" else itemsPreview,
                fontSize = 13.sp,
                color = colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${order.items.sumOf { it.quantity }} items",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    "KD ${String.format("%.3f", order.total)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            }
        }
    }
}