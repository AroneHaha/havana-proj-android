package com.example.havana.data.remote

import com.example.havana.data.model.Product
import com.example.havana.data.model.Review
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductDetailsApiService {

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") productId: String): Product

    @GET("products/{id}/reviews")
    suspend fun getReviews(@Path("id") productId: String): List<Review>
}