package com.example.havana.data.model

data class ProductsResponse(
    val data: List<Product>,
    val meta: PaginationMeta? = null,
)

data class PaginationMeta(
    val current_page: Int = 1,
    val last_page: Int = 1,
    val per_page: Int = 50,
    val total: Int = 0,
)