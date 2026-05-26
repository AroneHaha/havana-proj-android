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
import com.example.havana.data.model.Review
import com.example.havana.data.model.statusColor
import com.example.havana.data.model.statusLabel
import com.example.havana.data.model.statusEmoji
import com.example.havana.data.session.SessionManager
import com.example.havana.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    order: Order?,
    onBackClick: () -> Unit = {},
    onConfirmDelivery: (String) -> Unit = {},
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

    var currentStatus by remember { mutableStateOf(order.status) }
    val displayOrder = remember(order, currentStatus) {
        order.copy(status = currentStatus)
    }

    var showReviewSheet by remember { mutableStateOf(false) }
    var reviewingItem by remember { mutableStateOf<OrderItem?>(null) }
    var selectedRating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var isSubmittingReview by remember { mutableStateOf(false) }

    val itemReviews = remember { mutableStateMapOf<String, Review>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        displayOrder.orderNumber,
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
                        Text(displayOrder.statusEmoji(), fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = displayOrder.statusColor().copy(alpha = 0.12f)
                        ) {
                            Text(
                                displayOrder.statusLabel(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = displayOrder.statusColor(),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Ordered on ${displayOrder.createdAt}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

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
                                        isCancelled -> Color(0xFFE5E5E5)
                                        isCurrent -> displayOrder.statusColor()
                                        isCompleted -> Success
                                        else -> Color(0xFFE5E5E5)
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

            if (displayOrder.status == "out_for_delivery") {
                item {
                    Button(
                        onClick = {
                            currentStatus = "delivered"
                            onConfirmDelivery(order.id)
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
                            "Confirm Delivery",
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
                    Text("Items", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (displayOrder.status == "delivered") {
                        Text(
                            "Tap an item to rate",
                            fontSize = 12.sp,
                            color = Gold,
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 13.sp, color = TextSecondary)
                            Text("KD ${String.format("%.3f", displayOrder.subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", fontSize = 13.sp, color = TextSecondary)
                            Text("KD ${String.format("%.3f", displayOrder.deliveryFee)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFE5E5E5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("KD ${String.format("%.3f", displayOrder.total)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Maroon)
                        }
                    }
                }
            }

            item {
                Text("Delivery Address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                tint = Maroon,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    displayOrder.deliveryAddress.fullAddress,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
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
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (addr.block.isNotBlank()) {
                            InfoRow("Block", addr.block)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (addr.street.isNotBlank()) {
                            InfoRow("Street", addr.street)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (addr.building.isNotBlank()) {
                            InfoRow("Building", addr.building)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (addr.floor.isNotBlank()) {
                            InfoRow("Floor", addr.floor)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (addr.apartment.isNotBlank()) {
                            InfoRow("Apartment", addr.apartment)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Lat: ${String.format("%.6f", displayOrder.deliveryAddress.latitude)}, Lon: ${String.format("%.6f", displayOrder.deliveryAddress.longitude)}",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

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
                        InfoRow("Name", displayOrder.customerName)
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoRow("Phone", displayOrder.phone)
                        if (displayOrder.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            InfoRow("Notes", displayOrder.notes)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoRow("Payment", "Cash on Delivery")
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
            containerColor = Color.White,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "Rate This Item",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    reviewingItem!!.name,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Tap to rate:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
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
                            color = if (isSelected) Gold else Color(0xFFD4C5B9),
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
                            1 -> "Poor"
                            2 -> "Fair"
                            3 -> "Good"
                            4 -> "Very Good"
                            5 -> "Excellent"
                            else -> ""
                        },
                        fontSize = 13.sp,
                        color = Gold,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = {
                        if (it.length <= 500) reviewText = it
                    },
                    label = { Text("Your Review") },
                    placeholder = { Text("Tell us what you think about this product...") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Maroon,
                        focusedLabelColor = Maroon,
                        cursorColor = Maroon
                    ),
                    enabled = !isSubmittingReview
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "${reviewText.length} / 500",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedRating == 0 && reviewText.isBlank()) {
                    Text("Please select a rating and write a review", fontSize = 12.sp, color = Error)
                } else if (selectedRating == 0) {
                    Text("Please select a rating", fontSize = 12.sp, color = Error)
                } else if (reviewText.isBlank()) {
                    Text("Please write a review comment", fontSize = 12.sp, color = Error)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (selectedRating > 0 && reviewText.isNotBlank() && reviewingItem != null) {
                            isSubmittingReview = true
                            val user = SessionManager.currentUser
                            val userName = user?.let { "${it.firstName} ${it.lastName}" } ?: "Guest User"
                            val userId = user?.id ?: "guest"

                            val newReview = Review(
                                id = "r-${System.currentTimeMillis()}",
                                userId = userId,
                                userName = userName,
                                rating = selectedRating.toFloat(),
                                comment = reviewText.trim(),
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            )
                            itemReviews[reviewingItem!!.productId] = newReview

                            isSubmittingReview = false
                            showReviewSheet = false
                            reviewingItem = null
                            selectedRating = 0
                            reviewText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = selectedRating > 0 && reviewText.isNotBlank() && !isSubmittingReview
                ) {
                    if (isSubmittingReview) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text("Submit Review", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        .background(CreamBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.categoryEmoji(), fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Qty: ${item.quantity} x KD ${String.format("%.3f", item.price)}", fontSize = 12.sp, color = TextSecondary)
                }
                Text("KD ${String.format("%.3f", item.price * item.quantity)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Maroon)
            }

            if (existingReview != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
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
                                color = if (i < existingReview.rating.toInt()) Gold else Color(0xFFD4C5B9)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        existingReview.comment,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Reviewed",
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rate This Item", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
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
        "roses" -> "\uD83C\uDF39"
        "bouquets" -> "\uD83D\uDC90"
        "arrangements" -> "\uD83C\uDF3A"
        "gifts" -> "\uD83C\uDF81"
        "plants" -> "\uD83E\uDEB4"
        else -> "\uD83C\uDF38"
    }
}