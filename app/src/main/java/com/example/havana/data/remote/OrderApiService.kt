package com.example.havana.data.remote

import com.example.havana.data.model.Order
import retrofit2.http.GET
import retrofit2.http.Path

interface OrderApiService {

    @GET("orders")
    suspend fun getOrders(): List<Order>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") orderId: String): Order
}