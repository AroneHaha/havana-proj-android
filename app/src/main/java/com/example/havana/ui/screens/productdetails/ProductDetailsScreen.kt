package com.example.havana.ui.screens.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.havana.R
import com.example.havana.data.model.Product
import com.example.havana.data.model.ReviewState
import com.example.havana.ui.theme.*
import com.example.havana.data.model.Review
import com.example.havana.data.CategoryHelper

private fun safeStr(value: String?): String {
    return if (value.isNullOrBlank()) "" else value
}

private fun fmtPrice(amount: Double): String {
    return "KWD ${String.format("%.3f", amount)}"
}

private fun fmtPriceShort(amount: Double): String {
    val f = when {
        amount >= 100.0 -> String.format("%.0f", amount)
        amount >= 10.0 -> String.format("%.1f", amount)
        else -> String.format("%.3f", amount)
    }
    return "KWD $f"
}

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
    val errorMessage by viewModel.errorState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = stringResource(R.string.nav_cart),
                            tint = colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        bottomBar = {
            product?.let { p ->
                Surface(
                    shadowElevation = 16.dp,
                    color = cardColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.product_quantity),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
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
                                        contentColor = colorScheme.primary
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "$quantity",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onBackground
                                )
                                OutlinedButton(
                                    onClick = { viewModel.increaseQuantity() },
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colorScheme.primary
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.addToCart() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (addedToCart) Success else colorScheme.primary,
                                    containerColor = if (addedToCart) Success.copy(alpha = 0.08f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (addedToCart) Success else colorScheme.primary
                                )
                            ) {
                                if (addedToCart) {
                                    Text(stringResource(R.string.product_added), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(stringResource(R.string.product_add_to_cart), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.addToCart()
                                    onCheckoutClick()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    "${stringResource(R.string.product_checkout)}  \u2022  ${fmtPriceShort(p.price * quantity)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        if (isLoading && product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else if (product == null && errorMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Something went wrong",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        errorMessage,
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onBackClick) {
                        Text(stringResource(R.string.back), fontSize = 14.sp)
                    }
                }
            }
        } else if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            val p = product!!

            val nameText = safeStr(p.name).ifEmpty { "Product" }
            val descText = safeStr(p.description).ifEmpty { "No description available." }
            val catText = safeStr(p.categoryName).ifEmpty { "Flowers" }
            val imagesList: List<String> = (p.images ?: emptyList()).map { safeStr(it) }.filter { it.isNotEmpty() }
            val hasImages = imagesList.isNotEmpty()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    val imageCount = if (hasImages) imagesList.size else 3
                    val pagerState = rememberPagerState(pageCount = { imageCount })

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasImages && page < imagesList.size) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                            .data(imagesList[page])
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = nameText,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    val bgColors = listOf(
                                        colorScheme.primary.copy(alpha = 0.06f),
                                        colorScheme.secondary.copy(alpha = 0.08f),
                                        colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(bgColors[page % bgColors.size])
                                    )
                                    val pageEmoji = CategoryHelper.getEmoji(p.categoryName)
                                    val labels = listOf(
                                        stringResource(R.string.product_front_view),
                                        stringResource(R.string.product_detail_view),
                                        stringResource(R.string.product_arrangement)
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            pageEmoji,
                                            fontSize = 72.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            labels[page % labels.size],
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(imageCount) { i ->
                                val isSelected = pagerState.currentPage == i
                                Surface(
                                    modifier = Modifier
                                        .width(if (isSelected) 20.dp else 8.dp)
                                        .height(8.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.3f)
                                ) {}
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                catText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            nameText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                fmtPrice(p.price),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (isDark) GoldLight else Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    " ${p.rating}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onBackground
                                )
                                Text(
                                    " " + stringResource(R.string.product_reviews_count, p.reviewCount),
                                    fontSize = 13.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            if (p.inStock) stringResource(R.string.product_in_stock) else stringResource(R.string.product_out_of_stock),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (p.inStock) Success else Error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            stringResource(R.string.product_description),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            descText,
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = colorScheme.outline
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.product_reviews_ratings),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${p.rating}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { i ->
                                        val starValue = i + 1
                                        val filled = starValue <= p.rating.toInt()
                                        Text(
                                            if (filled) "\u2605" else "\u2606",
                                            fontSize = 12.sp,
                                            color = if (filled) if (isDark) GoldLight else Gold else colorScheme.outlineVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                when (reviewState) {
                    is ReviewState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colorScheme.primary, modifier = Modifier.size(24.dp))
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

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (ThemeManager.isDarkMode) CardDark else CardLight
    val isDark = ThemeManager.isDarkMode

    val rName = safeStr(review.userName).ifEmpty { "Anonymous" }
    val rComment = safeStr(review.comment).ifEmpty { "No comment." }
    val rDate = safeStr(review.date)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            rName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        rName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackground
                    )
                    Text(
                        rDate,
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    repeat(5) { i ->
                        Text(
                            if (i < review.rating.toInt()) "\u2605" else "\u2606",
                            fontSize = 14.sp,
                            color = if (i < review.rating.toInt()) if (isDark) GoldLight else Gold else colorScheme.outlineVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                rComment,
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}