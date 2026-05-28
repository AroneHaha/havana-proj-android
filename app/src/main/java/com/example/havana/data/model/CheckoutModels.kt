package com.example.havana.data.model

import com.google.gson.annotations.SerializedName
import androidx.compose.ui.graphics.Color

data class DeliveryAddress(
    val fullAddress: String,
    val area: String = "",
    val block: String = "",
    val street: String = "",
    val building: String = "",
    val floor: String = "",
    val apartment: String = "",
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

data class Order(
    val id: String,
    val orderNumber: String,
    val customerName: String,
    val phone: String,
    val deliveryAddress: DeliveryAddress,
    val notes: String,
    val paymentMethod: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val status: String,
    val createdAt: String,
)

data class OrderItem(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String,
)

sealed class OrderListState {
    data object Idle : OrderListState()
    data object Loading : OrderListState()
    data class Success(val orders: List<Order>) : OrderListState()
    data class Error(val message: String) : OrderListState()
}

fun Order.statusColor(): Color {
    return when (status) {
        "pending" -> Color(0xFFF59E0B)
        "confirmed" -> Color(0xFF3B82F6)
        "preparing" -> Color(0xFF8B5CF6)
        "out_for_delivery" -> Color(0xFF6366F1)
        "delivered" -> Color(0xFF10B981)
        "cancelled" -> Color(0xFFEF4444)
        else -> Color(0xFF737373)
    }
}

fun Order.statusLabel(): String {
    return when (status) {
        "pending" -> "Pending"
        "confirmed" -> "Confirmed"
        "preparing" -> "Preparing"
        "out_for_delivery" -> "Out for Delivery"
        "delivered" -> "Delivered"
        "cancelled" -> "Cancelled"
        else -> status.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

/** Localized version of statusLabel that accepts pre-resolved string resources. */
fun Order.localizedStatus(
    pending: String,
    confirmed: String,
    preparing: String,
    outForDelivery: String,
    delivered: String,
    cancelled: String,
): String {
    return when (status) {
        "pending" -> pending
        "confirmed" -> confirmed
        "preparing" -> preparing
        "out_for_delivery" -> outForDelivery
        "delivered" -> delivered
        "cancelled" -> cancelled
        else -> status.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

fun Order.statusEmoji(): String {
    return when (status) {
        "pending" -> "⏳"
        "confirmed" -> "✅"
        "preparing" -> "📦"
        "out_for_delivery" -> "🚚"
        "delivered" -> "🎉"
        "cancelled" -> "❌"
        else -> "📋"
    }
}