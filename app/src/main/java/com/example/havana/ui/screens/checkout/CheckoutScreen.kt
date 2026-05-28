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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havana.R
import com.example.havana.data.mock.MockData
import com.example.havana.data.model.CartItem
import com.example.havana.data.model.CheckoutState
import com.example.havana.data.model.DeliveryAddress
import com.example.havana.data.model.Order
import com.example.havana.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit = {},
    onOrderSuccess: (orderNumber: String, order: Order) -> Unit = { _, _ -> },
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

    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight

    LaunchedEffect(savedAddress) {
        if (savedAddress != null) {
            viewModel.setDeliveryAddress(savedAddress)
        }
    }

    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var block by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val deliveryFee = 1.500
    val total = subtotal + deliveryFee

    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            val response = (checkoutState as CheckoutState.Success).order
            val fullOrder = viewModel.lastPlacedOrder.value
            if (fullOrder != null) {
                onOrderSuccess(response.orderNumber, fullOrder)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.checkout_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    shadowElevation = 16.dp,
                    color = cardColor
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
                            Text(stringResource(R.string.total), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                            Text("KD ${String.format("%.3f", total)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val currentAddr = deliveryAddress
                                if (currentAddr != null) {
                                    val mergedAddr = currentAddr.copy(
                                        block = block,
                                        street = street,
                                        building = building,
                                        floor = floor,
                                        apartment = apartment
                                    )
                                    viewModel.setDeliveryAddress(mergedAddr)
                                }
                                viewModel.placeOrder(customerName, phone, notes)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = checkoutState !is CheckoutState.Loading
                        ) {
                            if (checkoutState is CheckoutState.Loading) {
                                CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            } else {
                                Text(stringResource(R.string.checkout_place_order), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (checkoutState is CheckoutState.Error) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) BannerErrorBgDark else BannerErrorBgLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = if (isDark) BannerErrorFgDark else BannerErrorFgLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                (checkoutState as CheckoutState.Error).message,
                                color = if (isDark) BannerErrorFgDark else BannerErrorFgLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Text(stringResource(R.string.checkout_order_summary), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            items(cartItems) { item ->
                CheckoutItemCard(item)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.subtotal), fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
                            Text("KD ${String.format("%.3f", subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.delivery_fee), fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
                            Text("KD ${String.format("%.3f", deliveryFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.total), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                            Text("KD ${String.format("%.3f", total)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Text(stringResource(R.string.checkout_delivery_address), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            item {
                if (deliveryAddress != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primary.copy(alpha = 0.05f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    deliveryAddress!!.fullAddress,
                                    fontSize = 13.sp,
                                    color = colorScheme.onBackground,
                                    lineHeight = 19.sp
                                )
                                Text(
                                    stringResource(R.string.lat_lon, String.format("%.4f", deliveryAddress!!.latitude), String.format("%.4f", deliveryAddress!!.longitude)),
                                    fontSize = 10.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF3A2F1A) else Color(0xFFFFF8E1)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                stringResource(R.string.checkout_pick_address_warning),
                                fontSize = 12.sp,
                                color = if (isDark) GoldLight else Color(0xFF92400E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onPickOnMap,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (deliveryAddress != null) stringResource(R.string.checkout_change_location) else stringResource(R.string.checkout_pick_location),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                Text(stringResource(R.string.checkout_address_details), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            // Address fields
            item { CheckoutField(stringResource(R.string.checkout_block), block, { block = it }, checkoutState, colorScheme) }
            item { CheckoutField(stringResource(R.string.checkout_street), street, { street = it }, checkoutState, colorScheme) }
            item { CheckoutField(stringResource(R.string.checkout_building), building, { building = it }, checkoutState, colorScheme) }
            item { CheckoutField(stringResource(R.string.checkout_floor), floor, { floor = it }, checkoutState, colorScheme) }
            item { CheckoutField(stringResource(R.string.checkout_apartment), apartment, { apartment = it }, checkoutState, colorScheme) }

            item {
                Text(stringResource(R.string.checkout_contact_info), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            item { CheckoutField(stringResource(R.string.checkout_full_name), customerName, { customerName = it }, checkoutState, colorScheme) }
            item { CheckoutField(stringResource(R.string.checkout_contact_number), phone, { phone = it }, checkoutState, colorScheme) }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.checkout_order_notes_label)) },
                    placeholder = { Text(stringResource(R.string.checkout_order_notes_placeholder)) },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        cursorColor = colorScheme.primary
                    ),
                    enabled = checkoutState !is CheckoutState.Loading
                )
            }

            item {
                Text(stringResource(R.string.checkout_payment_method), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.primary.copy(alpha = 0.05f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("\uD83D\uDCB5", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.checkout_cash_on_delivery), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
                            Text(stringResource(R.string.checkout_pay_when_arrives), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CheckoutField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    checkoutState: CheckoutState,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            focusedLabelColor = colorScheme.primary,
            cursorColor = colorScheme.primary
        ),
        enabled = checkoutState !is CheckoutState.Loading
    )
}

@Composable
fun CheckoutItemCard(item: CartItem) {
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (ThemeManager.isDarkMode) CardDark else CardLight

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                    .background(colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(MockData.categoryEmoji(item.category), fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${stringResource(R.string.qty)}: ${item.quantity} x KD ${String.format("%.3f", item.price)}", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            }
            Text("KD ${String.format("%.3f", item.price * item.quantity)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
        }
    }
}