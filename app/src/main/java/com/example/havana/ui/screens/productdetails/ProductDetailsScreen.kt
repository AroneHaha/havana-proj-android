package com.example.havana.ui.screens.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havana.data.model.Product
import com.example.havana.data.model.Review
import com.example.havana.data.model.ReviewState
import com.example.havana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ProductDetailsScreen(
    productId: String,
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    viewModel: ProductDetailsViewModel = viewModel()
) {
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val product by viewModel.productState.collectAsState()
    val reviewState by viewModel.reviewState.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val addedToCart by viewModel.addedToCart.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Maroon
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Maroon
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBg
                )
            )
        },
        bottomBar = {
            // Add to Cart + Checkout Bar
            product?.let { p ->
                Surface(
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Quantity selector row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Quantity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.decreaseQuantity() },
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Maroon
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "$quantity",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                OutlinedButton(
                                    onClick = { viewModel.increaseQuantity() },
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Maroon
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Add to Cart + Checkout buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Add to Cart
                            OutlinedButton(
                                onClick = { viewModel.addToCart() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (addedToCart) Success else Maroon,
                                    containerColor = if (addedToCart) Success.copy(alpha = 0.08f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (addedToCart) Success else Maroon
                                )
                            ) {
                                if (addedToCart) {
                                    Text("Added ✓", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text("Add to Cart", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Checkout
                            Button(
                                onClick = {
                                    viewModel.addToCart()
                                    onCheckoutClick()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Maroon
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    "Checkout  •  ${formatKdPrice(p.price * quantity)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = CreamBg
    ) { paddingValues ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Maroon)
            }
        } else {
            val p = product!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ===== PRODUCT IMAGE =====
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(Maroon.copy(alpha = 0.06f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            p.categoryEmoji(),
                            fontSize = 80.sp
                        )
                    }
                }

                // ===== PRODUCT INFO =====
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Category
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Maroon.copy(alpha = 0.1f)
                        ) {
                            Text(
                                p.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Maroon,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Name
                        Text(
                            p.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Price + Rating row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "KD ${String.format("%.3f", p.price)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Maroon
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    " ${p.rating}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    " (${p.reviewCount} reviews)",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Stock
                        Text(
                            if (p.inStock) "In Stock" else "Out of Stock",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (p.inStock) Success else Error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description
                        Text(
                            "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            p.description,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    }
                }

                // ===== DIVIDER =====
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE5E5E5)
                    )
                }

                // ===== REVIEWS SECTION =====
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Reviews & Ratings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        // Rating summary
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${p.rating}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                repeat(5) { i ->
                                    val starValue = i + 1
                                    val filled = starValue <= p.rating.toInt()
                                    Text(
                                        if (filled) "★" else "☆",
                                        fontSize = 12.sp,
                                        color = if (filled) Gold else Color(0xFFD4C5B9)
                                    )
                                }
                            }
                        }
                    }
                }

                // ===== REVIEW LIST =====
                when (reviewState) {
                    is ReviewState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Maroon, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    is ReviewState.Success -> {
                        val reviews = (reviewState as ReviewState.Success).reviews
                        items(reviews) { review ->
                            ReviewCard(review)
                        }
                    }
                    is ReviewState.Error -> {
                        item {
                            Text(
                                (reviewState as ReviewState.Error).message,
                                color = Error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {}
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

private fun formatKdPrice(amount: Double): String {
    return when {
        amount >= 100.0 -> "KD ${String.format("%.0f", amount)}"       // KD 210
        amount >= 10.0 -> "KD ${String.format("%.1f", amount)}"        // KD 27.5
        else -> "KD ${String.format("%.3f", amount)}"                   // KD 7.500
    }
}
// ===== REVIEW CARD =====
@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top row: Avatar + Name + Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar circle
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Maroon.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            review.userName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Maroon
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        review.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        review.date,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Star rating
                Row {
                    repeat(5) { i ->
                        Text(
                            if (i < review.rating.toInt()) "★" else "☆",
                            fontSize = 14.sp,
                            color = if (i < review.rating.toInt()) Gold else Color(0xFFD4C5B9)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment
            Text(
                review.comment,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

// Helper extension (same as HomeScreen)
private fun Product.categoryEmoji(): String {
    return when (category.lowercase()) {
        "roses" -> "🌹"
        "bouquets" -> "💐"
        "arrangements" -> "🌺"
        "gifts" -> "🎁"
        "plants" -> "🪴"
        else -> "🌸"
    }
}