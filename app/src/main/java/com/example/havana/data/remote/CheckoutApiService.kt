package com.example.havana.data.remote

import com.example.havana.data.model.OrderRequest
import com.example.havana.data.model.OrderResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckoutApiService {

    @POST("orders")
    suspend fun placeOrder(@Body request: OrderRequest): OrderResponse
}