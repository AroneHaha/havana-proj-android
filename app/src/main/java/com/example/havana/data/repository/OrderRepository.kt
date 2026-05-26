package com.example.havana.data.repository

import com.example.havana.data.mock.MockData
import com.example.havana.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single source of truth for orders across the app.
 * Replaces the global mutable `mockOrdersList` from MainActivity.
 */
object OrderRepository {

    private val _orders = MutableStateFlow<List<Order>>(MockData.orders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    fun addOrder(order: Order) {
        _orders.update { current ->
            if (current.none { it.id == order.id }) {
                listOf(order) + current
            } else {
                current
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        _orders.update { current ->
            current.map { order ->
                if (order.id == orderId) order.copy(status = newStatus) else order
            }
        }
    }

    fun setOrders(orders: List<Order>) {
        _orders.value = orders
    }
}
