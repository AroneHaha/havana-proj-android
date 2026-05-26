package com.example.havana.data.cart

import android.content.Context
import com.example.havana.data.model.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton that manages the shopping cart state.
 * Persists cart items to SharedPreferences so they survive process death.
 */
object CartManager {

    private const val PREFS_NAME = "havana_cart"
    private const val KEY_CART_ITEMS = "cart_items"

    private var prefs: android.content.SharedPreferences? = null
    private val gson = Gson()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Restore cart from SharedPreferences
        val json = prefs?.getString(KEY_CART_ITEMS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<CartItem>>() {}.type
                _cartItems.value = gson.fromJson(json, type)
            } catch (_: Exception) {
                _cartItems.value = emptyList()
            }
        }
    }

    fun addToCart(item: CartItem) {
        _cartItems.update { current ->
            val existing = current.find { it.productId == item.productId }
            if (existing != null) {
                current.map {
                    if (it.productId == item.productId) it.copy(quantity = it.quantity + item.quantity)
                    else it
                }
            } else {
                current + item
            }
        }
        persist()
    }

    fun updateQuantity(productId: String, quantity: Int) {
        _cartItems.update { current ->
            if (quantity <= 0) {
                current.filter { it.productId != productId }
            } else {
                current.map {
                    if (it.productId == productId) it.copy(quantity = quantity)
                    else it
                }
            }
        }
        persist()
    }

    fun removeFromCart(productId: String) {
        _cartItems.update { current ->
            current.filter { it.productId != productId }
        }
        persist()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        persist()
    }

    private fun persist() {
        val json = gson.toJson(_cartItems.value)
        prefs?.edit()?.putString(KEY_CART_ITEMS, json)?.apply()
    }
}
