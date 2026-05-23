package com.example.havana.ui.screens.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.Order
import com.example.havana.data.model.OrderItem
import com.example.havana.data.model.OrderListState
import com.example.havana.data.model.DeliveryAddress
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
                _orderListState.value = OrderListState.Success(orders)
            } catch (_: Exception) {
                // Mock fallback
                allOrders = getMockOrders()
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
        val filtered = if (filter == "all") {
            allOrders
        } else {
            allOrders.filter { it.status == filter }
        }
        _orderListState.value = OrderListState.Success(filtered)
    }

    // ===== MOCK DATA =====

    private fun getMockOrders(): List<Order> {
        return listOf(
            Order(
                id = "ord-1",
                orderNumber = "HAV-4821",
                customerName = "Fatima Al-Sabah",
                phone = "+965 5123 4567",
                deliveryAddress = DeliveryAddress(
                    fullAddress = "Salmiya, Salem Al Mubarak St, Hawalli Governorate, Kuwait",
                    latitude = 29.3375,
                    longitude = 48.0833
                ),
                notes = "Ring doorbell please",
                paymentMethod = "cod",
                items = listOf(
                    OrderItem("1", "Royal Red Roses", 27.500, 2, "Roses"),
                    OrderItem("5", "Golden Gift Box", 37.000, 1, "Gifts"),
                ),
                subtotal = 92.000,
                deliveryFee = 1.500,
                total = 93.500,
                status = "delivered",
                createdAt = "2026-05-20 14:30"
            ),
            Order(
                id = "ord-2",
                orderNumber = "HAV-5103",
                customerName = "Ahmed Hassan",
                phone = "+965 6234 5678",
                deliveryAddress = DeliveryAddress(
                    fullAddress = "Jabriya, Block 10, Street 5, Hawalli Governorate, Kuwait",
                    latitude = 29.3267,
                    longitude = 48.0044
                ),
                notes = "",
                paymentMethod = "cod",
                items = listOf(
                    OrderItem("9", "Peony Premium Box", 42.000, 1, "Bouquets"),
                ),
                subtotal = 42.000,
                deliveryFee = 1.500,
                total = 43.500,
                status = "out_for_delivery",
                createdAt = "2026-05-22 10:15"
            ),
            Order(
                id = "ord-3",
                orderNumber = "HAV-5210",
                customerName = "Sara Al-Ali",
                phone = "+965 9345 6789",
                deliveryAddress = DeliveryAddress(
                    fullAddress = "Kuwait City, Al Shuhada St, Al Asimah, Kuwait",
                    latitude = 29.3759,
                    longitude = 47.9774
                ),
                notes = "Leave at the gate",
                paymentMethod = "cod",
                items = listOf(
                    OrderItem("3", "Pink Blush Arrangement", 23.000, 1, "Arrangements"),
                    OrderItem("8", "Mini Rose Plant", 14.000, 2, "Plants"),
                ),
                subtotal = 51.000,
                deliveryFee = 1.500,
                total = 52.500,
                status = "preparing",
                createdAt = "2026-05-23 09:00"
            ),
            Order(
                id = "ord-4",
                orderNumber = "HAV-5345",
                customerName = "Mohammed Jassim",
                phone = "+965 5456 7890",
                deliveryAddress = DeliveryAddress(
                    fullAddress = "Hawalli, Ibn Khaldun St, Hawalli Governorate, Kuwait",
                    latitude = 29.2922,
                    longitude = 48.0089
                ),
                notes = "",
                paymentMethod = "cod",
                items = listOf(
                    OrderItem("11", "Romance Bundle", 30.000, 1, "Gifts"),
                ),
                subtotal = 30.000,
                deliveryFee = 1.500,
                total = 31.500,
                status = "confirmed",
                createdAt = "2026-05-23 11:30"
            ),
            Order(
                id = "ord-5",
                orderNumber = "HAV-5402",
                customerName = "Noor Al-Din",
                phone = "+965 8567 8901",
                deliveryAddress = DeliveryAddress(
                    fullAddress = "Mishref, Block 4, Street 20, Hawalli Governorate, Kuwait",
                    latitude = 29.2878,
                    longitude = 48.0653
                ),
                notes = "Call before delivery",
                paymentMethod = "cod",
                items = listOf(
                    OrderItem("2", "Sunset Bouquet", 20.000, 1, "Bouquets"),
                    OrderItem("12", "Daisy Delight", 10.000, 3, "Bouquets"),
                ),
                subtotal = 50.000,
                deliveryFee = 1.500,
                total = 51.500,
                status = "pending",
                createdAt = "2026-05-23 12:45"
            ),
            Order(
                id = "ord-6",
                orderNumber = "HAV-5520",
                customerName = "Layla Abbas",
                phone = "+965 6678 9012",
                deliveryAddress = DeliveryAddress(
                    fullAddress = "Salwa, Block 7, Street 1, Hawalli Governorate, Kuwait",
                    latitude = 29.3019,
                    longitude = 48.1178
                ),
                notes = "",
                paymentMethod = "cod",
                items = listOf(
                    OrderItem("6", "White Elegance", 29.000, 1, "Arrangements"),
                ),
                subtotal = 29.000,
                deliveryFee = 1.500,
                total = 30.500,
                status = "cancelled",
                createdAt = "2026-05-21 16:00"
            ),
        )
    }
}