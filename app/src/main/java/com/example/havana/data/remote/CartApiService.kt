package com.example.havana.data.remote

import com.example.havana.data.model.AddToCartRequest
import com.example.havana.data.model.CartResponse
import com.example.havana.data.model.WrappedServerCartItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface CartApiService {

    @GET("cart")
    suspend fun getCart(): CartResponse

    @POST("cart/items")
    suspend fun addToCart(@Body request: AddToCartRequest): WrappedServerCartItem

    @DELETE("cart")
    suspend fun clearCart()
}