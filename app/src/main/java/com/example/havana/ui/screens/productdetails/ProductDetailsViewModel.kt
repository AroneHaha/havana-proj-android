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

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _addedToCart = MutableStateFlow(false)
    val addedToCart: StateFlow<Boolean> = _addedToCart.asStateFlow()

    fun loadProduct(productId: String) {
        _quantity.value = 1
        _addedToCart.value = false
        viewModelScope.launch {
            when (val result = safeApiCall { detailsApi.getProduct(productId) }) {
                is ApiResult.Success -> _productState.value = result.data.data
                is ApiResult.ServerError -> {
                    _reviewState.value = ReviewState.Error(result.message)
                }
                is ApiResult.NetworkError -> {
                    _reviewState.value = ReviewState.Error(result.error)
                }
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
        if (!product.inStock) return

        com.example.havana.data.cart.CartManager.addToCart(
            com.example.havana.data.model.CartItem(
                productId = product.id,
                name = product.name,
                price = product.price,
                quantity = _quantity.value,
                category = product.categoryName
            )
        )
        _addedToCart.value = true
    }

    fun resetAddedToCart() {
        _addedToCart.value = false
    }
}