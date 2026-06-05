package com.example.havana.data.model

import com.google.gson.annotations.SerializedName
import androidx.compose.ui.graphics.Color

data class DeliveryAddress(
    @SerializedName("full_address")
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

// ─── Checkout request (backend expects only 4 fields) ─────────────

data class OrderRequest(
    @SerializedName("shipping_address")
    val shippingAddress: String,
    @SerializedName("shipping_phone")
    val shippingPhone: String,
    val notes: String,
    @SerializedName("payment_method")
    val paymentMethod: String = "cod",
)

data class OrderItemRequest(
    @SerializedName("product_id")
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
)

// ─── Backend response wrappers ────────────────────────────────────

data class CheckoutApiResponse(
    val data: Order,
    val message: String? = null,
)

data class OrdersListResponse(
    val data: List<Order>,
    val meta: ResponseMeta? = null,
)

data class ResponseMeta(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 15,
    val total: Int = 0,
)

data class OrderDetailResponse(
    val data: Order,
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

// ─── Order model (matches backend OrderResource) ───────────────────

data class Order(
    val id: String,
    @SerializedName("order_number")
    val orderNumber: String,
    @SerializedName("shipping_phone")
    val phone: String,
    @SerializedName("shipping_address")
    val shippingAddress: String,
    val notes: String = "",
    @SerializedName("payment_method")
    val paymentMethod: String = "cod",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    @SerializedName("shipping_cost")
    val shippingCost: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "pending",
    @SerializedName("created_at")
    val createdAt: String = "",
) {
    val customerName: String get() = ""
    val deliveryAddress: DeliveryAddress get() = DeliveryAddress(fullAddress = shippingAddress)
    val deliveryFee: Double get() = shippingCost
}

// ─── OrderItem (matches backend OrderItemResource) ────────────────

data class OrderItem(
    val id: String = "",
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("product_name")
    val name: String,
    val price: Double,
    val quantity: Int,
) {
    val category: String get() = "flowers"
}

sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Loading : CheckoutState()
    data class Success(val order: OrderResponse) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

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
        "pending" -> "\u23F3"
        "confirmed" -> "\u2705"
        "preparing" -> "\uD83D\uDCE6"
        "out_for_delivery" -> "\uD83D\uDE9A"
        "delivered" -> "\uD83C\uDF89"
        "cancelled" -> "\u274C"
        else -> "\uD83D\uDCCB"
    }
}