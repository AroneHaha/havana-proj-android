package com.example.havana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.havana.data.cart.CartManager
import com.example.havana.data.model.DeliveryAddress
import com.example.havana.data.model.Order
import com.example.havana.data.repository.OrderRepository
import com.example.havana.ui.screens.cart.CartScreen
import com.example.havana.ui.screens.checkout.CheckoutScreen
import com.example.havana.ui.screens.checkout.MapPickerScreen
import com.example.havana.ui.screens.home.HomeScreen
import com.example.havana.ui.screens.login.LoginScreen
import com.example.havana.ui.screens.orders.OrderConfirmationScreen
import com.example.havana.ui.screens.orders.OrderDetailsScreen
import com.example.havana.ui.screens.orders.OrdersScreen
import com.example.havana.ui.screens.productdetails.ProductDetailsScreen
import com.example.havana.ui.screens.profile.ProfileScreen
import com.example.havana.ui.theme.HavanaTheme
import com.example.havana.data.session.SessionManager

import com.example.havana.ui.screens.signup.SignupScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionManager.initialize(applicationContext)
        CartManager.initialize(applicationContext)
        setContent { HavanaTheme { HavanaApp() } }
    }
}

@Composable
fun HavanaApp() {
    var currentScreen by remember { mutableStateOf(if (SessionManager.isLoggedIn) "home" else "login") }
    var selectedProductId by remember { mutableStateOf<String?>(null) }
    var selectedAddress by remember { mutableStateOf<DeliveryAddress?>(null) }
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    var lastPlacedOrder by remember { mutableStateOf<Order?>(null) }

    when (currentScreen) {
        "login" -> LoginScreen(onLoginSuccess = { currentScreen = "home" }, onNavigateToSignup = { currentScreen = "signup" })
        "signup" -> SignupScreen(onNavigateToLogin = { currentScreen = "login" })
        "home" -> HomeScreen(onProductClick = { productId -> selectedProductId = productId; currentScreen = "productDetails" }, onCartClick = { currentScreen = "cart" }, onOrdersClick = { currentScreen = "orders" }, onProfileClick = { currentScreen = "profile" })
        "productDetails" -> ProductDetailsScreen(productId = selectedProductId ?: "", onBackClick = { currentScreen = "home" }, onCartClick = { currentScreen = "cart" }, onCheckoutClick = { currentScreen = "checkout" })
        "cart" -> CartScreen(onBackClick = { currentScreen = "home" }, onCheckoutClick = { currentScreen = "checkout" }, onHomeClick = { currentScreen = "home" }, onProfileClick = { currentScreen = "profile" })
        "checkout" -> CheckoutScreen(onBackClick = { currentScreen = "cart" }, onOrderSuccess = { orderNumber, order -> lastPlacedOrder = order; currentScreen = "orderConfirmation" }, onPickOnMap = { currentScreen = "mapPicker" }, savedAddress = selectedAddress)
        "orderConfirmation" -> OrderConfirmationScreen(order = lastPlacedOrder, onViewOrders = { if (lastPlacedOrder != null) OrderRepository.addOrder(lastPlacedOrder!!); currentScreen = "orders" }, onContinueShopping = { if (lastPlacedOrder != null) OrderRepository.addOrder(lastPlacedOrder!!); currentScreen = "home" })
        "mapPicker" -> MapPickerScreen(onAddressConfirmed = { address -> selectedAddress = address; currentScreen = "checkout" }, onBackClick = { currentScreen = "checkout" })
        "orders" -> OrdersScreen(onOrderClick = { orderId -> selectedOrderId = orderId; currentScreen = "orderDetails" }, onHomeClick = { currentScreen = "home" }, onCartClick = { currentScreen = "cart" }, onProfileClick = { currentScreen = "profile" })
        "orderDetails" -> OrderDetailsScreen(orderId = selectedOrderId, onBackClick = { currentScreen = "orders" }, onConfirmDelivery = { orderId -> OrderRepository.updateOrderStatus(orderId, "delivered") })
        "profile" -> ProfileScreen(onBackClick = { currentScreen = "home" }, onHomeClick = { currentScreen = "home" }, onCartClick = { currentScreen = "cart" }, onOrdersClick = { currentScreen = "orders" }, onLogoutClick = { currentScreen = "login" })
    }
}