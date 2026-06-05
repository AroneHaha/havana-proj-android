package com.example.havana.data.model
import com.google.gson.annotations.SerializedName

data class Product(
    val id: String,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    @SerializedName("sale_price")
    val salePrice: Double? = null,
    @SerializedName("effective_price")
    val effectivePrice: Double? = null,
    @SerializedName("is_on_sale")
    val isOnSale: Boolean = false,
    val image: String? = null,
    @SerializedName("category_id")
    val categoryId: String? = null,
    val category: Category? = null,
    val rating: Float = 0f,
    @SerializedName("reviews_count")
    val reviewCount: Int = 0,
    @SerializedName("is_featured")
    val isFeatured: Boolean = false,
    @SerializedName("is_best_seller")
    val isBestSeller: Boolean = false,
    @SerializedName("is_new")
    val isNew: Boolean = false,
    @SerializedName("in_stock")
    val inStock: Boolean = true,
    val stock: Int = 0,
    val images: List<String> = emptyList(),
    val reviews: List<Review> = emptyList(),
) {
    val displayPrice: Double get() = if (isOnSale && salePrice != null) salePrice else price
    val isTopSelling: Boolean get() = isBestSeller
    val categoryName: String get() = category?.name ?: ""
}

data class Category(
    val id: String,
    val name: String = "",
    val emoji: String = "",
    @SerializedName("name_en")
    val nameEn: String = "",
    @SerializedName("name_ar")
    val nameAr: String = "",
    val slug: String = "",
    val image: String? = null,
    @SerializedName("products_count")
    val productsCount: Int = 0,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    @SerializedName("sort_order")
    val sortOrder: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
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
    val userId: String = "",
    val userName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    @SerializedName("created_at")
    val date: String = "",
    val avatar: String? = null,
    @SerializedName("user")
    private val _user: ReviewUser? = null,
) {
    val displayName: String get() = _user?.fullName ?: userName
}

data class ReviewUser(
    @SerializedName("full_name")
    val fullName: String = "",
    @SerializedName("first_name")
    val firstName: String = "",
    @SerializedName("last_name")
    val lastName: String = "",
)

data class ReviewRequest(
    @SerializedName("product_id")
    val productId: String,
    val rating: Int,
    val title: String? = null,
    val comment: String? = null,
)

sealed class ReviewState {
    data object Idle : ReviewState()
    data object Loading : ReviewState()
    data class Success(val reviews: List<Review>) : ReviewState()
    data class Error(val message: String) : ReviewState()
}

data class ProductsResponse(
    val data: List<Product>,
    val meta: PaginatedMeta? = null,
)

data class PaginatedMeta(
    @SerializedName("current_page")
    val currentPage: Int = 1,
    @SerializedName("last_page")
    val lastPage: Int = 1,
    @SerializedName("per_page")
    val perPage: Int = 15,
    val total: Int = 0,
)

data class CategoriesResponse(
    val data: List<Category>,
)

data class ProductDataResponse(
    val data: Product,
)

data class CartItem(
    @SerializedName("product_id")
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val image: String? = null,
    val category: String = "",
)