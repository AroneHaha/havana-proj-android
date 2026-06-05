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

        // Build full address string (same as web: customer.address = one string)
        val addressParts = mutableListOf(address.fullAddress)
        if (address.block.isNotBlank()) addressParts.add("Block ${address.block}")
        if (address.street.isNotBlank()) addressParts.add("Street ${address.street}")
        if (address.building.isNotBlank()) addressParts.add("Building ${address.building}")
        if (address.floor.isNotBlank()) addressParts.add("Floor ${address.floor}")
        if (address.apartment.isNotBlank()) addressParts.add("Apt ${address.apartment}")
        val fullAddress = addressParts.joinToString(", ")

        // ── Build request matching web checkout-service.ts CheckoutPayload EXACTLY ──
        // Web sends: { items, customer: { name, email, phone, address }, notes, payment_method }
        val checkoutRequest = CheckoutRequest(
            items = items.map { CheckoutItemRequest(it.productId, it.quantity) },
            customer = CheckoutCustomerRequest(
                name = customerName,
                phone = phone,
                address = fullAddress
            ),
            notes = notes.ifBlank { null },
            paymentMethod = "cash_on_delivery"
        )

        // Build local Order for UI display (OrderConfirmationScreen)
        val fullOrder = Order(
            id = "order-${System.currentTimeMillis()}",
            orderNumber = "",
            status = "pending",
            subtotal = subtotal,
            shippingCost = deliveryFee,
            total = total,
            paymentMethod = "cash_on_delivery",
            shippingAddress = fullAddress,
            shippingPhone = phone,
            notes = notes,
            items = items.map { OrderItem(productId = it.productId, name = it.name, price = it.price, quantity = it.quantity, category = it.category) },
            createdAt = ""
        )

        viewModelScope.launch {
            when (val result = safeApiCall { checkoutApi.placeOrder(checkoutRequest) }) {
                is ApiResult.Success -> {
                    val apiResponse = result.data
                    val serverData = apiResponse.data
                    // Merge server response into local Order for UI
                    val mergedOrder = fullOrder.copy(
                        id = serverData.id,
                        orderNumber = serverData.orderNumber,
                        status = serverData.status,
                        subtotal = serverData.subtotal,
                        shippingCost = serverData.shippingCost,
                        total = serverData.total,
                        createdAt = serverData.createdAt ?: ""
                    )
                    val orderResponse = OrderResponse(
                        id = serverData.id,
                        orderNumber = serverData.orderNumber,
                        status = serverData.status,
                        total = serverData.total,
                        createdAt = serverData.createdAt ?: ""
                    )
                    _lastPlacedOrder.value = mergedOrder
                    _checkoutState.value = CheckoutState.Success(orderResponse)
                    CartManager.clearCart()
                }
                is ApiResult.ServerError -> {
                    _checkoutState.value = CheckoutState.Error(result.message)
                }
                is ApiResult.NetworkError -> {
                    // Server unreachable — cannot place order without server confirmation
                    _checkoutState.value = CheckoutState.Error("Unable to connect to server. Please check your connection and try again.")
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