package com.example.havana.data.remote

import com.example.havana.data.model.Product
import com.example.havana.data.model.ProductsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {

    /**
     * GET /api/products — paginated list with optional filters.
     * Backend returns: { data: [...], meta: { current_page, total, ... } }
     */
    @GET("products")
    suspend fun getProducts(
        @Query("per_page") perPage: Int = 50,
        @Query("locale") locale: String = "en",
    ): ProductsResponse

    /**
     * GET /api/products?filter[is_featured]=true
     */
    @GET("products")
    suspend fun getFeaturedProducts(
        @Query("filter[is_featured]") isFeatured: Boolean = true,
        @Query("per_page") perPage: Int = 20,
        @Query("locale") locale: String = "en",
    ): ProductsResponse

    /**
     * GET /api/products?filter[is_best_seller]=true
     */
    @GET("products")
    suspend fun getTopSellingProducts(
        @Query("filter[is_best_seller]") isBestSeller: Boolean = true,
        @Query("per_page") perPage: Int = 20,
        @Query("locale") locale: String = "en",
    ): ProductsResponse

    /**
     * GET /api/products?search=query
     */
    @GET("products")
    suspend fun searchProducts(
        @Query("search") query: String,
        @Query("per_page") perPage: Int = 50,
        @Query("locale") locale: String = "en",
    ): ProductsResponse

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
     * GET /api/categories — list all active categories.
     * Backend returns: { data: [...] }
     */
    @GET("categories")
    suspend fun getCategories(
        @Query("locale") locale: String = "en",
    ): CategoriesResponse
}
