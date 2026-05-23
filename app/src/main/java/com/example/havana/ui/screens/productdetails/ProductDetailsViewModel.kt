package com.example.havana.ui.screens.productdetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.*
import com.example.havana.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val detailsApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.ProductDetailsApiService::class.java
    )

    private val _productState = MutableStateFlow<Product?>(null)
    val productState: StateFlow<Product?> = _productState.asStateFlow()

    private val _reviewState = MutableStateFlow<ReviewState>(ReviewState.Idle)
    val reviewState: StateFlow<ReviewState> = _reviewState.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _addedToCart = MutableStateFlow(false)
    val addedToCart: StateFlow<Boolean> = _addedToCart.asStateFlow()

    fun loadProduct(productId: String) {
        _quantity.value = 1
        _addedToCart.value = false
        viewModelScope.launch {
            try {
                val product = detailsApi.getProduct(productId)
                _productState.value = product
            } catch (_: Exception) {
                // Mock fallback
                _productState.value = getMockProduct(productId)
            }
        }
        loadReviews(productId)
    }

    private fun loadReviews(productId: String) {
        _reviewState.value = ReviewState.Loading
        viewModelScope.launch {
            try {
                val reviews = detailsApi.getReviews(productId)
                _reviewState.value = ReviewState.Success(reviews)
            } catch (_: Exception) {
                // Mock fallback
                _reviewState.value = ReviewState.Success(getMockReviews(productId))
            }
        }
    }

    fun increaseQuantity() {
        _quantity.value = _quantity.value + 1
        _addedToCart.value = false
    }

    fun decreaseQuantity() {
        if (_quantity.value > 1) {
            _quantity.value = _quantity.value - 1
            _addedToCart.value = false
        }
    }

    fun addToCart() {
        val product = _productState.value ?: return

        // Stock validation
        if (!product.inStock) {
            return
        }

        com.example.havana.data.cart.CartManager.addToCart(
            com.example.havana.data.model.CartItem(
                productId = product.id,
                name = product.name,
                price = product.price,
                quantity = _quantity.value,
                category = product.category
            )
        )
        _addedToCart.value = true
    }

    fun resetAddedToCart() {
        _addedToCart.value = false
    }

    // ===== MOCK DATA =====

    private fun getMockProduct(productId: String): Product {
        val products = mapOf(
            "1" to Product("1", "Royal Red Roses", "A stunning bouquet of 12 premium red roses, hand-picked and carefully arranged in luxury wrapping paper. Perfect for anniversaries, Valentine's Day, or any occasion that calls for a bold romantic gesture. Each rose is selected for its deep red hue and long stem.", 27.500, null, "Roses", 4.8f, 124, true, true),
            "2" to Product("2", "Sunset Bouquet", "Warm-toned arrangement featuring sunflowers, orange roses, and carnations. This radiant bouquet captures the golden hour in floral form, bringing warmth and joy to any space.", 20.000, null, "Bouquets", 4.6f, 89, true, false),
            "3" to Product("3", "Pink Blush Arrangement", "Elegant pink lilies and roses beautifully arranged in a clear glass vase. A soft, romantic arrangement that speaks of grace and sophistication.", 23.000, null, "Arrangements", 4.7f, 67, false, true),
            "4" to Product("4", "Lavender Dreams", "Relaxing lavender and purple flower collection. Calming hues of purple create a serene and peaceful arrangement, perfect for creating a tranquil atmosphere.", 17.000, null, "Bouquets", 4.5f, 45, false, false),
            "5" to Product("5", "Golden Gift Box", "Premium flower box paired with artisan chocolates and a personalized greeting card. The ultimate luxury gift that combines beauty and sweetness.", 37.000, null, "Gifts", 4.9f, 156, true, true),
            "6" to Product("6", "White Elegance", "Pure white lilies and orchids for special occasions. Timeless and sophisticated, this arrangement conveys purity and elegance for weddings and formal events.", 29.000, null, "Arrangements", 4.8f, 98, true, true),
            "7" to Product("7", "Tropical Paradise", "Bold tropical flowers with birds of paradise. A vibrant and exotic arrangement that brings island energy and excitement to any room.", 34.000, null, "Arrangements", 4.4f, 34, false, false),
            "8" to Product("8", "Mini Rose Plant", "Beautiful mini rose plant in a ceramic pot. A living gift that keeps giving, these compact rose plants thrive indoors and bloom repeatedly.", 14.000, null, "Plants", 4.6f, 78, false, true),
            "9" to Product("9", "Peony Premium Box", "Luxury box of fresh seasonal peonies. Each box is carefully curated with the finest peonies available, creating a breathtaking display of lush, ruffled blooms.", 42.000, null, "Bouquets", 4.9f, 201, true, true),
            "10" to Product("10", "Succulent Garden", "Assorted succulents in a decorative tray. A modern and low-maintenance arrangement that brings green beauty to desks, shelves, and windowsills.", 12.000, null, "Plants", 4.3f, 56, false, false),
            "11" to Product("11", "Romance Bundle", "Red roses + teddy bear + balloon set. The complete romance package — stunning roses paired with an adorable teddy and celebratory balloon.", 30.000, null, "Gifts", 4.7f, 143, true, true),
            "12" to Product("12", "Daisy Delight", "Cheerful daisy bunch wrapped in kraft paper. Simple, fresh, and full of sunshine — perfect for brightening someone's day.", 10.000, null, "Bouquets", 4.2f, 29, false, false),
        )
        return products[productId] ?: Product(productId, "Unknown Product", "No description available", 0.0, null, "Bouquets")
    }

    private fun getMockReviews(productId: String): List<Review> {
        return listOf(
            Review("r1", "u1", "Fatima Al-Sabah", 5f, "Absolutely gorgeous! The roses were fresh and beautifully arranged. My wife loved them. Will definitely order again for our anniversary.", "2025-12-15"),
            Review("r2", "u2", "Ahmed Hassan", 4f, "Great quality flowers, delivery was on time. Only wish the wrapping was a bit more luxurious for the price point.", "2025-12-10"),
            Review("r3", "u3", "Sara Al-Ali", 5f, "This is my go-to shop for gifts in Kuwait. Never disappointed! The presentation is always top-notch.", "2025-11-28"),
            Review("r4", "u4", "Mohammed Jassim", 4f, "Beautiful bouquet, arrived fresh. The scent filled the entire room. Minor delay in delivery but overall very happy.", "2025-11-20"),
            Review("r5", "u5", "Noor Al-Din", 3f, "Flowers were nice but didn't look exactly like the photo. Still good quality though.", "2025-11-15"),
        )
    }
}