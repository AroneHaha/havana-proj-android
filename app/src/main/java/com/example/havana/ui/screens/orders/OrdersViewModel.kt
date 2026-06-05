package com.example.havana.ui.screens.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.Order
import com.example.havana.data.model.OrderListState
import com.example.havana.data.repository.OrderRepository
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.safeApiCall
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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var allOrders: List<Order> = emptyList()

    init {
        loadOrders()
    }

    fun loadOrders() {
        _orderListState.value = OrderListState.Loading
        viewModelScope.launch {
            when (val result = safeApiCall { orderApi.getOrders() }) {
                is ApiResult.Success -> {
                    allOrders = result.data.data
                    OrderRepository.setOrders(allOrders)
                    _orderListState.value = OrderListState.Success(allOrders)
                }
                is ApiResult.ServerError -> {
                    _orderListState.value = OrderListState.Error(result.message)
                }
                is ApiResult.NetworkError -> {
                    allOrders = OrderRepository.orders.value
                    _orderListState.value = OrderListState.Success(allOrders)
                }
            }
        }
    }

    fun refreshOrders() {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = safeApiCall { orderApi.getOrders() }) {
                is ApiResult.Success -> {
                    allOrders = result.data.data
                    OrderRepository.setOrders(allOrders)
                    filterOrders()
                }
                else -> {}
            }
            _isRefreshing.value = false
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        filterOrders()
    }

    private fun filterOrders() {
        val filter = _selectedFilter.value
        allOrders = OrderRepository.orders.value
        val filtered = if (filter == "all") {
            allOrders
        } else {
            allOrders.filter { it.status == filter }
        }
        _orderListState.value = OrderListState.Success(filtered)
    }
}