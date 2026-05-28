package com.example.havana.data.model
import com.google.gson.annotations.SerializedName

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image: String? = null,
    val category: String,
    val rating: Float = 0f,
    @SerializedName("review_count")
    val reviewCount: Int = 0,
    @SerializedName("is_featured")
    val isFeatured: Boolean = false,
    @SerializedName("is_top_selling")
    val isTopSelling: Boolean = false,
    @SerializedName("in_stock")
    val inStock: Boolean = true,
    val images: List<String> = emptyList(),
)

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
)

sealed class ProductListState {
    data object Idle : ProductListState()
    data object Loading : ProductListState()
    data class Success(val products: List<Product>) : ProductListState()
    data class Error(val message: String) : ProductListState()
}

sealed class CategoryState {
    data object Idle : CategoryState()
    data object Loading : CategoryState()
    data class Success(val categories: List<Category>) : CategoryState()
    data class Error(val message: String) : CategoryState()
}

data class Review(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_name")
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: String,
    val avatar: String? = null,
)

sealed class ReviewState {
    data object Idle : ReviewState()
    data object Loading : ReviewState()
    data class Success(val reviews: List<Review>) : ReviewState()
    data class Error(val message: String) : ReviewState()
}

data class CartItem(
    @SerializedName("product_id")
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val image: String? = null,
    val category: String,
)