package com.example.havana.data.remote

import com.example.havana.data.model.ProductDataResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductDetailsApiService {

    /**
     * GET /api/products/{id} — single product detail.
     * Backend returns: { data: { ... } }
     */
    @GET("products/{id}")
    suspend fun getProduct(
        @Path("id") productId: String,
        @Query("locale") locale: String = "en",
    ): ProductDataResponse

    /**
     * GET /api/products/{id}/reviews — reviews for a product.
     * Note: Backend includes reviews in the product response via ProductResource.
     * This endpoint may not exist yet on the backend; using product detail instead.
     */
    @GET("products/{id}/reviews")
    suspend fun getReviews(@Path("id") productId: String): List<Review>
}
