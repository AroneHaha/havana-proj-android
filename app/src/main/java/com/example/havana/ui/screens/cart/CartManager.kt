package com.example.havana.data.cart

import android.content.Context
import android.content.SharedPreferences
import com.example.havana.data.model.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object CartManager {

    private const val PREFS_NAME = "havana_cart"
    private const val KEY_CART_ITEMS = "cart_items"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    /**
     * Initialize with Application context. Call once from MainActivity.onCreate().
     * Restores persisted cart items from SharedPreferences.
     */
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        restoreCart()
    }

    private fun restoreCart() {
        try {
            val json = prefs?.getString(KEY_CART_ITEMS, null)
            if (json != null) {
                val type = object : TypeToken<List<CartItem>>() {}.type
                val items: List<CartItem> = gson.fromJson(json, type)
                _cartItems.value = items
            }
        } catch (_: Exception) {
            _cartItems.value = emptyList()
        }
    }

    private fun persistCart() {
        try {
            val json = gson.toJson(_cartItems.value)
            prefs?.edit()?.putString(KEY_CART_ITEMS, json)?.apply()
        } catch (_: Exception) {
            // Silently fail — cart will just not persist
        }
    }

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
        persistCart()
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
        persistCart()
    }

    fun removeFromCart(productId: String) {
        _cartItems.update { currentList ->
            currentList.filter { it.productId != productId }
        }
        persistCart()
    }

    fun clearCart() {
        _cartItems.update { emptyList() }
        persistCart()
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