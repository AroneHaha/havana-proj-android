package com.example.havana.data.remote

import com.example.havana.data.model.Order
import com.example.havana.data.model.OrdersListResponse
import com.example.havana.data.model.OrderDetailResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApiService {

    @GET("orders")
    suspend fun getOrders(): OrdersListResponse

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") orderId: String): OrderDetailResponse

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") orderId: String)
}