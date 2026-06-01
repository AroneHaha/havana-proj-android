package com.example.havana.data.remote

import com.example.havana.data.model.CartItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApiService {

    @GET("cart")
    suspend fun getCart(@Header("Authorization") authHeader: String): List<CartItem>

    @POST("cart")
    suspend fun addToCart(
        @Header("Authorization") authHeader: String,
        @Body item: CartItem,
    ): CartItem

    @PUT("cart/{productId}")
    suspend fun updateQuantity(
        @Header("Authorization") authHeader: String,
        @Path("productId") productId: String,
        @Body quantity: Map<String, Int>,
    ): CartItem

    @DELETE("cart/{productId}")
    suspend fun removeFromCart(
        @Header("Authorization") authHeader: String,
        @Path("productId") productId: String,
    )

    @DELETE("cart")
    suspend fun clearCart(@Header("Authorization") authHeader: String)
}
