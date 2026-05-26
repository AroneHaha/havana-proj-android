package com.example.havana.ui.screens.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.Order
import com.example.havana.data.model.OrderListState
import com.example.havana.data.repository.OrderRepository
import com.example.havana.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val orderApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.OrderApiService::class.java
    )

    private val _orderListState = MutableStateFlow<OrderListState>(OrderListState.Idle)
    val orderListState: StateFlow<OrderListState> = _orderListState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private var allOrders: List<Order> = emptyList()

    init {
        loadOrders()
    }

    fun loadOrders() {
        _orderListState.value = OrderListState.Loading
        viewModelScope.launch {
            try {
                val orders = orderApi.getOrders()
                allOrders = orders
                OrderRepository.setOrders(orders)
                _orderListState.value = OrderListState.Success(orders)
            } catch (_: Exception) {
                // Use OrderRepository (initialized from MockData) as fallback
                allOrders = OrderRepository.orders.value
                _orderListState.value = OrderListState.Success(allOrders)
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        filterOrders()
    }

    private fun filterOrders() {
        val filter = _selectedFilter.value
        // Re-read from OrderRepository to pick up any status changes
        allOrders = OrderRepository.orders.value
        val filtered = if (filter == "all") {
            allOrders
        } else {
            allOrders.filter { it.status == filter }
        }
        _orderListState.value = OrderListState.Success(filtered)
    }
}