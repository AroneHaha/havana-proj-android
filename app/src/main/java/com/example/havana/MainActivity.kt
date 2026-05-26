package com.example.havana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.havana.data.model.DeliveryAddress
import com.example.havana.data.model.Order
import com.example.havana.data.model.OrderItem
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
import com.example.havana.ui.screens.signup.SignupScreen
import com.example.havana.ui.theme.HavanaTheme
import com.example.havana.data.session.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionManager.initialize(applicationContext)
        setContent { HavanaTheme { HavanaApp() } }
    }
}

var mockOrdersList = mutableListOf(
    Order(id = "ord-1", orderNumber = "HAV-4821", customerName = "Fatima Al-Sabah", phone = "+965 5123 4567", deliveryAddress = DeliveryAddress(fullAddress = "Salmiya, Salem Al Mubarak St, Hawalli Governorate, Kuwait", area = "Salmiya", block = "12", street = "Salem Al Mubarak St", building = "8", floor = "3", apartment = "Apt 5", latitude = 29.3375, longitude = 48.0833), notes = "Ring doorbell please", paymentMethod = "cod", items = listOf(OrderItem("1", "Royal Red Roses", 27.500, 2, "Roses"), OrderItem("5", "Golden Gift Box", 37.000, 1, "Gifts")), subtotal = 92.000, deliveryFee = 1.500, total = 93.500, status = "delivered", createdAt = "2026-05-20 14:30"),
    Order(id = "ord-2", orderNumber = "HAV-5103", customerName = "Ahmed Hassan", phone = "+965 6234 5678", deliveryAddress = DeliveryAddress(fullAddress = "Jabriya, Block 10, Street 5, Hawalli Governorate, Kuwait", area = "Jabriya", block = "10", street = "Street 5", building = "2", floor = "1", apartment = "Office 3", latitude = 29.3267, longitude = 48.0044), notes = "", paymentMethod = "cod", items = listOf(OrderItem("9", "Peony Premium Box", 42.000, 1, "Bouquets")), subtotal = 42.000, deliveryFee = 1.500, total = 43.500, status = "out_for_delivery", createdAt = "2026-05-22 10:15"),
    Order(id = "ord-3", orderNumber = "HAV-5210", customerName = "Sara Al-Ali", phone = "+965 9345 6789", deliveryAddress = DeliveryAddress(fullAddress = "Kuwait City, Al Shuhada St, Al Asimah, Kuwait", area = "Kuwait City", block = "1", street = "Al Shuhada St", building = "5", floor = "2", latitude = 29.3759, longitude = 47.9774), notes = "Leave at the gate", paymentMethod = "cod", items = listOf(OrderItem("3", "Pink Blush Arrangement", 23.000, 1, "Arrangements"), OrderItem("8", "Mini Rose Plant", 14.000, 2, "Plants")), subtotal = 51.000, deliveryFee = 1.500, total = 52.500, status = "preparing", createdAt = "2026-05-23 09:00"),
    Order(id = "ord-4", orderNumber = "HAV-5345", customerName = "Mohammed Jassim", phone = "+965 5456 7890", deliveryAddress = DeliveryAddress(fullAddress = "Hawalli, Ibn Khaldun St, Hawalli Governorate, Kuwait", area = "Hawalli", block = "3", street = "Ibn Khaldun St", building = "11", floor = "4", latitude = 29.2922, longitude = 48.0089), notes = "", paymentMethod = "cod", items = listOf(OrderItem("11", "Romance Bundle", 30.000, 1, "Gifts")), subtotal = 30.000, deliveryFee = 1.500, total = 31.500, status = "confirmed", createdAt = "2026-05-23 11:30"),
    Order(id = "ord-5", orderNumber = "HAV-5402", customerName = "Noor Al-Din", phone = "+965 8567 8901", deliveryAddress = DeliveryAddress(fullAddress = "Mishref, Block 4, Street 20, Hawalli Governorate, Kuwait", area = "Mishref", block = "4", street = "Street 20", building = "7", floor = "1", latitude = 29.2878, longitude = 48.0653), notes = "Call before delivery", paymentMethod = "cod", items = listOf(OrderItem("2", "Sunset Bouquet", 20.000, 1, "Bouquets"), OrderItem("12", "Daisy Delight", 10.000, 3, "Bouquets")), subtotal = 50.000, deliveryFee = 1.500, total = 51.500, status = "pending", createdAt = "2026-05-23 12:45"),
    Order(id = "ord-6", orderNumber = "HAV-5520", customerName = "Layla Abbas", phone = "+965 6678 9012", deliveryAddress = DeliveryAddress(fullAddress = "Salwa, Block 7, Street 1, Hawalli Governorate, Kuwait", area = "Salwa", block = "7", street = "Street 1", building = "14", floor = "2", latitude = 29.3019, longitude = 48.1178), notes = "", paymentMethod = "cod", items = listOf(OrderItem("6", "White Elegance", 29.000, 1, "Arrangements")), subtotal = 29.000, deliveryFee = 1.500, total = 30.500, status = "cancelled", createdAt = "2026-05-21 16:00"),
)

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
        "orderConfirmation" -> OrderConfirmationScreen(order = lastPlacedOrder, onViewOrders = { if (lastPlacedOrder != null && mockOrdersList.none { it.id == lastPlacedOrder!!.id }) { mockOrdersList.add(0, lastPlacedOrder!!) }; currentScreen = "orders" }, onContinueShopping = { if (lastPlacedOrder != null && mockOrdersList.none { it.id == lastPlacedOrder!!.id }) { mockOrdersList.add(0, lastPlacedOrder!!) }; currentScreen = "home" })
        "mapPicker" -> MapPickerScreen(onAddressConfirmed = { address -> selectedAddress = address; currentScreen = "checkout" }, onBackClick = { currentScreen = "checkout" })
        "orders" -> OrdersScreen(onOrderClick = { orderId -> selectedOrderId = orderId; currentScreen = "orderDetails" }, onHomeClick = { currentScreen = "home" }, onCartClick = { currentScreen = "cart" }, onProfileClick = { currentScreen = "profile" })
        "orderDetails" -> OrderDetailsScreen(order = mockOrdersList.find { it.id == selectedOrderId }, onBackClick = { currentScreen = "orders" }, onConfirmDelivery = { orderId -> val index = mockOrdersList.indexOfFirst { it.id == orderId }; if (index >= 0) { mockOrdersList[index] = mockOrdersList[index].copy(status = "delivered") } })
        "profile" -> ProfileScreen(onBackClick = { currentScreen = "home" }, onHomeClick = { currentScreen = "home" }, onCartClick = { currentScreen = "cart" }, onOrdersClick = { currentScreen = "orders" }, onLogoutClick = { currentScreen = "login" })
    }
}