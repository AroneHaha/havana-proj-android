package com.example.havana.data.remote

import com.example.havana.data.model.CheckoutApiResponse
import com.example.havana.data.model.OrderRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckoutApiService {

    @POST("checkout")
    suspend fun placeOrder(@Body request: OrderRequest): CheckoutApiResponse
}