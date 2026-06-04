package com.example.havana.ui.screens.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.cart.CartManager
import com.example.havana.data.model.AddToCartRequest
import com.example.havana.data.model.CartItem
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.safeApiCall
import com.example.havana.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val cartApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.CartApiService::class.java
    )

    val cartItems = CartManager.cartItems

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total.asStateFlow()

    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()

    private val _syncState = MutableStateFlow<CartSyncState>(CartSyncState.Idle)
    val syncState: StateFlow<CartSyncState> = _syncState.asStateFlow()

    init {
        viewModelScope.launch {
            cartItems.collect { items ->
                _total.value = items.sumOf { it.price * it.quantity }
                _itemCount.value = items.sumOf { it.quantity }
            }
        }
    }

    fun syncFromServer() {
        if (SessionManager.token == null) return
        viewModelScope.launch {
            _syncState.value = CartSyncState.Syncing
            when (val result = safeApiCall { cartApi.getCart() }) {
                is ApiResult.Success -> {
                    val serverResponse = result.data
                    val serverItems = serverResponse.items.map { sci ->
                        CartItem(
                            productId = sci.productId,
                            name = sci.product.name,
                            price = sci.product.displayPrice,
                            quantity = sci.quantity,
                            image = sci.product.image,
                            category = sci.product.categoryName,
                        )
                    }
                    val localItems = CartManager.cartItems.value
                    val serverProductIds = serverItems.map { it.productId }.toSet()
                    val merged = serverItems + localItems.filter { it.productId !in serverProductIds }
                    CartManager.setCartItems(merged)
                    _syncState.value = CartSyncState.Synced
                }
                is ApiResult.ServerError -> {
                    _syncState.value = CartSyncState.Idle
                }
                is ApiResult.NetworkError -> {
                    _syncState.value = CartSyncState.Idle
                }
            }
        }
    }

    fun increaseQuantity(productId: String) {
        val items = cartItems.value
        val item = items.find { it.productId == productId } ?: return
        val newQty = item.quantity + 1
        CartManager.updateQuantity(productId, newQty)
    }

    fun decreaseQuantity(productId: String) {
        val items = cartItems.value
        val item = items.find { it.productId == productId } ?: return
        val newQty = item.quantity - 1
        CartManager.updateQuantity(productId, newQty)
    }

    fun removeItem(productId: String) {
        CartManager.removeFromCart(productId)
    }

    fun clearCart() {
        CartManager.clearCart()
        pushClearToServer()
    }

    private fun pushClearToServer() {
        viewModelScope.launch {
            try {
                safeApiCall { cartApi.clearCart() }
            } catch (_: Exception) { /* best-effort sync */ }
        }
    }
}

sealed class CartSyncState {
    data object Idle : CartSyncState()
    data object Syncing : CartSyncState()
    data object Synced : CartSyncState()
}