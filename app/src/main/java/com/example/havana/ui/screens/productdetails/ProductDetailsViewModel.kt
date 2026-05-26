package com.example.havana.ui.screens.productdetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.mock.MockData
import com.example.havana.data.model.*
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.session.SessionManager
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
                _productState.value = MockData.getProductById(productId)
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
                _reviewState.value = ReviewState.Success(MockData.reviews)
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
                category = product.category
            )
        )
        _addedToCart.value = true
    }

    fun resetAddedToCart() {
        _addedToCart.value = false
    }
}