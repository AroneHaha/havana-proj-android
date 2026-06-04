package com.example.havana.data.remote

import com.example.havana.data.model.Order
import com.example.havana.data.model.PaginatedMeta
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {

    /**
     * GET /api/orders — customer's order history (paginated).
     * Backend returns: { data: [Order, ...], meta: { current_page, last_page, ... } }
     * Auth header is added automatically by ApiClient's auth interceptor.
     */
    @GET("orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50,
    ): OrdersListApiResponse

    /**
     * GET /api/orders/{order} — single order detail.
     * Backend returns: { data: Order }
     */
    @GET("orders/{orderId}")
    suspend fun getOrder(@Path("orderId") orderId: String): OrderDetailApiResponse

    /**
     * POST /api/orders/{order}/cancel — customer cancels their order.
     * Only allowed for pending/confirmed orders.
     * Backend returns: { data: Order }
     */
    @POST("orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: String): OrderDetailApiResponse
}

// ── Response wrappers ──────────────────────────────────────────────────

/** GET /api/orders → { data: [Order], meta: {...} } */
data class OrdersListApiResponse(
    val data: List<Order>,
    val meta: PaginatedMeta? = null,
)

/** GET /api/orders/{id} → { data: Order } */
data class OrderDetailApiResponse(
    val data: Order,
)