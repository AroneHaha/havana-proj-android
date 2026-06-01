package com.example.havana.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.Category
import com.example.havana.data.model.CategoryState
import com.example.havana.data.model.Product
import com.example.havana.data.model.ProductListState
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.safeApiCall
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

    private var allProducts: List<Product> = emptyList()

    init {
        loadCategories()
        loadProducts()
    }

    private fun loadCategories() {
        _categoryState.value = CategoryState.Loading
        viewModelScope.launch {
            when (val result = safeApiCall { productApi.getCategories() }) {
                is ApiResult.Success -> {
                    val categories = result.data.data
                    _categoryState.value = CategoryState.Success(
                        listOf(Category("all", "All", "\uD83C\uDF38")) + categories
                    )
                }
                is ApiResult.ServerError -> {
                    _categoryState.value = CategoryState.Error(result.message)
                }
                is ApiResult.NetworkError -> {
                    _categoryState.value = CategoryState.Error(result.error)
                }
            }
        }
    }

    fun loadProducts() {
        _productState.value = ProductListState.Loading
        viewModelScope.launch {
            when (val result = safeApiCall { productApi.getProducts() }) {
                is ApiResult.Success -> {
                    val products = result.data.data
                    allProducts = products
                    _productState.value = ProductListState.Success(products)
                }
                is ApiResult.ServerError -> {
                    _productState.value = ProductListState.Error(result.message)
                }
                is ApiResult.NetworkError -> {
                    _productState.value = ProductListState.Error(result.error)
                }
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

            val matchesCategory = category == "All" ||
                    product.categoryName.equals(category, ignoreCase = true) ||
                    product.categoryId == category

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
}