package com.example.havana.data.remote

import com.example.havana.data.model.Order
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface OrderApiService {

    /**
     * GET /api/orders — customer's order history.
     * Backend returns: { data: [OrderResource, ...], meta: { current_page, total, ... } }
     */
    @GET("orders")
    suspend fun getOrders(): OrdersResponse

    /**
     * GET /api/orders/{id} — single order detail.
     * Backend returns: { data: OrderResource, message: "..." } via respondWithData()
     */
    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") orderId: String): OrderDetailResponse

    @PATCH("orders/{id}/confirm-delivery")
    suspend fun confirmDelivery(@Path("id") orderId: String): OrderDetailResponse
}

/** Wrapper for GET /api/orders paginated response. */
data class OrdersResponse(
    val data: List<Order>,
    val meta: OrdersMeta? = null
)

data class OrdersMeta(
    val current_page: Int = 1,
    val last_page: Int = 1,
    val per_page: Int = 15,
    val total: Int = 0,
)

/** Wrapper for single order responses that use respondWithData(). */
data class OrderDetailResponse(
    val data: Order,
    val message: String? = null
)
