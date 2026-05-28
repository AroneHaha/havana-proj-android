package com.example.havana.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havana.R
import com.example.havana.data.mock.MockData
import com.example.havana.data.model.Order
import com.example.havana.data.model.OrderItem
import com.example.havana.data.model.Review
import com.example.havana.data.model.statusColor
import com.example.havana.data.model.localizedStatus
import com.example.havana.data.model.statusEmoji
import com.example.havana.data.repository.OrderRepository
import com.example.havana.data.session.SessionManager
import com.example.havana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String?,
    onBackClick: () -> Unit = {},
    onConfirmDelivery: (String) -> Unit = {},
    viewModel: OrderDetailsViewModel = viewModel(),
) {
    val order by OrderRepository.orders.collectAsState()
    val resolvedOrder = remember(orderId, order) { order.find { it.id == orderId } }

    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight
    val dividerColor = if (isDark) DividerDark else DividerLight

    if (resolvedOrder == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.order_not_found), color = colorScheme.onSurfaceVariant)
        }
        return
    }

    var currentStatus by remember { mutableStateOf(resolvedOrder.status) }
    val displayOrder = remember(resolvedOrder, currentStatus) {
        resolvedOrder.copy(status = currentStatus)
    }

    var showReviewSheet by remember { mutableStateOf(false) }
    var reviewingItem by remember { mutableStateOf<OrderItem?>(null) }
    var selectedRating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }

    val itemReviews by viewModel.itemReviews.collectAsState()
    val isSubmittingReview by viewModel.isSubmittingReview.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        displayOrder.orderNumber,
                        fontSize = 18.sp,
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
        containerColor = colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(displayOrder.statusEmoji(), fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = displayOrder.statusColor().copy(alpha = 0.12f)
                        ) {
                            Text(
                                displayOrder.localizedStatus(
                                    stringResource(R.string.status_pending),
                                    stringResource(R.string.status_confirmed),
                                    stringResource(R.string.status_preparing),
                                    stringResource(R.string.status_out_for_delivery),
                                    stringResource(R.string.status_delivered),
                                    stringResource(R.string.status_cancelled),
                                ),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = displayOrder.statusColor(),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.order_ordered_on, displayOrder.createdAt),
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(stringResource(R.string.order_progress), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(12.dp))

                        val statuses = listOf("pending", "confirmed", "preparing", "out_for_delivery", "delivered")
                        val currentIndex = statuses.indexOf(displayOrder.status)
                        val isCancelled = displayOrder.status == "cancelled"

                        statuses.forEachIndexed { index, status ->
                            val isCompleted = !isCancelled && index <= currentIndex
                            val isCurrent = !isCancelled && index == currentIndex

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isCancelled -> colorScheme.outlineVariant
                                        isCurrent -> displayOrder.statusColor()
                                        isCompleted -> Success
                                        else -> colorScheme.outlineVariant
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            when {
                                                isCancelled && index == 0 -> "X"
                                                isCompleted -> "\u2713"
                                                isCurrent -> "\u25CF"
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
                                        "pending" -> stringResource(R.string.order_placed)
                                        "confirmed" -> stringResource(R.string.order_confirmed)
                                        "preparing" -> stringResource(R.string.order_preparing)
                                        "out_for_delivery" -> stringResource(R.string.order_out_for_delivery)
                                        "delivered" -> stringResource(R.string.order_delivered)
                                        else -> status
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                    color = when {
                                        isCancelled -> colorScheme.onSurfaceVariant
                                        isCurrent -> colorScheme.onBackground
                                        isCompleted -> colorScheme.onBackground
                                        else -> colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            }

                            if (index < statuses.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 11.dp)
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(
                                            if (!isCancelled && isCompleted && index < currentIndex) Success
                                            else colorScheme.outlineVariant
                                        )
                                )
                            }
                        }

                        if (isCancelled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.order_cancelled_status),
                                fontSize = 13.sp,
                                color = Error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (displayOrder.status == "out_for_delivery") {
                item {
                    Button(
                        onClick = {
                            currentStatus = "delivered"
                            onConfirmDelivery(resolvedOrder.id)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.order_confirm_delivery),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.order_items), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                    if (displayOrder.status == "delivered") {
                        Text(
                            stringResource(R.string.order_tap_to_rate),
                            fontSize = 12.sp,
                            color = colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            items(displayOrder.items) { item ->
                val existingReview = itemReviews[item.productId]
                OrderDetailItemCard(
                    item = item,
                    showRateButton = displayOrder.status == "delivered" && existingReview == null,
                    existingReview = existingReview,
                    onRateClick = {
                        reviewingItem = item
                        selectedRating = 0
                        reviewText = ""
                        showReviewSheet = true
                    }
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.subtotal), fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
                            Text("KD ${String.format("%.3f", displayOrder.subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.delivery_fee), fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
                            Text("KD ${String.format("%.3f", displayOrder.deliveryFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = dividerColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.total), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                            Text("KD ${String.format("%.3f", displayOrder.total)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Text(stringResource(R.string.order_delivery_address), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    displayOrder.deliveryAddress.fullAddress,
                                    fontSize = 13.sp,
                                    color = colorScheme.onBackground,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        val addr = displayOrder.deliveryAddress
                        val hasDetails = addr.block.isNotBlank() ||
                                addr.street.isNotBlank() ||
                                addr.building.isNotBlank() ||
                                addr.floor.isNotBlank() ||
                                addr.apartment.isNotBlank()

                        if (hasDetails) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = dividerColor)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (addr.block.isNotBlank()) {
                            InfoRow(stringResource(R.string.order_block), addr.block)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (addr.street.isNotBlank()) {
                            InfoRow(stringResource(R.string.order_street), addr.street)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (addr.building.isNotBlank()) {
                            InfoRow(stringResource(R.string.order_building), addr.building)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (addr.floor.isNotBlank()) {
                            InfoRow(stringResource(R.string.order_floor), addr.floor)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (addr.apartment.isNotBlank()) {
                            InfoRow(stringResource(R.string.order_apartment), addr.apartment)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.lat_lon, String.format("%.6f", displayOrder.deliveryAddress.latitude), String.format("%.6f", displayOrder.deliveryAddress.longitude)),
                            fontSize = 10.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(stringResource(R.string.order_contact_info), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        InfoRow(stringResource(R.string.order_name), displayOrder.customerName)
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoRow(stringResource(R.string.order_phone), displayOrder.phone)
                        if (displayOrder.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            InfoRow(stringResource(R.string.order_notes), displayOrder.notes)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoRow(stringResource(R.string.order_payment), stringResource(R.string.order_cash_on_delivery))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showReviewSheet && reviewingItem != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showReviewSheet = false
                reviewingItem = null
            },
            containerColor = cardColor,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(R.string.review_rate_item),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    reviewingItem!!.name,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    stringResource(R.string.review_tap_to_rate),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { i ->
                        val starValue = i + 1
                        val isSelected = starValue <= selectedRating
                        Text(
                            if (isSelected) "\u2605" else "\u2606",
                            fontSize = 40.sp,
                            color = if (isSelected) if (isDark) GoldLight else Gold else colorScheme.outlineVariant,
                            modifier = Modifier
                                .clickable { selectedRating = starValue }
                                .padding(4.dp)
                        )
                    }
                }

                if (selectedRating > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        when (selectedRating) {
                            1 -> stringResource(R.string.review_poor)
                            2 -> stringResource(R.string.review_fair)
                            3 -> stringResource(R.string.review_good)
                            4 -> stringResource(R.string.review_very_good)
                            5 -> stringResource(R.string.review_excellent)
                            else -> ""
                        },
                        fontSize = 13.sp,
                        color = colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = {
                        if (it.length <= 500) reviewText = it
                    },
                    label = { Text(stringResource(R.string.review_your_review)) },
                    placeholder = { Text(stringResource(R.string.review_placeholder)) },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        cursorColor = colorScheme.primary
                    ),
                    enabled = !isSubmittingReview
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    stringResource(R.string.review_char_count, reviewText.length),
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedRating == 0 && reviewText.isBlank()) {
                    Text(stringResource(R.string.review_error_both), fontSize = 12.sp, color = Error)
                } else if (selectedRating == 0) {
                    Text(stringResource(R.string.review_error_rating), fontSize = 12.sp, color = Error)
                } else if (reviewText.isBlank()) {
                    Text(stringResource(R.string.review_error_comment), fontSize = 12.sp, color = Error)
                }

                Spacer(modifier = Modifier.height(8.dp))

                val guestUserLabel = stringResource(R.string.guest_user)

                Button(
                    onClick = {
                        if (selectedRating > 0 && reviewText.isNotBlank() && reviewingItem != null) {
                            val user = SessionManager.currentUser
                            val userName = user?.let { "${it.firstName} ${it.lastName}" } ?: guestUserLabel
                            val userId = user?.id ?: "guest"

                            viewModel.submitReview(
                                productId = reviewingItem!!.productId,
                                rating = selectedRating,
                                comment = reviewText.trim(),
                                userId = userId,
                                userName = userName
                            )
                            showReviewSheet = false
                            reviewingItem = null
                            selectedRating = 0
                            reviewText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = selectedRating > 0 && reviewText.isNotBlank() && !isSubmittingReview
                ) {
                    if (isSubmittingReview) {
                        CircularProgressIndicator(
                            color = colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(stringResource(R.string.review_submit), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun OrderDetailItemCard(
    item: OrderItem,
    showRateButton: Boolean = false,
    existingReview: Review? = null,
    onRateClick: () -> Unit = {}
) {
    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

            if (existingReview != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = if (isDark) DividerDark else DividerLight)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        repeat(5) { i ->
                            Text(
                                if (i < existingReview.rating.toInt()) "\u2605" else "\u2606",
                                fontSize = 14.sp,
                                color = if (i < existingReview.rating.toInt()) if (isDark) GoldLight else Gold else colorScheme.outlineVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        existingReview.comment,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(R.string.review_reviewed),
                        fontSize = 10.sp,
                        color = Success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (showRateButton) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRateClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) GoldLight else Gold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) GoldLight else Gold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = if (isDark) GoldLight else Gold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.review_rate_button), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
    }
}
