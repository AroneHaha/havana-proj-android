package com.example.havana.data.remote

import com.example.havana.data.model.AddToCartRequest
import com.example.havana.data.model.CartItem
import com.example.havana.data.model.CartResponse
import com.example.havana.data.model.WrappedServerCartItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApiService {

    @GET("cart")
    suspend fun getCart(@Header("Authorization") authHeader: String): CartResponse

    @POST("cart")
    suspend fun addToCart(
        @Header("Authorization") authHeader: String,
        @Body request: AddToCartRequest,
    ): WrappedServerCartItem

    @PUT("cart/{productId}")
    suspend fun updateQuantity(
        @Header("Authorization") authHeader: String,
        @Path("productId") productId: String,
        @Body quantity: Map<String, Int>,
    ): WrappedServerCartItem

    @DELETE("cart/{productId}")
    suspend fun removeFromCart(
        @Header("Authorization") authHeader: String,
        @Path("productId") productId: String,
    )

    @DELETE("cart")
    suspend fun clearCart(@Header("Authorization") authHeader: String)
}