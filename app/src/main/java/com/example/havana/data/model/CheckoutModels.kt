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

// ═══════════════════════════════════════════════════════════════════
// Checkout request/response — matches web checkout-service.ts exactly
// Web payload: { items, customer: { name, email, phone, address }, notes, payment_method }
// ═══════════════════════════════════════════════════════════════════

/**
 * Request body sent to POST /api/checkout.
 * Matches web checkout-service.ts CheckoutPayload format exactly.
 * Backend validates stock and prices server-side.
 */
data class CheckoutRequest(
    val items: List<CheckoutItemRequest>,
    val customer: CheckoutCustomerRequest,
    val notes: String? = null,
    @SerializedName("payment_method")
    val paymentMethod: String = "cash_on_delivery"
)

data class CheckoutCustomerRequest(
    val name: String,
    val email: String? = null,
    val phone: String,
    val address: String
)

data class CheckoutItemRequest(
    @SerializedName("product_id")
    val productId: String,
    val quantity: Int
)

/**
 * Backend response wrapper: { data: OrderResource, message: "..." }
 * respondCreated() wraps in { data: {...}, message: "..." }
 * OrderResource includes: id, order_id, order_number, status, subtotal,
 * shipping_cost, total, items, created_at, etc.
 */
data class CheckoutApiResponse(
    val data: CheckoutOrderData,
    val message: String? = null
)

/**
 * Matches Laravel OrderResource::toArray() output (the web reads this too).
 */
data class CheckoutOrderData(
    val id: String,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("order_number")
    val orderNumber: String,
    val status: String,
    val subtotal: Double,
    @SerializedName("shipping_cost")
    val shippingCost: Double,
    val discount: Double = 0.0,
    val total: Double,
    @SerializedName("payment_method")
    val paymentMethod: String = "",
    @SerializedName("shipping_address")
    val shippingAddress: String = "",
    @SerializedName("shipping_phone")
    val shippingPhone: String = "",
    val notes: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val items: List<CheckoutOrderItemData>? = null
)

data class CheckoutOrderItemData(
    val id: String? = null,
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("product_name")
    val productName: String,
    val price: Double,
    val quantity: Int,
    @SerializedName("subtotal")
    val subtotalVal: Double = 0.0,
)

// ═══════════════════════════════════════════════════════════════════
// Local Order model — used for UI display (OrderConfirmationScreen etc.)
// ═══════════════════════════════════════════════════════════════════

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
    @SerializedName("order_number")
    val orderNumber: String,
    @SerializedName("customer_name")
    val customerName: String,
    val phone: String,
    @SerializedName("delivery_address")
    val deliveryAddress: DeliveryAddress,
    val notes: String,
    @SerializedName("payment_method")
    val paymentMethod: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    @SerializedName("delivery_fee")
    val deliveryFee: Double,
    val total: Double,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
)

data class OrderItem(
    @SerializedName("product_id")
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
        "pending" -> "\u23F3"
        "confirmed" -> "\u2705"
        "preparing" -> "\uD83D\uDCE6"
        "out_for_delivery" -> "\uD83D\uDE9A"
        "delivered" -> "\uD83C\uDF89"
        "cancelled" -> "\u274C"
        else -> "\uD83D\uDCCB"
    }
}