package com.example.havana.data.model

import com.google.gson.annotations.SerializedName

data class DeliveryAddress(
    val fullAddress: String,
    val area: String = "",
    val block: String = "",
    val street: String = "",
    val building: String = "",
    val floor: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)

data class OrderRequest(
    val customerName: String,
    val phone: String,
    val deliveryAddress: DeliveryAddress,
    val notes: String,
    val paymentMethod: String = "cod",
    val items: List<OrderItemRequest>,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
)

data class OrderItemRequest(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
)

data class OrderResponse(
    val id: String,
    @SerializedName("order_number")
    val orderNumber: String,
    val status: String,
    val total: Double,
    @SerializedName("created_at")
    val createdAt: String,
)

sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Loading : CheckoutState()
    data class Success(val order: OrderResponse) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}