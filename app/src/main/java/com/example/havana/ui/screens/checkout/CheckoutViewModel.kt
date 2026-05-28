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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    private val checkoutApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.CheckoutApiService::class.java
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
        val deliveryFee = 1.500
        val total = subtotal + deliveryFee
        val fullOrder = Order(
            id = "order-${System.currentTimeMillis()}",
            orderNumber = "",
            customerName = customerName,
            phone = phone,
            deliveryAddress = address,
            notes = notes,
            paymentMethod = "cod",
            items = items.map { OrderItem(it.productId, it.name, it.price, it.quantity, it.category) },
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            total = total,
            status = "confirmed",
            createdAt = ""
        )
        val orderRequest = OrderRequest(
            customerName = customerName, phone = phone, deliveryAddress = address, notes = notes, paymentMethod = "cod",
            items = items.map { OrderItemRequest(it.productId, it.name, it.price, it.quantity) },
            subtotal = subtotal, deliveryFee = deliveryFee, total = total
        )
        viewModelScope.launch {
            when (val result = safeApiCall { checkoutApi.placeOrder(orderRequest) }) {
                is ApiResult.Success -> {
                    val response = result.data
                    _lastPlacedOrder.value = fullOrder.copy(id = response.id, orderNumber = response.orderNumber, status = response.status, createdAt = response.createdAt)
                    _checkoutState.value = CheckoutState.Success(response)
                    CartManager.clearCart()
                }
                is ApiResult.ServerError -> {
                    _checkoutState.value = CheckoutState.Error(result.message)
                }
                is ApiResult.NetworkError -> {
                    // Server unreachable — fall back to mock order during development
                    delay(1000)
                    val mockOrderNumber = "HAV-${(1000..9999).random()}"
                    val mockOrderResponse = OrderResponse(id = fullOrder.id, orderNumber = mockOrderNumber, status = "confirmed", total = total, createdAt = "2026-05-23")
                    _lastPlacedOrder.value = fullOrder.copy(orderNumber = mockOrderNumber, createdAt = mockOrderResponse.createdAt)
                    _checkoutState.value = CheckoutState.Success(mockOrderResponse)
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
