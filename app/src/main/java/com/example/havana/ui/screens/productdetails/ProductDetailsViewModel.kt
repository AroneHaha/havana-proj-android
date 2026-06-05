package com.example.havana.ui.screens.productdetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.*
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.safeApiCall
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

    private val _errorState = MutableStateFlow("")
    val errorState: StateFlow<String> = _errorState.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _addedToCart = MutableStateFlow(false)
    val addedToCart: StateFlow<Boolean> = _addedToCart.asStateFlow()

    fun loadProduct(productId: String) {
        _quantity.value = 1
        _addedToCart.value = false
        _errorState.value = ""
        _isLoading.value = true
        _reviewState.value = ReviewState.Idle
        viewModelScope.launch {
            when (val result = safeApiCall { detailsApi.getProduct(productId) }) {
                is ApiResult.Success -> {
                    val product = result.data.data
                    _productState.value = product
                    _errorState.value = ""

                    if (product.reviews.isNotEmpty()) {
                        _reviewState.value = ReviewState.Success(product.reviews)
                    } else {
                        _reviewState.value = ReviewState.Success(emptyList())
                    }
                }
                is ApiResult.ServerError -> {
                    _errorState.value = result.message
                    _productState.value = null
                }
                is ApiResult.NetworkError -> {
                    _errorState.value = result.error
                    _productState.value = null
                }
            }
            _isLoading.value = false
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
        if (!product.inStock) return

        com.example.havana.data.cart.CartManager.addToCart(
            com.example.havana.data.model.CartItem(
                productId = product.id,
                name = product.name,
                price = product.price,
                quantity = _quantity.value,
                image = product.image,
                category = product.categoryName
            )
        )
        _addedToCart.value = true
    }

    fun resetAddedToCart() {
        _addedToCart.value = false
    }
}