package com.example.havana.ui.screens.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.cart.CartManager
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.safeApiCall
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
        // Observe cart changes and recompute totals
        viewModelScope.launch {
            cartItems.collect { items ->
                _total.value = items.sumOf { it.price * it.quantity }
                _itemCount.value = items.sumOf { it.quantity }
            }
        }
    }

    /**
     * Pull server cart on login. If server has items, merge with local.
     * Call this after successful login.
     */
    fun syncFromServer() {
        viewModelScope.launch {
            _syncState.value = CartSyncState.Syncing
            when (val result = safeApiCall { cartApi.getCart() }) {
                is ApiResult.Success -> {
                    // Merge: server items take precedence, local-only items are kept
                    val serverItems = result.data
                    val localItems = CartManager.cartItems.value
                    val serverProductIds = serverItems.map { it.productId }.toSet()
                    // Keep local items not on server, add all server items
                    val merged = serverItems + localItems.filter { it.productId !in serverProductIds }
                    CartManager.setCartItems(merged)
                    _syncState.value = CartSyncState.Synced
                }
                is ApiResult.ServerError -> {
                    _syncState.value = CartSyncState.Idle
                }
                is ApiResult.NetworkError -> {
                    // Offline — keep local cart as-is
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
        pushUpdateToServer(productId, newQty)
    }

    fun decreaseQuantity(productId: String) {
        val items = cartItems.value
        val item = items.find { it.productId == productId } ?: return
        val newQty = item.quantity - 1
        CartManager.updateQuantity(productId, newQty)
        if (newQty > 0) {
            pushUpdateToServer(productId, newQty)
        } else {
            pushRemoveToServer(productId)
        }
    }

    fun removeItem(productId: String) {
        CartManager.removeFromCart(productId)
        pushRemoveToServer(productId)
    }

    fun clearCart() {
        CartManager.clearCart()
        pushClearToServer()
    }

    private fun pushUpdateToServer(productId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                safeApiCall { cartApi.updateQuantity(productId, mapOf("quantity" to quantity)) }
            } catch (_: Exception) { /* best-effort sync */ }
        }
    }

    private fun pushRemoveToServer(productId: String) {
        viewModelScope.launch {
            try {
                safeApiCall { cartApi.removeFromCart(productId) }
            } catch (_: Exception) { /* best-effort sync */ }
        }
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