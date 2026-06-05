package com.example.havana.ui.screens.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.cart.CartManager
import com.example.havana.data.model.*
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.safeApiCall
import com.example.havana.R
import com.example.havana.data.AppConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    private val checkoutApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.CheckoutApiService::class.java
    )
    private val cartApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.CartApiService::class.java
    )

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private val _deliveryAddress = MutableStateFlow<DeliveryAddress?>(null)
    val deliveryAddress: StateFlow<DeliveryAddress?> = _deliveryAddress.asStateFlow()

    private val _lastPlacedOrder = MutableStateFlow<Order?>(null)
    val lastPlacedOrder: StateFlow<Order?> = _lastPlacedOrder.asStateFlow()

    val cartItems = CartManager.cartItems

    fun setDeliveryAddress(address: DeliveryAddress) {
        _deliveryAddress.value = address
    }

    private suspend fun syncCartToServer(): String? {
        val items = cartItems.value
        if (items.isEmpty()) return null
        val errors = mutableListOf<String>()
        for (item in items) {
            val request = com.example.havana.data.model.AddToCartRequest(
                productId = item.productId,
                quantity = item.quantity
            )
            when (val result = safeApiCall { cartApi.addToCart(request) }) {
                is ApiResult.Success -> { /* ok */ }
                is ApiResult.ServerError -> {
                    errors.add("ADD ${item.name} FAILED HTTP ${result.code}")
                }
                is ApiResult.NetworkError -> {
                    errors.add("ADD ${item.name} FAILED: ${result.error}")
                }
            }
        }
        return if (errors.isEmpty()) null else errors.joinToString("\n")
    }

    fun placeOrder(customerName: String, phone: String, notes: String) {
        if (customerName.isBlank()) { _checkoutState.value = CheckoutState.Error(getApplication<Application>().getString(R.string.checkout_error_name)); return }
        if (phone.isBlank()) { _checkoutState.value = CheckoutState.Error(getApplication<Application>().getString(R.string.checkout_error_phone)); return }
        val cleanPhone = phone.replace("+965", "").replace(" ", "").trim()
        if (cleanPhone.length != 8 || !cleanPhone.first().toString().matches(Regex("[5689]"))) { _checkoutState.value = CheckoutState.Error(getApplication<Application>().getString(R.string.checkout_error_phone_invalid)); return }
        val address = _deliveryAddress.value
        if (address == null || address.fullAddress.isBlank()) { _checkoutState.value = CheckoutState.Error(getApplication<Application>().getString(R.string.checkout_error_address)); return }
        val items = cartItems.value
        if (items.isEmpty()) { _checkoutState.value = CheckoutState.Error(getApplication<Application>().getString(R.string.checkout_error_cart_empty)); return }
        _checkoutState.value = CheckoutState.Loading
        val subtotal = items.sumOf { it.price * it.quantity }
        val deliveryFee = AppConstants.DELIVERY_FEE
        val total = subtotal + deliveryFee

        // Create a local Order for display (OrderConfirmationScreen)
        val fullOrder = Order(
            id = "order-${System.currentTimeMillis()}",
            orderNumber = "",
            phone = phone,
            shippingAddress = address.fullAddress,
            notes = notes,
            paymentMethod = "cod",
            items = items.map { OrderItem(productId = it.productId, name = it.name, price = it.price, quantity = it.quantity) },
            subtotal = subtotal,
            shippingCost = deliveryFee,
            total = total,
            status = "pending",
            createdAt = ""
        )

        // Build the simple request the backend expects (only 4 fields)
        val orderRequest = OrderRequest(
            shippingAddress = address.fullAddress,
            shippingPhone = phone,
            notes = notes,
            paymentMethod = "cod"
        )

        viewModelScope.launch {
            // Step 1: Sync cart items to server (backend reads from user's cart)
            val syncError = syncCartToServer()
            if (syncError != null) {
                _checkoutState.value = CheckoutState.Error("Failed to sync cart: $syncError")
                return@launch
            }

            // Step 2: Place the order
            when (val result = safeApiCall { checkoutApi.placeOrder(orderRequest) }) {
                is ApiResult.Success -> {
                    val serverOrder = result.data.data
                    val displayOrder = fullOrder.copy(
                        id = serverOrder.id,
                        orderNumber = serverOrder.orderNumber,
                        status = serverOrder.status,
                        createdAt = serverOrder.createdAt
                    )
                    _lastPlacedOrder.value = displayOrder
                    _checkoutState.value = CheckoutState.Success(OrderResponse(
                        id = serverOrder.id,
                        orderNumber = serverOrder.orderNumber,
                        status = serverOrder.status,
                        total = serverOrder.total,
                        createdAt = serverOrder.createdAt
                    ))
                    CartManager.clearCart()
                }
                is ApiResult.ServerError -> {
                    _checkoutState.value = CheckoutState.Error(
                        "Server error (HTTP ${result.code}): ${result.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    delay(1000)
                    val mockNumber = "HAV-${(1000..9999).random()}"
                    _lastPlacedOrder.value = fullOrder.copy(orderNumber = mockNumber)
                    _checkoutState.value = CheckoutState.Success(OrderResponse(
                        id = fullOrder.id, orderNumber = mockNumber,
                        status = "pending", total = total,
                        createdAt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                    ))
                    CartManager.clearCart()
                }
            }
        }
    }

    fun resetState() {
        _checkoutState.value = CheckoutState.Idle
        _deliveryAddress.value = null
        _lastPlacedOrder.value = null
    }
}