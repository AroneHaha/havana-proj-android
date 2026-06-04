package com.example.havana.data.repository

import com.example.havana.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Local cache for orders — populated by ViewModels from API responses.
 *
 * This is NOT a mock data source. Orders are fetched from the backend
 * via ViewModels (OrdersViewModel, OrderDetailsViewModel) and stored
 * here so that other screens (OrderDetailsScreen, OrderConfirmationScreen)
 * can access recently-fetched orders without re-fetching.
 *
 * Flow: API → ViewModel → OrderRepository.setOrders() → Screen reads from here
 */
object OrderRepository {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
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