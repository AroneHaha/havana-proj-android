package com.example.havana.ui.screens.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.havana.data.cart.CartManager
import com.example.havana.data.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    val cartItems = CartManager.cartItems

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total.asStateFlow()

    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()

    init {
        // Observe cart changes and recompute totals
        viewModelScope.launch {
            cartItems.collect { items ->
                _total.value = items.sumOf { it.price * it.quantity }
                _itemCount.value = items.sumOf { it.quantity }
            }
        }
    }

    fun increaseQuantity(productId: String) {
        val items = cartItems.value
        val item = items.find { it.productId == productId } ?: return
        CartManager.updateQuantity(productId, item.quantity + 1)
    }

    fun decreaseQuantity(productId: String) {
        val items = cartItems.value
        val item = items.find { it.productId == productId } ?: return
        CartManager.updateQuantity(productId, item.quantity - 1)
    }

    fun removeItem(productId: String) {
        CartManager.removeFromCart(productId)
    }

    fun clearCart() {
        CartManager.clearCart()
    }
}