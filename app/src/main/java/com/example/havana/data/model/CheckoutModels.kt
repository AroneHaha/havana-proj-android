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

data class OrderRequest(
    @SerializedName("shipping_address")
    val shippingAddress: String,
    @SerializedName("shipping_phone")
    val shippingPhone: String,
    val notes: String? = null,
    @SerializedName("payment_method")
    val paymentMethod: String = "cash_on_delivery",
)

data class CheckoutApiResponse(
    val data: OrderResponse,
    val message: String,
)

data class OrderResponse(
    val id: String,
    @SerializedName("order_number")
    val orderNumber: String,
    val status: String,
    val subtotal: Double = 0.0,
    @SerializedName("shipping_cost")
    val shippingCost: Double = 0.0,
    val total: Double,
    @SerializedName("shipping_address")
    val shippingAddress: String = "",
    @SerializedName("shipping_phone")
    val shippingPhone: String = "",
    @SerializedName("created_at")
    val createdAt: String,
)

sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Loading : CheckoutState()
    data class Success(val order: OrderResponse) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

/**
 * Order model — matches backend OrderResource exactly.
 * Fields use @SerializedName to map backend snake_case to Kotlin camelCase.
 */
data class Order(
    val id: String,
    @SerializedName("order_number")
    val orderNumber: String,
    @SerializedName("customer_name")
    val customerName: String = "",
    @SerializedName("shipping_phone")
    val phone: String = "",
    @SerializedName("shipping_address")
    val shippingAddress: String = "",
    val notes: String = "",
    @SerializedName("payment_method")
    val paymentMethod: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    @SerializedName("shipping_cost")
    val shippingCost: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double,
    @SerializedName("payment_status")
    val paymentStatus: String = "",
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String = "",
) {
    /** Backward-compatible: address as DeliveryAddress object */
    val deliveryAddress: DeliveryAddress get() = DeliveryAddress(fullAddress = shippingAddress)

    /** Backward-compatible: use shipping_cost */
    val deliveryFee: Double get() = shippingCost
}

/**
 * Order item — matches backend OrderItemResource exactly.
 */
data class OrderItem(
    val id: String = "",
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("product_name")
    val name: String = "",
    @SerializedName("product_image")
    val image: String? = null,
    val price: Double = 0.0,
    val quantity: Int = 0,
)

data class OrdersListResponse(
    val data: List<Order>,
    val meta: PaginatedMeta? = null,
)

data class OrderDetailResponse(
    val data: Order,
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