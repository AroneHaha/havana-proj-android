package com.example.havana.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.Category
import com.example.havana.data.model.CategoryState
import com.example.havana.data.model.Product
import com.example.havana.data.model.ProductListState
import com.example.havana.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val productApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.ProductApiService::class.java
    )

    private val _productState = MutableStateFlow<ProductListState>(ProductListState.Idle)
    val productState: StateFlow<ProductListState> = _productState.asStateFlow()

    private val _categoryState = MutableStateFlow<CategoryState>(CategoryState.Idle)
    val categoryState: StateFlow<CategoryState> = _categoryState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // All products loaded (for local filtering)
    private var allProducts: List<Product> = emptyList()

    init {
        loadCategories()
        loadProducts()
    }

    private fun loadCategories() {
        _categoryState.value = CategoryState.Loading
        viewModelScope.launch {
            try {
                val categories = productApi.getCategories()
                _categoryState.value = CategoryState.Success(listOf(Category("all", "All", "🌸")) + categories)
            } catch (_: Exception) {
                // Mock fallback
                _categoryState.value = CategoryState.Success(getMockCategories())
            }
        }
    }

    private fun loadProducts() {
        _productState.value = ProductListState.Loading
        viewModelScope.launch {
            try {
                val products = productApi.getProducts()
                allProducts = products
                _productState.value = ProductListState.Success(products)
            } catch (_: Exception) {
                // Mock fallback
                allProducts = getMockProducts()
                _productState.value = ProductListState.Success(allProducts)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterProducts()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        filterProducts()
    }

    private fun filterProducts() {
        val query = _searchQuery.value.lowercase().trim()
        val category = _selectedCategory.value

        val filtered = allProducts.filter { product ->
            val matchesSearch = query.isEmpty() ||
                    product.name.lowercase().contains(query) ||
                    product.description.lowercase().contains(query)

            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)

            matchesSearch && matchesCategory
        }

        _productState.value = ProductListState.Success(filtered)
    }

    fun getFeaturedProducts(): List<Product> {
        return allProducts.filter { it.isFeatured }
    }

    fun getTopSellingProducts(): List<Product> {
        return allProducts.filter { it.isTopSelling }
    }

    // ===== MOCK DATA =====

    private fun getMockCategories(): List<Category> {
        return listOf(
            Category("all", "All", "🌸"),
            Category("bouquets", "Bouquets", "💐"),
            Category("roses", "Roses", "🌹"),
            Category("arrangements", "Arrangements", "🌺"),
            Category("gifts", "Gifts", "🎁"),
            Category("plants", "Plants", "🪴"),
        )
    }

    private fun getMockProducts(): List<Product> {
        return listOf(
            Product("1", "Royal Red Roses", "A stunning bouquet of 12 premium red roses wrapped in luxury paper", 27.500, null, "Roses", 4.8f, 124, true, true),
            Product("2", "Sunset Bouquet", "Warm-toned arrangement with sunflowers, roses, and carnations", 20.000, null, "Bouquets", 4.6f, 89, true, false),
            Product("3", "Pink Blush Arrangement", "Elegant pink lilies and roses in a glass vase", 23.000, null, "Arrangements", 4.7f, 67, false, true),
            Product("4", "Lavender Dreams", "Relaxing lavender and purple flower collection", 17.000, null, "Bouquets", 4.5f, 45, false, false),
            Product("5", "Golden Gift Box", "Premium flower box with chocolates and a greeting card", 37.000, null, "Gifts", 4.9f, 156, true, true),
            Product("6", "White Elegance", "Pure white lilies and orchids for special occasions", 29.000, null, "Arrangements", 4.8f, 98, true, true),
            Product("7", "Tropical Paradise", "Bold tropical flowers with birds of paradise", 34.000, null, "Arrangements", 4.4f, 34, false, false),
            Product("8", "Mini Rose Plant", "Beautiful mini rose plant in a ceramic pot", 14.000, null, "Plants", 4.6f, 78, false, true),
            Product("9", "Peony Premium Box", "Luxury box of fresh seasonal peonies", 42.000, null, "Bouquets", 4.9f, 201, true, true),
            Product("10", "Succulent Garden", "Assorted succulents in a decorative tray", 12.000, null, "Plants", 4.3f, 56, false, false),
            Product("11", "Romance Bundle", "Red roses + teddy bear + balloon set", 30.000, null, "Gifts", 4.7f, 143, true, true),
            Product("12", "Daisy Delight", "Cheerful daisy bunch wrapped in kraft paper", 10.000, null, "Bouquets", 4.2f, 29, false, false),
        )
    }
}