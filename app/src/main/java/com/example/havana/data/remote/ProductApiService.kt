package com.example.havana.data.remote

import com.example.havana.data.model.Category
import com.example.havana.data.model.Product
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApiService {

    @GET("products")
    suspend fun getProducts(): List<Product>

    @GET("products/featured")
    suspend fun getFeaturedProducts(): List<Product>

    @GET("products/top-selling")
    suspend fun getTopSellingProducts(): List<Product>

    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): List<Product>

    @GET("categories")
    suspend fun getCategories(): List<Category>
}