package com.example.havana.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.havana.data.model.ProductDataResponse

interface ProductDetailsApiService {
    @GET("products/{id}")
    suspend fun getProduct(
        @Path("id") productId: String,
        @Query("locale") locale: String = "en",
    ): ProductDataResponse
}