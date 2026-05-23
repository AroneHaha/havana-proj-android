package com.example.havana.data.cart

import com.example.havana.data.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object CartManager {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(item: CartItem) {
        _cartItems.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.productId == item.productId }
            if (existingIndex >= 0) {
                // Already in cart — increase quantity
                currentList.toMutableList().apply {
                    val existing = this[existingIndex]
                    this[existingIndex] = existing.copy(quantity = existing.quantity + item.quantity)
                }
            } else {
                currentList + item
            }
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        if (newQuantity < 1) {
            removeFromCart(productId)
            return
        }
        _cartItems.update { currentList ->
            currentList.map { item ->
                if (item.productId == productId) item.copy(quantity = newQuantity) else item
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.update { currentList ->
            currentList.filter { it.productId != productId }
        }
    }

    fun clearCart() {
        _cartItems.update { emptyList() }
    }

    fun getTotal(): Double {
        return _cartItems.value.sumOf { it.price * it.quantity }
    }

    fun getItemCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    fun isInCart(productId: String): Boolean {
        return _cartItems.value.any { it.productId == productId }
    }
}