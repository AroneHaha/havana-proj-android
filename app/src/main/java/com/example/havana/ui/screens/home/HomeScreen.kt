package com.example.havana.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havana.data.model.Category
import com.example.havana.data.model.CategoryState
import com.example.havana.data.model.Product
import com.example.havana.data.model.ProductListState
import com.example.havana.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit = {},
    onCartClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel()
    val productState by viewModel.productState.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var searchInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight
    val navBarColor = if (isDark) NavBarDark else NavBarLight
    val dividerColor = if (isDark) DividerDark else DividerLight
    val switchUncheckedTrack = if (isDark) SwitchUncheckedTrackDark else SwitchUncheckedTrackLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "HAVANA",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            letterSpacing = 3.sp
                        )
                        Text(
                            "Luxury Flowers & Gifts",
                            fontSize = 10.sp,
                            color = colorScheme.secondary,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "Cart",
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
            NavigationBar(
                containerColor = navBarColor,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {
                        coroutineScope.launch { listState.animateScrollToItem(0) }
                        viewModel.loadProducts()
                    },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorScheme.primary,
                        selectedTextColor = colorScheme.primary,
                        indicatorColor = colorScheme.primary.copy(alpha = 0.1f)
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
                    selected = false,
                    onClick = onOrdersClick,
                    icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = "Orders") },
                    label = { Text("Orders", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = colorScheme.onSurfaceVariant,
                        unselectedTextColor = colorScheme.onSurfaceVariant
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it; viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search flowers, gifts...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        cursorColor = colorScheme.primary,
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor
                    )
                )
            }

            item {
                val categories = when (categoryState) {
                    is CategoryState.Success -> (categoryState as CategoryState.Success).categories
                    else -> emptyList()
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category.name == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category.name) },
                            label = { Text("${category.emoji} ${category.name}", fontSize = 13.sp) },
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
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // ===== SHOP BY OCCASION =====
            item {
                Text(
                    "Shop by Occasion",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(occasionList) { occasion ->
                        OccasionCard(
                            occasion = occasion,
                            onClick = { }
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            val featuredProducts = viewModel.getFeaturedProducts()
            if (featuredProducts.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "All") {
                item {
                    Text(
                        "Featured",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featuredProducts) { product ->
                            FeaturedProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            val topSelling = viewModel.getTopSellingProducts()
            if (topSelling.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "All") {
                item {
                    Text(
                        "Top Selling",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(topSelling) { product ->
                            TopSellingCard(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            item {
                val sectionTitle = when {
                    searchQuery.isNotBlank() -> "Search Results"
                    selectedCategory != "All" -> selectedCategory
                    else -> "All Products"
                }
                Text(
                    sectionTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when (productState) {
                is ProductListState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colorScheme.primary)
                        }
                    }
                }
                is ProductListState.Success -> {
                    val products = (productState as ProductListState.Success).products
                    if (products.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No products found",
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        val rows = products.chunked(2)
                        items(rows.size) { rowIndex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rows[rowIndex].forEach { product ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProductCard(
                                            product = product,
                                            onClick = { onProductClick(product.id) }
                                        )
                                    }
                                }
                                if (rows[rowIndex].size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                is ProductListState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    (productState as ProductListState.Error).message,
                                    color = Error
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { viewModel.loadProducts() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                else -> {}
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun FeaturedProductCard(
    product: Product,
    onClick: () -> Unit
) {
    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.categoryEmoji(), fontSize = 40.sp)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = colorScheme.secondary
                ) {
                    Text(
                        "Featured",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "KD ${String.format("%.3f", product.price)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\u2605", fontSize = 12.sp, color = colorScheme.secondary)
                    Text(
                        " ${product.rating} (${product.reviewCount})",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TopSellingCard(
    product: Product,
    onClick: () -> Unit
) {
    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight

    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(colorScheme.secondary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.categoryEmoji(), fontSize = 32.sp)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = colorScheme.primary
                ) {
                    Text(
                        "Top",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "KD ${String.format("%.3f", product.price)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(product.categoryEmoji(), fontSize = 36.sp)
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "KD ${String.format("%.3f", product.price)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\u2605", fontSize = 10.sp, color = colorScheme.secondary)
                        Text(
                            " ${product.rating}",
                            fontSize = 10.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (!product.inStock) {
                    Text(
                        "Out of Stock",
                        fontSize = 11.sp,
                        color = Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun Product.categoryEmoji(): String {
    return when (category.lowercase()) {
        "roses" -> "\uD83C\uDF39"
        "bouquets" -> "\uD83D\uDC90"
        "arrangements" -> "\uD83C\uDF3A"
        "gifts" -> "\uD83C\uDF81"
        "plants" -> "\uD83E\uDEB4"
        else -> "\uD83C\uDF38"
    }
}

data class OccasionItem(val key: String, val label: String, val emoji: String)

val occasionList = listOf(
    OccasionItem("birthday", "Birthday", "\uD83C\uDF82"),
    OccasionItem("weddings", "Weddings", "\uD83D\uDC8D"),
    OccasionItem("anniversary", "Anniversary", "\u2764\uFE0F"),
    OccasionItem("graduation", "Graduation", "\uD83C\uDF93"),
    OccasionItem("mothersDay", "Mother's Day", "\uD83C\uDF3A"),
    OccasionItem("loveRomance", "Love", "\uD83D\uDC95"),
    OccasionItem("eid", "Eid", "\uD83C\uDF38"),
    OccasionItem("sympathy", "Sympathy", "\uD83D\uDE22")
)

@Composable
fun OccasionCard(
    occasion: OccasionItem,
    onClick: () -> Unit
) {
    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme
    val cardColor = if (isDark) CardDark else CardLight

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .width(90.dp)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                occasion.emoji,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                occasion.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}